package com.example.mlogbook03.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mlogbook03.data.Contact;
import com.example.mlogbook03.data.ContactRepository;

import java.util.List;

public class ContactViewModel extends AndroidViewModel {
    private ContactRepository repository;
    private LiveData<List<Contact>> allContacts;

    public ContactViewModel(@NonNull Application application) {
        super(application);
        repository = new ContactRepository(application);
        allContacts = repository.getAllContacts();
    }

    public LiveData<List<Contact>> getAllContacts() { return allContacts; }
    public void insert(Contact contact) { repository.insert(contact); }
    public void update(Contact contact) { repository.update(contact); }
    public void delete(Contact contact) { repository.delete(contact); }
}

