package com.youraccident.detection.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.youraccident.detection.R;
import com.youraccident.detection.utils.SMSUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class TrackingActivity extends AppCompatActivity {

    private static final String TAG = "TrackingActivity";
    // Replace with your IoT device's MAC address
    private static final String DEVICE_ADDRESS = "00:11:22:33:44:55"; 
    // Standard Bluetooth SPP UUID
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private TextView textViewStatus, textViewCountdown;
    private Button buttonStopTracking;
    private FusedLocationProviderClient fusedLocationClient;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private Thread workerThread;

    private boolean isTracking = false;
    private boolean accidentDetected = false;
    private CountDownTimer countDownTimer;
    private volatile boolean stopWorker = false;

    private static final float ACCIDENT_THRESHOLD = 2.5f; // G-force threshold for IoT device
    private static final int PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        initViews();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkPermissionsAndStartTracking();
    }

    private void initViews() {
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewCountdown = findViewById(R.id.textViewCountdown);
        buttonStopTracking = findViewById(R.id.buttonStopTracking);

        buttonStopTracking.setOnClickListener(v -> {
            if (accidentDetected) {
                cancelAccidentAlert();
            } else {
                stopTracking();
            }
        });
    }

    private void checkPermissionsAndStartTracking() {
        if (hasRequiredPermissions()) {
            startTracking();
        } else {
            requestPermissions();
        }
    }

    private boolean hasRequiredPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.BLUETOOTH
        }, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTracking();
            } else {
                Toast.makeText(this, "Permissions denied! Cannot start tracking.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startTracking() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, 1);
            return;
        }

        connectToDevice();
    }

    private void connectToDevice() {
        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            beginListenForData();
            isTracking = true;
            textViewStatus.setText("Status: Connected to IoT Device");
            Toast.makeText(this, "Tracking started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect to device", e);
            Toast.makeText(this, "Failed to connect to IoT device", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void beginListenForData() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final byte[] buffer = new byte[1024];

        workerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                try {
                    int bytesAvailable = inputStream.read(buffer);
                    if (bytesAvailable > 0) {
                        String data = new String(buffer, 0, bytesAvailable);
                        handler.post(() -> processData(data));
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading from input stream", e);
                    stopWorker = true;
                }
            }
        });
        workerThread.start();
    }

    private void processData(String data) {
        // Assuming data is comma-separated: "x,y,z"
        try {
            String[] values = data.trim().split(",");
            if (values.length == 3) {
                float x = Float.parseFloat(values[0]);
                float y = Float.parseFloat(values[1]);
                float z = Float.parseFloat(values[2]);

                double gForce = Math.sqrt(x * x + y * y + z * z);

                if (gForce > ACCIDENT_THRESHOLD && !accidentDetected) {
                    accidentDetected = true;
                    handleAccidentDetection();
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid data format from IoT device", e);
        }
    }

    private void handleAccidentDetection() {
        textViewStatus.setText("Status: Accident Detected!");
        countDownTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                textViewCountdown.setText("Cancel in: " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                sendEmergencyAlerts();
            }
        }.start();
        buttonStopTracking.setText("CANCEL ALERT");
    }

    private void sendEmergencyAlerts() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                String message = createEmergencyMessage(location);
                sendSms(message);
            });
        } else {
            String message = createEmergencyMessage(null);
            sendSms(message);
        }
    }

    private String createEmergencyMessage(Location location) {
        if (location != null) {
            return "EMERGENCY: Accident detected! Location: https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
        } else {
            return "EMERGENCY: Accident detected! Location unavailable.";
        }
    }

    private void sendSms(String message) {
        boolean smsSent = SMSUtils.sendEmergencySMS(this, message);
        if (smsSent) {
            Toast.makeText(this, "Emergency alerts sent!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to send alerts!", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void stopTracking() {
        if (isTracking) {
            stopWorker = true;
            if (workerThread != null) {
                workerThread.interrupt();
            }
            try {
                if (inputStream != null) inputStream.close();
                if (bluetoothSocket != null) bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close resources", e);
            }
            isTracking = false;
            textViewStatus.setText("Status: Tracking Stopped");
            if (countDownTimer != null) countDownTimer.cancel();
            Toast.makeText(this, "Tracking stopped", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void cancelAccidentAlert() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        accidentDetected = false;
        textViewStatus.setText("Status: Alert Cancelled");
        textViewCountdown.setText("");
        buttonStopTracking.setText("STOP TRACKING");
        Toast.makeText(this, "Accident alert cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTracking();
    }
}
