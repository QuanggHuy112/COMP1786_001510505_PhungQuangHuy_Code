package com.example.mhike.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mhike.R;
import com.example.mhike.models.Hike;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddHikeActivity extends AppCompatActivity {

    private EditText inputHikeName, inputHikeLocation, inputHikeDate, inputHikeDistance;
    private EditText inputHikeDesc, inputHikeWeatherInfo, inputHikeTeamSize;
    private RadioGroup parkingRadioGroup;
    private RadioButton parkingYesOption, parkingNoOption;

    // Difficulty radio group
    private RadioGroup difficultyRadioGroup;
    private RadioButton difficultyEasyOption, difficultyMediumOption, difficultyHardOption;

    private Button continueButton;

    private Calendar cal;
    private Hike currentHike = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hike);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        setupViews();

        // Setup date picker
        cal = Calendar.getInstance();
        setupDateSelection();

        // Check if editing existing hike
        if (getIntent().hasExtra("hike")) {
            currentHike = (Hike) getIntent().getSerializableExtra("hike");
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Hike");
            fillFormFields(currentHike);
        } else {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Add New Hike");
        }

        // Setup confirm button
        continueButton.setOnClickListener(v -> checkFormAndContinue());
    }

    private void setupViews() {
        inputHikeName = findViewById(R.id.inputHikeName);
        inputHikeLocation = findViewById(R.id.inputHikeLocation);
        inputHikeDate = findViewById(R.id.inputHikeDate);
        inputHikeDistance = findViewById(R.id.inputHikeDistance);
        inputHikeDesc = findViewById(R.id.inputHikeDesc);
        inputHikeWeatherInfo = findViewById(R.id.inputHikeWeatherInfo);
        inputHikeTeamSize = findViewById(R.id.inputHikeTeamSize);

        parkingRadioGroup = findViewById(R.id.parkingRadioGroup);
        parkingYesOption = findViewById(R.id.parkingYesOption);
        parkingNoOption = findViewById(R.id.parkingNoOption);

        // Difficulty radios
        difficultyRadioGroup = findViewById(R.id.difficultyRadioGroup);
        difficultyEasyOption = findViewById(R.id.difficultyEasyOption);
        difficultyMediumOption = findViewById(R.id.difficultyMediumOption);
        difficultyHardOption = findViewById(R.id.difficultyHardOption);

        continueButton = findViewById(R.id.continueButton);
    }

    private void setupDateSelection() {
        inputHikeDate.setFocusable(false);
        inputHikeDate.setClickable(true);
        inputHikeDate.setOnClickListener(v -> displayDatePicker());
    }

    private void displayDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    refreshDateField();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void refreshDateField() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        inputHikeDate.setText(formatter.format(cal.getTime()));
    }

    private void fillFormFields(Hike hike) {
        inputHikeName.setText(hike.getName());
        inputHikeLocation.setText(hike.getLocation());
        inputHikeDate.setText(hike.getDate());
        inputHikeDistance.setText(String.valueOf(hike.getLength()));
        inputHikeDesc.setText(hike.getDescription());
        inputHikeWeatherInfo.setText(hike.getWeather());
        inputHikeTeamSize.setText(String.valueOf(hike.getGroupSize()));

        if (hike.isParking()) {
            parkingYesOption.setChecked(true);
        } else {
            parkingNoOption.setChecked(true);
        }

        // Set difficulty radio based on hike.getDifficulty()
        String diff = hike.getDifficulty();
        if (diff == null) diff = "";
        switch (diff.toLowerCase(Locale.ROOT)) {
            case "easy":
                difficultyEasyOption.setChecked(true);
                break;
            case "medium":
                difficultyMediumOption.setChecked(true);
                break;
            case "hard":
                difficultyHardOption.setChecked(true);
                break;
            default:
                // none selected
                difficultyRadioGroup.clearCheck();
                break;
        }
    }

    private void checkFormAndContinue() {
        boolean formValid = true;

        // Validate name
        String hikeName = inputHikeName.getText().toString().trim();
        if (hikeName.isEmpty()) {
            inputHikeName.setError("Name is required");
            formValid = false;
        }

        // Validate location
        String hikeLocation = inputHikeLocation.getText().toString().trim();
        if (hikeLocation.isEmpty()) {
            inputHikeLocation.setError("Location is required");
            formValid = false;
        }

        // Validate date
        String hikeDate = inputHikeDate.getText().toString().trim();
        if (hikeDate.isEmpty()) {
            inputHikeDate.setError("Date is required");
            formValid = false;
        }

        // Validate length
        String distanceText = inputHikeDistance.getText().toString().trim();
        double hikeDistance = 0;
        if (distanceText.isEmpty()) {
            inputHikeDistance.setError("Length is required");
            formValid = false;
        } else {
            try {
                hikeDistance = Double.parseDouble(distanceText);
                if (hikeDistance <= 0) {
                    inputHikeDistance.setError("Length must be greater than 0");
                    formValid = false;
                }
            } catch (NumberFormatException e) {
                inputHikeDistance.setError("Invalid length format");
                formValid = false;
            }
        }

        // Validate parking
        if (parkingRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select parking availability", Toast.LENGTH_SHORT).show();
            formValid = false;
        }

        // Validate difficulty (now required)
        if (difficultyRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select difficulty", Toast.LENGTH_SHORT).show();
            formValid = false;
        }

        if (!formValid) {
            Toast.makeText(this, "Please fix the errors", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create hike object
        Hike hike = new Hike();
        if (currentHike != null) {
            hike.setId(currentHike.getId());
        }

        hike.setName(hikeName);
        hike.setLocation(hikeLocation);
        hike.setDate(hikeDate);
        hike.setParking(parkingYesOption.isChecked());
        hike.setLength(hikeDistance);

        // Read difficulty from selected radio
        int selectedDiffId = difficultyRadioGroup.getCheckedRadioButtonId();
        String difficultyValue = "Easy"; // default fallback
        if (selectedDiffId == difficultyEasyOption.getId()) {
            difficultyValue = "Easy";
        } else if (selectedDiffId == difficultyMediumOption.getId()) {
            difficultyValue = "Medium";
        } else if (selectedDiffId == difficultyHardOption.getId()) {
            difficultyValue = "Hard";
        }
        hike.setDifficulty(difficultyValue);

        hike.setDescription(inputHikeDesc.getText().toString().trim());
        hike.setWeather(inputHikeWeatherInfo.getText().toString().trim());

        String teamSizeText = inputHikeTeamSize.getText().toString().trim();
        if (!teamSizeText.isEmpty()) {
            try {
                hike.setGroupSize(Integer.parseInt(teamSizeText));
            } catch (NumberFormatException e) {
                hike.setGroupSize(0);
            }
        }

        // Go to confirmation screen
        Intent intent = new Intent(AddHikeActivity.this, ConfirmationActivity.class);
        intent.putExtra("hike", hike);
        intent.putExtra("isEdit", currentHike != null);
        startActivity(intent);
        finish();
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
