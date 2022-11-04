package com.cgsoft.mnr.Common;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgsoft.mnr.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

// Container Details Screen Back-end.

public class FindActivity extends AppCompatActivity {

    // Global Variables.
    SearchAdapter adapter;
    DatabaseReference mbase;
    FirebaseRecyclerOptions<ContainerDetails> options;
    LinearLayout view;
    int recyclerID = 1;
    public static View mLoading;
    public static TextView msg, records;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);

        // Initialize Variables.
        SearchAdapter.count = 0;
        view = findViewById(R.id.mainLayout);
        String container = getIntent().getStringExtra("container");
        mbase = FirebaseDatabase.getInstance().getReference("Data");
        msg = findViewById(R.id.msg);
        msg.setText("Results for Container : " + container);
        records = findViewById(R.id.recordCount);
        mLoading = findViewById(R.id.prgsbar);

        // Prevent touches in loading screen.
        mLoading.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        showLoading();

        mbase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot b : snapshot.getChildren()) {
                    // Branch
                    String branch = b.getKey();
                    for (DataSnapshot l : b.getChildren()) {
                        // Location
                        String location = l.getKey();
                        for (DataSnapshot ln : l.getChildren()) {
                            // Line
                            String line = ln.getKey();
                            for (DataSnapshot y : ln.getChildren()) {
                                // Year
                                String year = y.getKey();
                                for (DataSnapshot m : y.getChildren()) {
                                    // Month
                                    String month = m.getKey();
                                    for (DataSnapshot d : m.getChildren()) {
                                        // Day
                                        String day = d.getKey();
                                        DatabaseReference dayRef  = FirebaseDatabase.getInstance().getReference("Data/" + branch + "/" + location + "/" + line + "/" + year + "/" + month + "/" + day);
                                            // Generate recycler view.
                                            RecyclerView recyclerView = new RecyclerView(getApplicationContext());
                                            // Adjust recycler view.
                                            recyclerView.setId(recyclerID);
                                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                            // Add recycler view to main view.
                                            view.addView(recyclerView);
                                            // Increment ID.
                                            recyclerID++;
                                            // Query to find container.
                                            Query q = dayRef.orderByChild("Container").equalTo(container);
                                            q.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                    if (task.isComplete()) {
                                                        if (SearchAdapter.count == 0) {
                                                            // No records found.
                                                            records.setText("No records found!");
                                                        }
                                                        hideLoading();
                                                    }
                                                }
                                            });
                                            // Fill fetched details in recycler view.
                                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                            options = new FirebaseRecyclerOptions.Builder<ContainerDetails>()
                                                    .setQuery(q, ContainerDetails.class)
                                                    .build();
                                            adapter = new SearchAdapter(options);
                                            adapter.startListening();
                                            recyclerView.setAdapter(adapter);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public static void showLoading() {
        // Show loading screen.
        if (mLoading != null) {
            mLoading.setVisibility(View.VISIBLE);
        }
    }

    public static void hideLoading() {
        // Hide loading screen.
        if (mLoading != null) {
            mLoading.setVisibility(View.GONE);
        }
    }
}