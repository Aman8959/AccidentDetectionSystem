package com.youraccident.detection.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.youraccident.detection.R;
import com.youraccident.detection.models.EmergencyContact;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private final Context context;
    private final List<EmergencyContact> contactList;
    private final Runnable saveCallback;

    public ContactAdapter(Context context, List<EmergencyContact> contactList, Runnable saveCallback) {
        this.context = context;
        this.contactList = contactList;
        this.saveCallback = saveCallback;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        EmergencyContact contact = contactList.get(position);
        holder.editTextName.setText(contact.getName());
        holder.editTextPhone.setText(contact.getPhoneNumber());
        holder.checkBoxCall.setChecked(contact.shouldCall());

        holder.buttonDelete.setOnClickListener(v -> {
            contactList.remove(position);
            notifyDataSetChanged();
            saveCallback.run();
        });

        // Add text watchers or focus change listeners to save changes
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        EditText editTextName, editTextPhone;
        CheckBox checkBoxCall;
        ImageButton buttonDelete;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            editTextName = itemView.findViewById(R.id.editTextName);
            editTextPhone = itemView.findViewById(R.id.editTextPhone);
            checkBoxCall = itemView.findViewById(R.id.checkBoxCall);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
