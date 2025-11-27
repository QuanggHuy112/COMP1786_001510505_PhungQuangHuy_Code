package com.example.mhike.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mhike.R;
import com.example.mhike.adapters.HikeAdapter;
import com.example.mhike.database.DatabaseHelper;
import com.example.mhike.models.Hike;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RadioGroup searchTypeGroup;
    private RadioButton simpleSearchOption, advancedSearchOption;
    private EditText simpleSearchInput, searchNameInput, searchLocationInput;
    private EditText searchDistanceInput, searchDateInput;
    private Button performSearchButton;
    private ListView resultsListView;
    private TextView noResultsMessage;
    private View simpleSearchLayout, advancedSearchLayout;

    private DatabaseHelper dbHelper;
    private HikeAdapter adapter;
    private List<Hike> foundHikes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Search Hikes");

        // Initialize views
        setupViews();

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Setup radio group listener
        searchTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.simpleSearchOption) {
                simpleSearchLayout.setVisibility(View.VISIBLE);
                advancedSearchLayout.setVisibility(View.GONE);
            } else if (checkedId == R.id.advancedSearchOption) {
                simpleSearchLayout.setVisibility(View.GONE);
                advancedSearchLayout.setVisibility(View.VISIBLE);
            }
            clearSearchResults();
        });

        // Setup search button
        performSearchButton.setOnClickListener(v -> executeSearch());

        // Setup ListView click listener
        resultsListView.setOnItemClickListener((parent, view, position, id) -> {
            Hike selectedHike = foundHikes.get(position);
            Intent intent = new Intent(SearchActivity.this, HikeDetailActivity.class);
            intent.putExtra("hike", selectedHike);
            startActivity(intent);
        });
    }

    private void setupViews() {
        searchTypeGroup = findViewById(R.id.searchTypeGroup);
        simpleSearchOption = findViewById(R.id.simpleSearchOption);
        advancedSearchOption = findViewById(R.id.advancedSearchOption);

        simpleSearchLayout = findViewById(R.id.simpleSearchLayout);
        advancedSearchLayout = findViewById(R.id.advancedSearchLayout);

        simpleSearchInput = findViewById(R.id.simpleSearchInput);
        searchNameInput = findViewById(R.id.searchNameInput);
        searchLocationInput = findViewById(R.id.searchLocationInput);
        searchDistanceInput = findViewById(R.id.searchDistanceInput);
        searchDateInput = findViewById(R.id.searchDateInput);

        performSearchButton = findViewById(R.id.performSearchButton);
        resultsListView = findViewById(R.id.resultsListView);
        noResultsMessage = findViewById(R.id.noResultsMessage);
    }

    private void executeSearch() {
        if (simpleSearchOption.isChecked()) {
            doSimpleSearch();
        } else {
            doAdvancedSearch();
        }
    }

    private void doSimpleSearch() {
        String keyword = simpleSearchInput.getText().toString().trim();

        if (keyword.isEmpty()) {
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }

        foundHikes = dbHelper.searchHikesByName(keyword);
        showSearchResults();
    }

    private void doAdvancedSearch() {
        String name = searchNameInput.getText().toString().trim();
        String location = searchLocationInput.getText().toString().trim();
        String distance = searchDistanceInput.getText().toString().trim();
        String date = searchDateInput.getText().toString().trim();

        if (name.isEmpty() && location.isEmpty() && distance.isEmpty() && date.isEmpty()) {
            Toast.makeText(this, "Please enter at least one search criteria", Toast.LENGTH_SHORT).show();
            return;
        }

        foundHikes = dbHelper.advancedSearch(name, location, distance, date);
        showSearchResults();
    }

    private void showSearchResults() {
        if (foundHikes.isEmpty()) {
            noResultsMessage.setVisibility(View.VISIBLE);
            resultsListView.setVisibility(View.GONE);
            Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
        } else {
            noResultsMessage.setVisibility(View.GONE);
            resultsListView.setVisibility(View.VISIBLE);
            adapter = new HikeAdapter(this, foundHikes);
            resultsListView.setAdapter(adapter);
            Toast.makeText(this, foundHikes.size() + " result(s) found", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearSearchResults() {
        foundHikes = new ArrayList<>();
        noResultsMessage.setVisibility(View.GONE);
        resultsListView.setVisibility(View.GONE);
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
