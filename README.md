# Accident Detection System

An Android application that detects potential car accidents and automatically sends an SMS alert with the user's current location to their registered emergency contacts.

## Features

*   **User Authentication**: Secure sign-up and login functionality using Firebase Authentication.
*   **Automatic Accident Detection**: Uses the device's accelerometer to detect sudden impacts.
*   **Location Services**: Gets the user's precise location using GPS for the emergency alert.
*   **Automatic SMS Alerts**: Sends an SMS to emergency contacts with a Google Maps link to the user's location.
*   **Emergency Contacts**: Users can add and manage their emergency contacts within the app.
*   **Dashboard**: A central place to view user information and manage settings.
*   **Manual SOS**: A dedicated SOS button to manually trigger an alert.

## Permissions Required

The app requires the following permissions to function correctly:

*   `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_LOCATION`: To run the accident detection service in the background.
*   `ACCESS_FINE_LOCATION` & `ACCESS_COARSE_LOCATION`: To get the user's location.
*   `SEND_SMS`: To send the alert message.
*   `INTERNET`: For user authentication and other network operations.
*   `CALL_PHONE`: To allow making calls from the app.
*   `ACTIVITY_RECOGNITION`: To help optimize battery usage.
*   `WAKE_LOCK` & `VIBRATE`: For notifications and alerts.
*   `SYSTEM_ALERT_WINDOW`: To display alerts over other apps.

## Technologies Used

*   **Frontend**: Java, Android SDK
*   **Backend**: Firebase (Authentication)
*   **APIs**: Google Play Services for Location

## Setup

1.  **Clone the repository:**
    ```bash
    git clone <(https://github.com/Aman8959/AccidentDetectionSystem.git)>
    ```
2.  **Open in Android Studio:**
    Open the cloned project in Android Studio.
3.  **Firebase Setup:**
    *   Create a new project on the [Firebase Console](https://console.firebase.google.com/).
    *   Add an Android app to your Firebase project with the package name `com.youraccident.detection`.
    *   Download the `google-services.json` file and place it in the `app/` directory of the project.
4.  **Build and Run:**
    Sync the project with Gradle files and run it on an Android device or emulator.
