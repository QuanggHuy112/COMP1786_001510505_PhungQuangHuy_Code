package com.example.mhike.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mhike.R;
import com.example.mhike.adapters.ObservationAdapter;
import com.example.mhike.database.DatabaseHelper;
import com.example.mhike.models.Observation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ObservationActivity extends AppCompatActivity {

    private ListView observationListView;
    private ObservationAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Observation> allObservations;
    private int hikeIdentifier;
    private String hikeName;
    private TextView emptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation);

        // Get hike info
        hikeIdentifier = getIntent().getIntExtra("hikeId", -1);
        hikeName = getIntent().getStringExtra("hikeName");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Observations - " + hikeName);

        // Initialize views
        observationListView = findViewById(R.id.observationListView);
        emptyMessage = findViewById(R.id.emptyMessage);
        Button btnAddObservation = findViewById(R.id.btnAddObservation);

        // Database helper
        dbHelper = new DatabaseHelper(this);

        // Add Observation button
        btnAddObservation.setOnClickListener(v -> openAddDialog());

        // List item click
        observationListView.setOnItemClickListener((parent, view, position, id) -> {
            Observation obs = allObservations.get(position);
            showOptionsDialog(obs);
        });

        loadAllObservations();
    }

    private void loadAllObservations() {
        allObservations = dbHelper.getObservationsByHike(hikeIdentifier);

        if (allObservations.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            observationListView.setVisibility(View.GONE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            observationListView.setVisibility(View.VISIBLE);
            adapter = new ObservationAdapter(this, allObservations);
            observationListView.setAdapter(adapter);
        }
    }

    private void openAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_observation, null);
        builder.setView(dialogView);

        EditText inputObsText = dialogView.findViewById(R.id.inputObsText);
        EditText inputObsTime = dialogView.findViewById(R.id.inputObsTime);
        EditText inputObsComments = dialogView.findViewById(R.id.inputObsComments);

        // Set current time
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        inputObsTime.setText(formatter.format(cal.getTime()));

        inputObsTime.setFocusable(false);
        inputObsTime.setOnClickListener(v -> openDateTimePicker(inputObsTime));

        builder.setTitle("Add Observation")
                .setPositiveButton("Add", (dialog, which) -> {
                    String obsText = inputObsText.getText().toString().trim();
                    String obsTime = inputObsTime.getText().toString().trim();
                    String obsComments = inputObsComments.getText().toString().trim();

                    if (obsText.isEmpty()) {
                        Toast.makeText(this, "Observation text is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Observation obs = new Observation(hikeIdentifier, obsText, obsTime, obsComments);
                    long result = dbHelper.addObservation(obs);

                    if (result > 0) {
                        Toast.makeText(this, "Observation added", Toast.LENGTH_SHORT).show();
                        loadAllObservations();
                    } else {
                        Toast.makeText(this, "Failed to add observation", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openDateTimePicker(EditText inputField) {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (timeView, hour, minute) -> {
                                cal.set(Calendar.HOUR_OF_DAY, hour);
                                cal.set(Calendar.MINUTE, minute);

                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                inputField.setText(formatter.format(cal.getTime()));
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.show();
    }

    private void showOptionsDialog(Observation obs) {
        String[] options = {"View Details", "Edit", "Delete"};

        new AlertDialog.Builder(this)
                .setTitle("Choose Action")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showDetailDialog(obs);
                    else if (which == 1) openEditDialog(obs);
                    else confirmDeleteDialog(obs);
                })
                .show();
    }

    private void showDetailDialog(Observation obs) {
        StringBuilder details = new StringBuilder();
        details.append("Observation:\n").append(obs.getObservationText()).append("\n\n");
        details.append("Time:\n").append(obs.getTime()).append("\n\n");

        if (obs.getComments() != null && !obs.getComments().isEmpty()) {
            details.append("Comments:\n").append(obs.getComments());
        } else {
            details.append("Comments: None");
        }

        new AlertDialog.Builder(this)
                .setTitle("Observation Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void openEditDialog(Observation obs) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_observation, null);
        builder.setView(dialogView);

        EditText inputObsText = dialogView.findViewById(R.id.inputObsText);
        EditText inputObsTime = dialogView.findViewById(R.id.inputObsTime);
        EditText inputObsComments = dialogView.findViewById(R.id.inputObsComments);

        inputObsText.setText(obs.getObservationText());
        inputObsTime.setText(obs.getTime());
        inputObsComments.setText(obs.getComments());

        inputObsTime.setFocusable(false);
        inputObsTime.setOnClickListener(v -> openDateTimePicker(inputObsTime));

        builder.setTitle("Edit Observation")
                .setPositiveButton("Update", (dialog, which) -> {
                    String obsText = inputObsText.getText().toString().trim();
                    String obsTime = inputObsTime.getText().toString().trim();
                    String obsComments = inputObsComments.getText().toString().trim();

                    if (obsText.isEmpty()) {
                        Toast.makeText(this, "Observation text is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    obs.setObservationText(obsText);
                    obs.setTime(obsTime);
                    obs.setComments(obsComments);

                    int result = dbHelper.updateObservation(obs);

                    if (result > 0) {
                        Toast.makeText(this, "Observation updated", Toast.LENGTH_SHORT).show();
                        loadAllObservations();
                    } else {
                        Toast.makeText(this, "Failed to update observation", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteDialog(Observation obs) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Observation")
                .setMessage("Are you sure you want to delete this observation?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteObservation(obs.getId());
                    Toast.makeText(this, "Observation deleted", Toast.LENGTH_SHORT).show();
                    loadAllObservations();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
