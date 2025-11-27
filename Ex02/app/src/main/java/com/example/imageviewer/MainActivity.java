package com.example.imageviewer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Image Viewer (Java)
 * - UI (activity_main.xml) is separate from logic here.
 * - Images are stored in res/drawable: img1, img2, img3, ...
 * - Next wraps to first when at last; Previous wraps to last when at first.
 */
public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button btnPrevious;
    private Button btnNext;

    // List of drawable resource IDs (edit/add images as needed)
    @DrawableRes
    private final int[] images = {
            R.drawable.img1,
            R.drawable.img2,
            R.drawable.img3
    };

    // current index into images[]
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // UI separated

        // bind views
        imageView = findViewById(R.id.imageView);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);

        // initial UI state
        updateUI();

        // listeners (logic separated here)
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showPrevious(); }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showNext(); }
        });
    }

    private void updateUI() {
        // set the image resource
        imageView.setImageResource(images[index]);
        // Buttons remain enabled (wrapping behavior), no enable/disable necessary.
    }

    private void showNext() {
        if (images.length == 0) return;
        index++;
        if (index >= images.length) index = 0; // wrap to first
        updateUI();
    }

    private void showPrevious() {
        if (images.length == 0) return;
        index--;
        if (index < 0) index = images.length - 1; // wrap to last
        updateUI();
    }
}
