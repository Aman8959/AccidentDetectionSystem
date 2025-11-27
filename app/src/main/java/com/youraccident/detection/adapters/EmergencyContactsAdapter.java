package com.youraccident.detection.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.youraccident.detection.R;
import com.youraccident.detection.models.EmergencyContact;
import java.util.List;

public class EmergencyContactsAdapter extends ArrayAdapter<EmergencyContact> {

    private final List<EmergencyContact> contacts;
    private final OnDeleteButtonClickListener listener;

    public interface OnDeleteButtonClickListener {
        void onDeleteClick(int position);
    }

    public EmergencyContactsAdapter(Context context, List<EmergencyContact> contacts, OnDeleteButtonClickListener listener) {
        super(context, 0, contacts);
        this.contacts = contacts;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_emergency_contact, parent, false);
        }

        EmergencyContact contact = contacts.get(position);

        TextView textViewName = convertView.findViewById(R.id.textViewName);
        TextView textViewNumber = convertView.findViewById(R.id.textViewNumber);
        ImageButton buttonDelete = convertView.findViewById(R.id.buttonDelete);
        ImageView imageViewCallIcon = convertView.findViewById(R.id.imageViewCallIcon);

        textViewName.setText(contact.getName());
        textViewNumber.setText(contact.getPhoneNumber());

        if (contact.shouldCall()) {
            imageViewCallIcon.setVisibility(View.VISIBLE);
        } else {
            imageViewCallIcon.setVisibility(View.GONE);
        }

        buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });

        return convertView;
    }
}
