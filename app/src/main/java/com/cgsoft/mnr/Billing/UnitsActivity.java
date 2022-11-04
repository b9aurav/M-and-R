package com.cgsoft.mnr.Billing;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

// Units Screen Back-end.

public class UnitsActivity extends AppCompatActivity {

    // Global Variables.
    ArrayList<String> avUnits;
    DatabaseReference mbase;
    LinearLayout view;
    TextView branchTV, locationTV;
    private View mLoading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);

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
        showLoading();
        avUnits = new ArrayList<String>();

        // Fetch variables from Intent.
        String branch = getIntent().getStringExtra("branch");
        String location = getIntent().getStringExtra("location");
        String line = getIntent().getStringExtra("line");
        String path = getIntent().getStringExtra("path");
        String fromDate = getIntent().getStringExtra("fromDate");
        String toDate = getIntent().getStringExtra("toDate");

        mbase = FirebaseDatabase.getInstance().getReference(path);

        branchTV.setText("Branch : " + branch);
        locationTV.setText("Location : " + location);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        Date from;
        Date to;
        try {
            // Adjust date format.
            from = dateFormat.parse(fromDate);
            to = dateFormat.parse(toDate);
            mbase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot year : snapshot.getChildren()) {
                        // Year
                        for (DataSnapshot month : year.getChildren()) {
                            // Month
                            for (DataSnapshot day : month.getChildren()) {
                                // Day
                                for (DataSnapshot contSnapshot : day.getChildren()) {
                                    // Container
                                    String repairDate = contSnapshot.child("RepairDate").getValue().toString();
                                    try {
                                        // Check if repair date is between selected two dates.
                                        Date rDate;
                                        rDate = dateFormat.parse(repairDate);
                                        if (rDate.compareTo(from) >= 0 && rDate.compareTo(to) <= 0) {
                                            // Add unit to the list.
                                            avUnits.add(contSnapshot.getKey());
                                        }
                                    } catch (Exception e) {}
                                }
                            }
                        }
                    }
                    // Show containers to recycler view from list.
                    Collections.sort(avUnits);
                    RecyclerView recyclerView = findViewById(R.id.recycler);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    RecyclerView.Adapter avAdapter = new UnitsAdapter(avUnits, path);
                    recyclerView.setAdapter(avAdapter);
                    hideLoading();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) { }
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