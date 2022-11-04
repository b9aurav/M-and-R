package com.cgsoft.mnr.Mies;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cgsoft.mnr.Common.FindActivity;
import com.cgsoft.mnr.Common.MainActivity;
import com.cgsoft.mnr.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

// M.I.S. Fragment Back-end.

public class MiesFragment extends Fragment {

    // Global Variables.
    Spinner yearSp, branchSp, lineSp;
    int totalUnits;
    double totalAmount;
    TextView totalUnitsTV, totalAmountTV;
    TableLayout tableLayout;
    FirebaseDatabase firebaseDatabase;
    EditText searchET;
    ImageButton searchBtn;
    boolean oddRow = true;
    private View mLoading;

    public MiesFragment() {
        // Required empty public constructor
    }

    // Pre-generated code.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mies, container, false);

        // Initialize variables.
        firebaseDatabase = FirebaseDatabase.getInstance();
        mLoading = v.findViewById(R.id.prgsbar);
        searchET = v.findViewById(R.id.searchET);
        searchBtn = v.findViewById(R.id.searchImgBtn);
        yearSp = v.findViewById(R.id.yearSp);
        branchSp = v.findViewById(R.id.filterBranchSp);
        lineSp = v.findViewById(R.id.filterLineSp);
        totalUnitsTV = v.findViewById(R.id.totalUnits);
        totalAmountTV = v.findViewById(R.id.totalBilling);
        tableLayout = (TableLayout) v.findViewById(R.id.tableLayout);

        // Prevent touches on loading screen.
        mLoading.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

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

