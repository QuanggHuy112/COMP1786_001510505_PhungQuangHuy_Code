package com.example.mlogbook03.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mlogbook03.R;
import com.example.mlogbook03.data.Contact;
import com.example.mlogbook03.viewmodel.ContactViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class AddEditContactActivity extends AppCompatActivity {

    private EditText etName, etPhone;
    private ImageView ivAvatar;
    private ContactViewModel viewModel;

    private int selectedAvatarRes = R.drawable.avatar_01; // default avatar
    private int editingContactId = -1;
    private Contact editingContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_contact);

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        ivAvatar = findViewById(R.id.iv_avatar_preview);
        Button btnChooseAvatar = findViewById(R.id.btn_choose_avatar);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnDelete = findViewById(R.id.btn_delete); // phải có trong layout (visibility="gone" mặc định)

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        // hide delete by default
        if (btnDelete != null) {
            btnDelete.setVisibility(View.GONE);
        }

        // ------------------ EDIT MODE ------------------
        if (getIntent() != null && getIntent().hasExtra("contact_id")) {
            editingContactId = getIntent().getIntExtra("contact_id", -1);
            if (editingContactId != -1) {
                viewModel.getAllContacts().observe(this, contacts -> {
                    for (Contact c : contacts) {
                        if (c.id == editingContactId) {
                            editingContact = c;
                            etName.setText(c.name);
                            etPhone.setText(c.phone);
                            selectedAvatarRes = c.avatarResId;
                            ivAvatar.setImageResource(selectedAvatarRes);
                            // show delete button when editing an existing contact
                            if (btnDelete != null) {
                                btnDelete.setVisibility(View.VISIBLE);
                            }
                            break;
                        }
                    }
                });
            }
        } else {
            ivAvatar.setImageResource(selectedAvatarRes);
            if (btnDelete != null) {
                btnDelete.setVisibility(View.GONE);
            }
        }

        // ------------------ CHOOSE AVATAR ------------------
        btnChooseAvatar.setOnClickListener(v -> openAvatarBottomSheet());

        // ------------------ SAVE CONTACT ------------------
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Enter Name and Phone Number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (editingContact != null) {
                editingContact.name = name;
                editingContact.phone = phone;
                editingContact.avatarResId = selectedAvatarRes;
                viewModel.update(editingContact);
            } else {
                Contact newContact = new Contact(name, phone, selectedAvatarRes);
                viewModel.insert(newContact);
            }

            finish();
        });

        // ------------------ DELETE CONTACT ------------------
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                if (editingContact != null) {
                    viewModel.delete(editingContact);
                    Toast.makeText(this, "Delete Successful;", Toast.LENGTH_SHORT).show();
                }
                finish();
            });
        }
    }

    // ------------------ AVATAR BOTTOMSHEET ------------------
    private void openAvatarBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_avatar_picker, null);
        dialog.setContentView(view);

        // Gán onclick cho từng avatar
        view.findViewById(R.id.avatar_01).setOnClickListener(v -> selectAvatar(R.drawable.avatar_01, dialog));
        view.findViewById(R.id.avatar_02).setOnClickListener(v -> selectAvatar(R.drawable.avatar_02, dialog));
        view.findViewById(R.id.avatar_03).setOnClickListener(v -> selectAvatar(R.drawable.avatar_03, dialog));
        view.findViewById(R.id.avatar_04).setOnClickListener(v -> selectAvatar(R.drawable.avatar_04, dialog));

        dialog.show();
    }

    private void selectAvatar(int resId, BottomSheetDialog dialog) {
        selectedAvatarRes = resId;
        ivAvatar.setImageResource(resId);
        dialog.dismiss();
    }
}
