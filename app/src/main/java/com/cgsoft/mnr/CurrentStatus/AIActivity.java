package com.cgsoft.mnr.CurrentStatus;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgsoft.mnr.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

// AI Screen Back-end.

public class AIActivity extends AppCompatActivity {

    // Global Variables.
    ArrayList<String> aiUnits;
    DatabaseReference mbase;
    LinearLayout view;
    TextView branchTV, locationTV;
    private View mLoading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_i);

        // Initialize variables.
        view = findViewById(R.id.mainLayout);
        branchTV = findViewById(R.id.branch);
        locationTV = findViewById(R.id.location);
        mLoading = findViewById(R.id.prgsbar);
        // Prevent touches on loading screen.
        mLoading.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        aiUnits = new ArrayList<String>();
        showLoading();

        // Fetch variables from Intent.
        String branch = getIntent().getStringExtra("branch");
        String location = getIntent().getStringExtra("location");
        String path = getIntent().getStringExtra("path");
        mbase = FirebaseDatabase.getInstance().getReference(path);

        branchTV.setText("Branch : " + branch);
        locationTV.setText("Location : " + location);

        mbase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get containers from AI.
                for (DataSnapshot year : snapshot.getChildren()) {
                    // Year
                    for (DataSnapshot month : year.getChildren()) {
                        // Month
                        for (DataSnapshot day : month.getChildren()) {
                            // Day
                            for (DataSnapshot contSnapshot : day.getChildren()) {
                                // Container
                                // Check if particular container has status as AI.
                                if (contSnapshot.child("Status").getValue().toString().contentEquals("AI")) {
                                    // Add container to list.
                                    aiUnits.add(contSnapshot.getKey());
                                }
                            }
                        }
                    }
                }
                // Set fetched containers to recycler view.
                Collections.sort(aiUnits);
                RecyclerView recyclerView = findViewById(R.id.recycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                RecyclerView.Adapter aiAdapter = new AIAdapter(aiUnits, path);
                recyclerView.setAdapter(aiAdapter);
                hideLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    private void showLoading() {
        // Show loading screen.
        if (mLoading != null) {
            mLoading.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        // Hide loading screen.
        if (mLoading != null) {
            mLoading.setVisibility(View.GONE);
        }
    }
}