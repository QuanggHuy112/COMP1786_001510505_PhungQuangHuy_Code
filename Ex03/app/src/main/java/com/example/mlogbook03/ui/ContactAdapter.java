package com.example.mlogbook03.ui;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mlogbook03.R;
import com.example.mlogbook03.data.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<Contact> contacts = new ArrayList<>();
    private Context context;

    public ContactAdapter(Context ctx) { this.context = ctx; }

    public void setContacts(List<Contact> newContacts) {
        this.contacts = newContacts;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(v);
    }

    @Override public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact c = contacts.get(position);
        holder.name.setText(c.name);
        holder.phone.setText(c.phone);
        holder.avatar.setImageResource(c.avatarResId > 0 ? c.avatarResId : R.drawable.ic_person_placeholder);

        holder.itemView.setOnClickListener(v -> {
            // open AddEdit contact in edit mode
            Intent i = new Intent(context, AddEditContactActivity.class);
            i.putExtra("contact_id", c.id);
            context.startActivity(i);
        });
    }

    @Override public int getItemCount() { return contacts.size(); }

    public Contact getContactAt(int pos) { return contacts.get(pos); }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView name, phone;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.item_avatar);
            name = itemView.findViewById(R.id.item_name);
            phone = itemView.findViewById(R.id.item_phone);
        }
    }
}

