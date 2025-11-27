package com.example.mhike.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mhike.R;
import com.example.mhike.database.DatabaseHelper;
import com.example.mhike.models.Hike;

public class HikeDetailActivity extends AppCompatActivity {

    private TextView nameDisplay, locationDisplay, dateDisplay, parkingDisplay;
    private TextView distanceDisplay, difficultyDisplay, descriptionDisplay;
    private TextView weatherDisplay, teamSizeDisplay;
    private Button viewObservationsBtn;
    private DatabaseHelper dbHelper;
    private Hike currentHike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_detail);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Hike Details");

        // Initialize views
        setupViews();

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Get hike data
        currentHike = (Hike) getIntent().getSerializableExtra("hike");

        // Display hike details
        showHikeInfo();

        // Setup button
        viewObservationsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HikeDetailActivity.this, ObservationActivity.class);
            intent.putExtra("hikeId", currentHike.getId());
            intent.putExtra("hikeName", currentHike.getName());
            startActivity(intent);
        });
    }

    private void setupViews() {
        nameDisplay = findViewById(R.id.nameDisplay);
        locationDisplay = findViewById(R.id.locationDisplay);
        dateDisplay = findViewById(R.id.dateDisplay);
        parkingDisplay = findViewById(R.id.parkingDisplay);
        distanceDisplay = findViewById(R.id.distanceDisplay);
        difficultyDisplay = findViewById(R.id.difficultyDisplay);
        descriptionDisplay = findViewById(R.id.descriptionDisplay);
        weatherDisplay = findViewById(R.id.weatherDisplay);
        teamSizeDisplay = findViewById(R.id.teamSizeDisplay);
        viewObservationsBtn = findViewById(R.id.viewObservationsBtn);
    }

    private void showHikeInfo() {
        nameDisplay.setText(currentHike.getName());
        locationDisplay.setText(currentHike.getLocation());
        dateDisplay.setText(currentHike.getDate());
        parkingDisplay.setText(currentHike.isParking() ? "Available" : "Not Available");
        distanceDisplay.setText(currentHike.getLength() + " km");
        difficultyDisplay.setText(currentHike.getDifficulty());

        if (currentHike.getDescription() != null && !currentHike.getDescription().isEmpty()) {
            descriptionDisplay.setText(currentHike.getDescription());
        } else {
            descriptionDisplay.setText("No description");
        }

        if (currentHike.getWeather() != null && !currentHike.getWeather().isEmpty()) {
            weatherDisplay.setText(currentHike.getWeather());
        } else {
            weatherDisplay.setText("Not specified");
        }

        if (currentHike.getGroupSize() > 0) {
            teamSizeDisplay.setText(String.valueOf(currentHike.getGroupSize()));
        } else {
            teamSizeDisplay.setText("Not specified");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            Intent intent = new Intent(HikeDetailActivity.this, AddHikeActivity.class);
            intent.putExtra("hike", currentHike);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hike")
                .setMessage("Are you sure you want to delete this hike? All related observations will also be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteHike(currentHike.getId());
                    Toast.makeText(HikeDetailActivity.this, "Hike deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
