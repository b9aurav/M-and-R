package com.cgsoft.mnr.Billing;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cgsoft.mnr.Common.FindActivity;
import com.cgsoft.mnr.Common.MainActivity;
import com.cgsoft.mnr.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

// Billing Fragment Back-end.

public class BillingFragment extends Fragment {

    // Global Variables.
    public MaterialButton fromDate, toDate;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    int totalUnits = 0;
    float totalAmt = 0;
    private View mLoading;
    TextView totalUnitsTV, totalAmtTV;
    TableLayout tl;
    EditText searchET;
    ImageButton searchBtn;
    boolean oddRow = true;

    public BillingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Data/");
        View v = inflater.inflate(R.layout.fragment_billing, container, false);

        // Initialize variables.
        String today = new SimpleDateFormat("dd/MM/yy").format(Calendar.getInstance().getTime());
        String[] fromDateStr = today.split("/");
        String[] toDateStr = today.split("/");
        String firstDayOfMonth = "1" + "/" + fromDateStr[1] + "/" + fromDateStr[2];
        fromDate = v.findViewById(R.id.fromDate);
        toDate = v.findViewById(R.id.toDate);
        fromDate.setText(firstDayOfMonth);
        toDate.setText(today);
        totalUnitsTV = v.findViewById(R.id.totalUnits);
        totalAmtTV = v.findViewById(R.id.totalAmt);

