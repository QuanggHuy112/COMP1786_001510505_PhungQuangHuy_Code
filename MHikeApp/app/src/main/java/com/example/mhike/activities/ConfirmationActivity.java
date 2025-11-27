package com.example.mhike.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mhike.R;
import com.example.mhike.database.DatabaseHelper;
import com.example.mhike.models.Hike;

public class ConfirmationActivity extends AppCompatActivity {

    private static final String TAG = "ConfirmationActivity";

    private TextView summaryText;
    private Button saveButton, editButton;
    private DatabaseHelper dbHelper;
    private Hike currentHike;
    private boolean isEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Confirm Details");
        }

        // Initialize views
        summaryText = findViewById(R.id.summaryText);
        saveButton = findViewById(R.id.saveButton);
        editButton = findViewById(R.id.editButton);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Get hike data (null-check để tránh crash)
        currentHike = (Hike) getIntent().getSerializableExtra("hike");
        isEditMode = getIntent().getBooleanExtra("isEdit", false);

        if (currentHike == null) {
            Toast.makeText(this, "No hike data provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display summary
        showSummary();

        // Setup buttons
        saveButton.setOnClickListener(v -> saveHikeData());
        editButton.setOnClickListener(v -> finish());
    }

    private void showSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Name: ").append(currentHike.getName()).append("\n\n");
        summary.append("Location: ").append(currentHike.getLocation()).append("\n\n");
        summary.append("Date: ").append(currentHike.getDate()).append("\n\n");
        summary.append("Parking: ").append(currentHike.isParking() ? "Yes" : "No").append("\n\n");
        summary.append("Length: ").append(currentHike.getLength()).append(" km\n\n");
        summary.append("Difficulty: ").append(currentHike.getDifficulty()).append("\n\n");

        if (currentHike.getDescription() != null && !currentHike.getDescription().isEmpty()) {
            summary.append("Description: ").append(currentHike.getDescription()).append("\n\n");
        }

        if (currentHike.getWeather() != null && !currentHike.getWeather().isEmpty()) {
            summary.append("Weather Expectation: ").append(currentHike.getWeather()).append("\n\n");
        }

        if (currentHike.getGroupSize() > 0) {
            summary.append("Group Size: ").append(currentHike.getGroupSize()).append("\n\n");
        }

        summaryText.setText(summary.toString());
    }

    private void saveHikeData() {
        try {
            if (isEditMode) {
                int rowsAffected = dbHelper.updateHike(currentHike);
                if (rowsAffected > 0) {
                    Toast.makeText(this, "Hike updated successfully", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Hike updated id=" + currentHike.getId());

                    // Notify MainActivity to refresh
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("refresh", true);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Failed to update hike", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "updateHike returned 0 rowsAffected for id=" + currentHike.getId());
                }
            } else {
                long newId = dbHelper.addHike(currentHike);
                if (newId > 0) {
                    // set id vào object (tiện cho sau này)
                    currentHike.setId((int) newId);
                    Toast.makeText(this, "Hike saved successfully", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Inserted hike id=" + newId);

                    // Mở MainActivity với flag CLEAR_TOP để reuse existing MainActivity và trigger onNewIntent
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("refresh", true);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Failed to save hike", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "addHike returned id <= 0");
                }
            }
        } catch (Exception e) {
            // Bắt exceptions từ DB để dễ debug
            Toast.makeText(this, "DB error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "DB exception", e);
            e.printStackTrace();
        } finally {
            // Đóng helper nếu cần
            try {
                dbHelper.close();
            } catch (Exception ignored) {
            }
            finish(); // quay lại MainActivity
        }
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