        String initBranch = "";
        if (!MainActivity.userBranch.contentEquals("")) {
            // If user is not an Owner.
            // Setting branch to spinner.
            initBranch = MainActivity.userBranch;
            branchSp.setEnabled(false);
            final ArrayList<String> branches = new ArrayList<String>();
            branches.add(initBranch);
            ArrayAdapter<String> branchAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, branches);
            branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            branchSp.setAdapter(branchAdapter);
            // Setting other spinners according to the branch.
            setFilter(initBranch);
        } else {
            // If user is an Owner.
            DatabaseReference branchRef = firebaseDatabase.getReference("Data/");
            branchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Fetch all branches from Database and set to the branch spinner.
                    final ArrayList<String> branches = new ArrayList<String>();
                    branches.add("All");
                    for (DataSnapshot branchSnapshot : snapshot.getChildren()) {
                        String branch = branchSnapshot.getKey();
                        branches.add(branch);
                    }
                    ArrayAdapter<String> branchAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, branches);
                    branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    branchSp.setAdapter(branchAdapter);
                    // Setting other spinners.
                    setFilter("");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }

        branchSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (MainActivity.userBranch.contentEquals("")) {
                    // Setting other spinners.
                    setFilter(branchSp.getSelectedItem().toString());
                    // Filter data according to selected branch and line.
                    filterData(branchSp.getSelectedItem().toString(), lineSp.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        lineSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Filter data according to selected branch and line.
                filterData(branchSp.getSelectedItem().toString(), lineSp.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        yearSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Filter data according to selected branch and line (Year will be fetched by method).
                filterData(MainActivity.userBranch, lineSp.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Fetching years from database.
        // Years must be of Repair date of containers.
        DatabaseReference yearRef = firebaseDatabase.getReference("Data/");
        yearRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final ArrayList<Integer> years = new ArrayList<Integer>();
                ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<Integer>(getContext(), android.R.layout.simple_spinner_item, years);
                for (DataSnapshot branchSnapshot : snapshot.getChildren()) {
                    // Branch
                    for (DataSnapshot locSnapshot : branchSnapshot.getChildren()) {
                        // Location
                        for (DataSnapshot lineSnapshot : locSnapshot.getChildren()) {
                            // Line
                            for (DataSnapshot yearSnapshot : lineSnapshot.getChildren()) {
                                // Year
                                for (DataSnapshot monthSnapshot : yearSnapshot.getChildren()) {
                                    // Month
                                    for (DataSnapshot daySnapshot : monthSnapshot.getChildren()) {
                                        // Day
                                        for (DataSnapshot contSnapshot : daySnapshot.getChildren()) {
                                            // Container
                                            if (!contSnapshot.child("RepairDate").getValue().toString().contentEquals("")) {
                                                // Split year from Repair date.
                                                String[] repairDate = contSnapshot.child("RepairDate").getValue().toString().split("/");
                                                int year = Integer.parseInt(repairDate[2]);
                                                // Check if the year is already in list.
                                                boolean flag = false;
                                                for (int i = 0; i < years.size(); i++) {
                                                    if (years.get(i) == year) {
                                                        flag = true;
                                                    }
                                                }
                                                // Add year to the list.
                                                if (!flag) {
                                                    years.add(year);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            // Set year list to spinner.
                            yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            yearSp.setAdapter(yearAdapter);
                        }
                    }
                }
                // Select latest year
                yearSp.setSelection(years.indexOf(Collections.max(years)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        return v;
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

    void setFilter(String fbranch) {
        DatabaseReference lineRef = firebaseDatabase.getReference("Data/");
        lineRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final ArrayList<String> lines = new ArrayList<String>();
                boolean branchNotFiltered = fbranch.contentEquals("All") || fbranch.contentEquals("");

                lines.add("All");

                for (DataSnapshot filterBranchSnapshot : snapshot.getChildren()) {
                    if (branchNotFiltered || fbranch.contentEquals(filterBranchSnapshot.getKey())) {
                        // Filtered branch OR Filter is not applied.
                        for (DataSnapshot filterLocSnapshot : filterBranchSnapshot.getChildren()) {
                            // Location
                            for (DataSnapshot lineSnapshot : filterLocSnapshot.getChildren()) {
                                // Line
                                String line = lineSnapshot.getKey();
                                boolean flag = true;
                                // Check if the line is already in list.
                                for (int i = 0; i < lines.size(); i++) {
                                    if (lines.get(i).contentEquals(line)) {
                                        flag = false;
                                    }
                                }
                                // Add line to the list.
                                if (flag) {
                                    lines.add(line);
                                }
                            }
                            // Set line list to spinner.
                            ArrayAdapter<String> lineAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, lines);
                            lineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            lineSp.setAdapter(lineAdapter);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    void filterData(String fbranch, String fline) {
        boolean branchNotFiltered = fbranch.contentEquals("") || fbranch.contentEquals("All");
        boolean lineNotFiltered = fline.contentEquals("") || fline.contentEquals("All");
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        showLoading();

        DatabaseReference filtRef = firebaseDatabase.getReference("Data/");
        filtRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Fetch selected year from spinner.
                String fyear = yearSp.getSelectedItem().toString();
                if (!dataSnapshot.exists()) {
                    // No data available for selected year.
                    Toast.makeText(getContext(), "Nothing to show!", Toast.LENGTH_SHORT).show();
                    hideLoading();
                    return;
                }

                // Initialize OR Reset Variables.
                totalUnits = 0;
                totalAmount = 0;
                totalAmountTV.setText("Amount : " + totalAmount);
                totalUnitsTV.setText("Units : " + totalUnits);
                while (tableLayout.getChildCount() > 1) {
                    // Remove rows from table.
                    tableLayout.removeView(tableLayout.getChildAt(tableLayout.getChildCount() - 1));
                }

                for (int i = 0; i <= months.length - 1; i++) {
                    // 12 Iteration for months.
                    TableRow tr = new TableRow(getContext());
                    TextView monthTV = new TextView(getContext());
                    // Set month to 1st column of table.
                    monthTV.setText(months[i]);
                    monthTV.setPadding(5, 30, 5, 30);
                    ViewGroup.MarginLayoutParams monthParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                    monthParams.setMargins(0, 0, 5, 0);
                    monthTV.setLayoutParams(monthParams);
                    monthTV.setGravity(Gravity.CENTER_HORIZONTAL);

                    // Set units to 2nd column of table.
                    TextView unitsTV = new TextView(getContext());
                    unitsTV.setText("0");
                    unitsTV.setPadding(5, 30, 5, 30);
                    unitsTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                    unitsTV.setGravity(Gravity.CENTER_HORIZONTAL);

                    // Set billing amount to 3rd column of table.
                    TextView amtTV = new TextView(getContext());
                    amtTV.setText("0");
                    amtTV.setPadding(5, 30, 5, 30);
                    amtTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                    ViewGroup.MarginLayoutParams amtParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                    amtParams.setMargins(0, 0, 5, 0);
                    amtTV.setLayoutParams(amtParams);
                    amtTV.setGravity(Gravity.CENTER_HORIZONTAL);

                    for (DataSnapshot b : dataSnapshot.getChildren()) {
                        if (branchNotFiltered || fbranch.contentEquals(b.getKey())) {
                            // Filtered branch OR Filter is not applied.
                            String branch = b.getKey();
                            for (DataSnapshot l : b.getChildren()) {
                                // Location
                                String location = l.getKey();
                                for (DataSnapshot ln : l.getChildren()) {
                                    if (lineNotFiltered || fline.contentEquals(ln.getKey())) {
                                        // Filtered line OR Filter not applied.
                                        for (DataSnapshot year : ln.getChildren()) {
                                            // Year
                                            for (DataSnapshot month : year.getChildren()) {
                                                // Month
                                                for (DataSnapshot day : month.getChildren()) {
                                                    // Day
                                                    for (DataSnapshot contSnapshot : day.getChildren()) {
                                                        // Container
                                                        int newUnit = Integer.parseInt(unitsTV.getText().toString());
                                                        double newAmt = Double.parseDouble(amtTV.getText().toString());
                                                        // Check if container has repaired or not.
                                                        if (!contSnapshot.child("RepairDate").getValue().toString().contentEquals("")) {
                                                            // Get repair date.
                                                            String[] repairDate = contSnapshot.child("RepairDate").getValue().toString().split("/");
                                                            // Check if filter year matches repair year.
                                                            if (Integer.parseInt(repairDate[2]) == Integer.parseInt(fyear)) {
                                                                // Check if month matches current iteration.
                                                                if (Integer.parseInt(repairDate[1]) == i + 1) {
                                                                    // Increment counts.
                                                                    newUnit++;
                                                                    totalUnits++;
                                                                    totalUnitsTV.setText("Units : " + totalUnits);
                                                                    unitsTV.setText(String.valueOf(newUnit));
                                                                    if (!contSnapshot.child("TotalCost").getValue().toString().contentEquals("")) {
                                                                        // Append total cost to total amount.
                                                                        newAmt += Double.parseDouble(contSnapshot.child("TotalCost").getValue().toString());
                                                                        totalAmount += Double.parseDouble(contSnapshot.child("TotalCost").getValue().toString());
                                                                        amtTV.setText(String.format("%.0f", newAmt));
                                                                        totalAmountTV.setText("Amount : " + String.format("%.0f", totalAmount));
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // Add cells to table.
                    tr.addView(monthTV);
                    tr.addView(unitsTV);
                    tr.addView(amtTV);
                    // Alternating background color of rows.
                    if (Integer.parseInt(unitsTV.getText().toString()) != 0) {
                        tableLayout.addView(tr);
                        if (oddRow) {
                            tr.setBackgroundColor(getResources().getColor(R.color.white));
                            oddRow = false;
                        } else {
                            tr.setBackgroundColor(getResources().getColor(R.color.orangeAlpha));
                            oddRow = true;
                        }
                    }
                }
                hideLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
}