        searchET = v.findViewById(R.id.searchET);
        searchBtn = v.findViewById(R.id.searchImgBtn);
        searchET.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Setting 'Enter' key to submit search query.
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (!searchET.getText().toString().contentEquals("")) {
                        searchBtn.performClick();
                    }
                }
                return false;
            }
        });
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchET.getText().toString().contentEquals("")) {
                    // Switching Main Screen to Search Screen.
                    Intent i = new Intent(getContext(), FindActivity.class);
                    i.putExtra("container", searchET.getText().toString());
                    startActivity(i);
                    searchET.setText("");
                }
            }
        });

        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show date picker dialog.
                getActivity().showDialog(999);
            }
        });
        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show date picker dialog.
                getActivity().showDialog(990);
            }
        });

        mLoading = v.findViewById(R.id.prgsbar);
        // Prevent touches on loading screen.
        mLoading.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        tl = (TableLayout) v.findViewById(R.id.tableLayout);
        // Filter data according to the dates.
        filterData(fromDate.getText().toString(), toDate.getText().toString());
        return v;
    }

    void filterData(String fromDate, String toDate) {
        showLoading();
        totalAmt = 0;
        totalUnits = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        Date from;
        Date to;
        try {
            from = dateFormat.parse(fromDate);
            to = dateFormat.parse(toDate);
            DatabaseReference recordRef = firebaseDatabase.getReference("Data/");
            recordRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // If data is not available.
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Nothing to show!", Toast.LENGTH_SHORT).show();
                        hideLoading();
                        return;
                    }

                    // Reset variables.
                    totalUnits = 0;
                    totalAmt = 0;

                    // Reset table.
                    while (tl.getChildCount() > 1) {
                        tl.removeView(tl.getChildAt(tl.getChildCount() - 1));
                    }

                    boolean fixedBranch = !MainActivity.userBranch.contentEquals("");
                    for (DataSnapshot b : snapshot.getChildren()) {
                        if (b.getKey().contentEquals(MainActivity.userBranch) || !fixedBranch) {
                            // Filtered branch OR All branches (Depends on user).
                            String branch = b.getKey();
                            for (DataSnapshot l : b.getChildren()) {
                                // Location
                                String location = l.getKey();
                                for (DataSnapshot ln : l.getChildren()) {
                                    // Line
                                    String line = ln.getKey();

                                    // Generate view for units.
                                    TextView unitsTV = new TextView(getContext());
                                    unitsTV.setText("0");
                                    unitsTV.setPadding(5, 30, 5, 30);
                                    unitsTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                                    unitsTV.setGravity(Gravity.CENTER_HORIZONTAL);

                                    // Generate view for amount.
                                    TextView amtTV = new TextView(getContext());
                                    amtTV.setText("0");
                                    amtTV.setPadding(5, 30, 5, 30);
                                    ViewGroup.MarginLayoutParams invParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                                    invParams.setMargins(0, 0, 5, 0);
                                    amtTV.setLayoutParams(invParams);
                                    amtTV.setGravity(Gravity.CENTER_HORIZONTAL);
                                    for (DataSnapshot year : ln.getChildren()) {
                                        // Year
                                        for (DataSnapshot month : year.getChildren()) {
                                            // Month
                                            for (DataSnapshot day : month.getChildren()) {
                                                // Day
                                                for (DataSnapshot contSnapshot : day.getChildren()) {
                                                    // Container
                                                    // Get counted container variables.
                                                    int newUnit = Integer.parseInt(unitsTV.getText().toString());
                                                    float newAmt = Float.parseFloat(amtTV.getText().toString());
                                                    // Check if container has repaired.
                                                    if (!contSnapshot.child("RepairDate").getValue().toString().contentEquals("")) {
                                                        String repairDate = contSnapshot.child("RepairDate").getValue().toString();
                                                        try {
                                                            Date rDate;
                                                            rDate = dateFormat.parse(repairDate);
                                                            // Check if repair date is between selected two dates.
                                                            if (rDate.compareTo(from) >= 0 && rDate.compareTo(to) <= 0) {
                                                                // Increment count.
                                                                newUnit++;
                                                                // Display counts on generated view.
                                                                unitsTV.setText(String.valueOf(newUnit));
                                                                unitsTV.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        // Switch screen from current fragment to AI Screen.
                                                                        Intent i = new Intent(getContext(), UnitsActivity.class);
                                                                        i.putExtra("fromDate", fromDate);
                                                                        i.putExtra("toDate", toDate);
                                                                        i.putExtra("branch", branch);
                                                                        i.putExtra("location", location);
                                                                        i.putExtra("line", line);
                                                                        i.putExtra("path", "Data/" + branch + "/" + location + "/" + line);
                                                                        startActivity(i);
                                                                    }
                                                                });
                                                                // Add cost to total amount and display in generated view.
                                                                if (!contSnapshot.child("TotalCost").getValue().toString().contentEquals("")) {
                                                                    newAmt += Float.parseFloat(contSnapshot.child("TotalCost").getValue().toString());
                                                                    amtTV.setText(String.valueOf(newAmt));
                                                                    hideLoading();
                                                                }
                                                            }
                                                        } catch (Exception e) {
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Generate new row in table.
                                    TableRow tr = new TableRow(getContext());
                                    tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                                    // Generate view for branch.
                                    TextView branchTV = new TextView(getContext());
                                    branchTV.setText(branch);
                                    branchTV.setPadding(5, 30, 5, 30);
                                    branchTV.setGravity(Gravity.CENTER_HORIZONTAL);
                                    ViewGroup.MarginLayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                                    params.setMargins(5, 0, 0, 0);
                                    branchTV.setLayoutParams(params);

                                    // Generate view for location.
                                    TextView locationTV = new TextView(getContext());
                                    locationTV.setText(location);
                                    locationTV.setPadding(5, 30, 5, 30);
                                    locationTV.setGravity(Gravity.CENTER_HORIZONTAL);
                                    locationTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                                    // Generate view for line.
                                    TextView lineTV = new TextView(getContext());
                                    lineTV.setText(line);
                                    lineTV.setGravity(Gravity.CENTER_HORIZONTAL);
                                    lineTV.setPadding(5, 30, 5, 30);
                                    lineTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                                    // Add views in row.
                                    tr.addView(branchTV);
                                    tr.addView(locationTV);
                                    tr.addView(lineTV);
                                    tr.addView(unitsTV);
                                    tr.addView(amtTV);

                                    // count fetched units.
                                    totalUnits += Integer.parseInt(unitsTV.getText().toString());

                                    // Calculate total amount and display.
                                    totalAmt += Float.parseFloat(amtTV.getText().toString());
                                    totalAmtTV.setText("Amount : " + totalAmt);
                                    totalUnitsTV.setText("Units : " + totalUnits);

                                    // Alternate color of rows.
                                    if (Integer.parseInt(unitsTV.getText().toString()) != 0) {
                                        tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                                        if (oddRow) {
                                            tr.setBackgroundColor(getResources().getColor(R.color.white));
                                            oddRow = false;
                                        } else {
                                            tr.setBackgroundColor(getResources().getColor(R.color.greenAlpha));
                                            oddRow = true;
                                        }
                                    }
                                    hideLoading();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
        }
    }

    public void showFromDate(int year, int month, int day, String name) {
        // Get date from date picker and show on view.
        // Filter data according to dates.
        if (name.contentEquals("fromDate")) {
            fromDate.setText(new StringBuilder().append(day).append("/").append(month).append("/").append(year));
            filterData(fromDate.getText().toString(), toDate.getText().toString());
        } else {
            toDate.setText(new StringBuilder().append(day).append("/").append(month).append("/").append(year));
            filterData(fromDate.getText().toString(), toDate.getText().toString());
        }
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