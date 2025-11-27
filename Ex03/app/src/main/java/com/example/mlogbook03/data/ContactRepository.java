package com.example.mlogbook03.data;


import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ContactRepository {
    private ContactDao contactDao;
    private LiveData<List<Contact>> allContacts;

    public ContactRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        contactDao = db.contactDao();
        allContacts = contactDao.getAllContacts();
    }

    public LiveData<List<Contact>> getAllContacts() { return allContacts; }

    public void insert(Contact contact) {
        new InsertAsyncTask(contactDao).execute(contact);
    }

    public void update(Contact contact) {
        new UpdateAsyncTask(contactDao).execute(contact);
    }

    public void delete(Contact contact) {
        new DeleteAsyncTask(contactDao).execute(contact);
    }

    private static class InsertAsyncTask extends AsyncTask<Contact, Void, Void> {
        private ContactDao dao;
        InsertAsyncTask(ContactDao dao){ this.dao = dao; }
        @Override protected Void doInBackground(Contact... contacts) {
            dao.insert(contacts[0]); return null;
        }
    }
    private static class UpdateAsyncTask extends AsyncTask<Contact, Void, Void> {
        private ContactDao dao;
        UpdateAsyncTask(ContactDao dao){ this.dao = dao; }
        @Override protected Void doInBackground(Contact... contacts) {
            dao.update(contacts[0]); return null;
        }
    }
    private static class DeleteAsyncTask extends AsyncTask<Contact, Void, Void> {
        private ContactDao dao;
        DeleteAsyncTask(ContactDao dao){ this.dao = dao; }
        @Override protected Void doInBackground(Contact... contacts) {
            dao.delete(contacts[0]); return null;
        }
    }
}

