package com.example.mhike.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mhike.R;
import com.example.mhike.adapters.HikeAdapter;
import com.example.mhike.database.DatabaseHelper;
import com.example.mhike.models.Hike;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView hikeListView;
    private HikeAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Hike> allHikes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Views
        hikeListView = findViewById(R.id.hikeListView);
        Button addHikingButton = findViewById(R.id.btnAddHiking);

        // Database
        dbHelper = new DatabaseHelper(this);

        // New button listener
        addHikingButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddHikeActivity.class);
            startActivity(intent);
        });

        // List click
        hikeListView.setOnItemClickListener((parent, view, position, id) -> {
            Hike selectedHike = allHikes.get(position);
            Intent intent = new Intent(MainActivity.this, HikeDetailActivity.class);
            intent.putExtra("hike", selectedHike);
            startActivity(intent);
        });

        loadAllHikes();

        // If activity is started with refresh extra (edge-case when first created)
        Intent startingIntent = getIntent();
        if (startingIntent != null && startingIntent.getBooleanExtra("refresh", false)) {
            loadAllHikes();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // keep this - safe fallback (reload every resume)
        loadAllHikes();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // update stored intent
        if (intent != null && intent.getBooleanExtra("refresh", false)) {
            loadAllHikes();
        }
    }

    private void loadAllHikes() {
        if (dbHelper == null) dbHelper = new DatabaseHelper(this);
        allHikes = dbHelper.getAllHikes();
        adapter = new HikeAdapter(this, allHikes);
        hikeListView.setAdapter(adapter);

        if (allHikes == null || allHikes.isEmpty()) {
            Toast.makeText(this, "No hikes found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Custom SEARCH click
        if (menu.findItem(R.id.action_search) != null && menu.findItem(R.id.action_search).getActionView() != null) {
            menu.findItem(R.id.action_search).getActionView()
                    .setOnClickListener(v -> {
                        onOptionsItemSelected(menu.findItem(R.id.action_search));
                    });
        }

        // Custom DELETE click
        if (menu.findItem(R.id.action_delete_all) != null && menu.findItem(R.id.action_delete_all).getActionView() != null) {
            menu.findItem(R.id.action_delete_all).getActionView()
                    .setOnClickListener(v -> {
                        onOptionsItemSelected(menu.findItem(R.id.action_delete_all));
                    });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        } else if (id == R.id.action_delete_all) {
            confirmDeleteAll();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteAll() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete All Hikes")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteAllHikes();
                    loadAllHikes();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
