package com.cgsoft.mnr.CurrentStatus;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

// Current Status Fragment Back-end.

public class CurrentFragment extends Fragment {

    // Global Variables.
    int totalAI = 0;
    int totalAR = 0;
    int totalAP = 0;
    int totalINV = 0;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    Spinner branchSpinner;
    Spinner lineSpinner;
    Spinner locSpinner;

    TextView totalAItv;
    TextView totalARtv;
    TextView totalAPtv;
    TextView totalINVtv;
    TableLayout tl;
    EditText searchET;
    ImageButton searchBtn;
    boolean oddRow = true;
    private View mLoading;

    public CurrentFragment() {
        // Required empty public constructor
    }

    // Pre-generated code.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Data/");

        // Initialize variables.
        View v = inflater.inflate(R.layout.fragment_current, container, false);
        branchSpinner = v.findViewById(R.id.filterBranchSp);
        lineSpinner = v.findViewById(R.id.filterLineSp);
        locSpinner = v.findViewById(R.id.filterLocSp);
        totalAItv = v.findViewById(R.id.totalAI);
        totalARtv = v.findViewById(R.id.totalAR);
        totalAPtv = v.findViewById(R.id.totalAP);
        totalINVtv = v.findViewById(R.id.totalINV);
        searchET = v.findViewById(R.id.searchET);
        searchBtn = v.findViewById(R.id.searchImgBtn);
        locSpinner.setEnabled(false);

        String initBranch = "";
        if (!MainActivity.userBranch.contentEquals("")) {
            // If user is not an owner.
            // Setting branch to spinner.
            initBranch = MainActivity.userBranch;
            branchSpinner.setEnabled(false);
            final ArrayList<String> branches = new ArrayList<String>();
            branches.add(MainActivity.userBranch);
            ArrayAdapter<String> branchAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, branches);
            branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            branchSpinner.setAdapter(branchAdapter);
            // Setting other spinners according to the branch.
            setFilter(initBranch, "");
        } else {
            // If user is an Owner.
            DatabaseReference branchRef = firebaseDatabase.getReference("Data/");
            branchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Fetch all branches from Database and set to the branch spinner.
                    final ArrayList<String> branches = new ArrayList<String>();
                    branches.add("No filter");
                    for (DataSnapshot branchSnapshot : snapshot.getChildren()) {
                        String branch = branchSnapshot.getKey();
                        branches.add(branch);
                    }
                    ArrayAdapter<String> branchAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, branches);
                    branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    branchSpinner.setAdapter(branchAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

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
        branchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!branchSpinner.getSelectedItem().toString().contentEquals("No filter")) {
                    // If branch filter is applied.
                    // Load location spinner with all locations of particular branch.
                    locSpinner.setEnabled(true);
                    databaseReference = firebaseDatabase.getReference("Data/" + branchSpinner.getSelectedItem().toString());
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // Fetch locations.
                            final ArrayList<String> locations = new ArrayList<String>();
                            locations.add("No filter");
                            for (DataSnapshot locSnapshot : snapshot.getChildren()) {
                                // Add location to list.
                                String location = locSnapshot.getKey();
                                locations.add(location);
                            }
                            ArrayAdapter<String> locAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, locations);
                            locAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            // Attach list to spinner.
                            locSpinner.setAdapter(locAdapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    // Setting other spinners.
                    setFilter(branchSpinner.getSelectedItem().toString(), "");
                    // Filter data according to selected branch.
                    filterData(branchSpinner.getSelectedItem().toString(), "", "");
                } else {
                    // If branch filter is not applied.
                    locSpinner.setEnabled(false);
                    // Set other spinners.
                    setFilter("", "");
                    // Remove Filters.
                    filterData("", "", "");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        locSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!locSpinner.getSelectedItem().toString().contentEquals("No filter")) {
                    // If location is filtered.
                    // Set other spinners.
                    setFilter(branchSpinner.getSelectedItem().toString(), locSpinner.getSelectedItem().toString());
                    // Filter data according to selected branch and location.
                    filterData(branchSpinner.getSelectedItem().toString(), locSpinner.getSelectedItem().toString(), "");
                } else {
                    // If location filter is not applied.
                    // Set other spinners.
                    setFilter(branchSpinner.getSelectedItem().toString(), "");
                    // Remove location filters.
                    filterData(branchSpinner.getSelectedItem().toString(), "", "");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        lineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!lineSpinner.getSelectedItem().toString().contentEquals("No filter")) {
                    // If line is filtered.
                    String loc = "";
                    if (locSpinner.isEnabled()) {
                        loc = locSpinner.getSelectedItem().toString();
                    }
                    // Filter data according to selected branch, location and line.
                    filterData(branchSpinner.getSelectedItem().toString(), loc, lineSpinner.getSelectedItem().toString());
                } else {
                    // If line is not filtered.
                    String loc = "";
                    if (locSpinner.isEnabled()) {
                        loc = locSpinner.getSelectedItem().toString();
                    }
                    // Filter data according to branch and location.
                    filterData(branchSpinner.getSelectedItem().toString(), loc, "");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mLoading = v.findViewById(R.id.prgsbar);
        // Prevent touches on loading screen
        mLoading.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        tl = (TableLayout) v.findViewById(R.id.tableLayout);
        return v;
    }

    void filterData(String fbranch, String flocation, String fline) {
        // Check if filter is applied.
        boolean branchNotFiltered = fbranch.contentEquals("") || fbranch.contentEquals("No filter");
        boolean locationNotFiltered = flocation.contentEquals("") || flocation.contentEquals("No filter");
        boolean lineNotFiltered = fline.contentEquals("");
        showLoading();
        DatabaseReference filtRef = firebaseDatabase.getReference("Data/");
        filtRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // If data is not available.
                    Toast.makeText(getContext(), "Nothing to show!", Toast.LENGTH_SHORT).show();
                    hideLoading();
                    return;
                }

                // Reset variables.
                totalAI = 0;
                totalAP = 0;
                totalAR = 0;

                // Reset table.
                while (tl.getChildCount() > 1) {
                    tl.removeView(tl.getChildAt(tl.getChildCount() - 1));
                }

                for (DataSnapshot b : dataSnapshot.getChildren()) {
                    if (branchNotFiltered || fbranch.contentEquals(b.getKey())) {
                        // Filtered branch OR Filter is not applied.
                        String branch = b.getKey();
                        for (DataSnapshot l : b.getChildren()) {
                            if (locationNotFiltered || flocation.contentEquals(l.getKey())) {
                                // Filtered location OR Filter is not applied.
                                String location = l.getKey();
                                for (DataSnapshot ln : l.getChildren()) {
                                    if (lineNotFiltered || fline.contentEquals(ln.getKey())) {
                                        // Filtered line OR Filter is not applied.
                                        String line = ln.getKey();

                                        // Generate view for AI Units in table
                                        TextView aiTV = new TextView(getContext());
                                        aiTV.setText("0");
                                        aiTV.setPadding(5, 30, 5, 30);
                                        aiTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                                        aiTV.setGravity(Gravity.CENTER_HORIZONTAL);

                                        // Generate view for AP Units in table
                                        TextView apTV = new TextView(getContext());
                                        apTV.setText("0");
                                        apTV.setPadding(5, 30, 5, 30);
                                        apTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                                        apTV.setGravity(Gravity.CENTER_HORIZONTAL);

                                        // Generate view for AR Units in table
                                        TextView arTV = new TextView(getContext());
                                        arTV.setText("0");
                                        arTV.setPadding(5, 30, 5, 30);
                                        arTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                                        arTV.setGravity(Gravity.CENTER_HORIZONTAL);

                                        // Generate view for Inventory Units in table
                                        TextView invTV = new TextView(getContext());
                                        invTV.setText("0");
                                        invTV.setPadding(5, 30, 5, 30);
                                        ViewGroup.MarginLayoutParams invParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                                        invParams.setMargins(0, 0, 5, 0);
                                        invTV.setLayoutParams(invParams);
                                        invTV.setGravity(Gravity.CENTER_HORIZONTAL);

                                        for (DataSnapshot year : ln.getChildren()) {
                                            // Year
                                            for (DataSnapshot month : year.getChildren()) {
                                                // Month
                                                for (DataSnapshot day : month.getChildren()) {
                                                    // Day
                                                    for (DataSnapshot contSnapshot : day.getChildren()) {
                                                        // Container
                                                        // Get counted container variables.
                                                        int newAI = Integer.parseInt(aiTV.getText().toString());
                                                        int newAP = Integer.parseInt(apTV.getText().toString());
                                                        int newAR = Integer.parseInt(arTV.getText().toString());

                                                        // Fetch category of current container and increment the count.
                                                        if (contSnapshot.child("Status").getValue().toString().contentEquals("AI")) {
                                                            newAI++;
                                                            totalAI++;
                                                        } else if (contSnapshot.child("Status").getValue().toString().contentEquals("AP")) {
                                                            newAP++;
                                                            totalAP++;
                                                        } else if (contSnapshot.child("Status").getValue().toString().contentEquals("AR")) {
                                                            newAR++;
                                                            totalAR++;
                                                        }

                                                        // Calculate inventory.
                                                        int inventory = newAI + newAP + newAR;

                                                        // Display counts to view.
                                                        aiTV.setText(String.valueOf(newAI));
                                                        apTV.setText(String.valueOf(newAP));
                                                        arTV.setText(String.valueOf(newAR));
                                                        invTV.setText(String.valueOf(inventory));

                                                        aiTV.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                // Switch screen from current fragment to AI Screen.
                                                                Intent i = new Intent(getContext(), AIActivity.class);
                                                                i.putExtra("status", "AI");
                                                                i.putExtra("branch", branch);
                                                                i.putExtra("location", location);
                                                                i.putExtra("line", line);
                                                                i.putExtra("path", "Data/" + branch + "/" + location + "/" + line);
                                                                startActivity(i);
                                                            }
                                                        });

                                                        apTV.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                // Switch screen from current fragment to AP Screen.
                                                                Intent i = new Intent(getContext(), APActivity.class);
                                                                i.putExtra("status", "AP");
                                                                i.putExtra("branch", branch);
                                                                i.putExtra("location", location);
                                                                i.putExtra("line", line);
                                                                i.putExtra("path", "Data/" + branch + "/" + location + "/" + line);
                                                                startActivity(i);
                                                            }
                                                        });

                                                        arTV.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                // // Switch screen from current fragment to AR Screen.
                                                                Intent i = new Intent(getContext(), ARActivity.class);
                                                                i.putExtra("status", "AR");
                                                                i.putExtra("branch", branch);
                                                                i.putExtra("location", location);
                                                                i.putExtra("line", line);
                                                                i.putExtra("path", "Data/" + branch + "/" + location + "/" + line);
                                                                startActivity(i);
                                                            }
                                                        });

                                                        invTV.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                // Switch screen from current fragment to Inventory Screen.
                                                                Intent i = new Intent(getContext(), InventoryActivity.class);
                                                                i.putExtra("branch", branch);
                                                                i.putExtra("location", location);
                                                                i.putExtra("path", "Data/" + branch + "/" + location + "/" + line);
                                                                startActivity(i);
                                                            }
                                                        });

                                                        // Display sub-total in View.
                                                        totalINV = totalAI + totalAR + totalAP;
                                                        totalAItv.setText("AI : " + totalAI);
                                                        totalARtv.setText("AR : " + totalAR);
                                                        totalAPtv.setText("AP : " + totalAP);
                                                        totalINVtv.setText("Inventory : " + totalINV);
                                                        hideLoading();
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

                                        // Add views to row.
                                        tr.addView(branchTV);
                                        tr.addView(locationTV);
                                        tr.addView(lineTV);
                                        tr.addView(aiTV);
                                        tr.addView(apTV);
                                        tr.addView(arTV);
                                        tr.addView(invTV);

                                        // Alternating background color of rows.
                                        if (Integer.parseInt(invTV.getText().toString()) != 0) {
                                            tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                                            if (oddRow) {
                                                tr.setBackgroundColor(getResources().getColor(R.color.white));
                                                oddRow = false;
                                            } else {
                                                tr.setBackgroundColor(getResources().getColor(R.color.skylApha));
                                                oddRow = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void setFilter(String fbranch, String flocation) {
        DatabaseReference lineRef = firebaseDatabase.getReference("Data/");
        lineRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final ArrayList<String> lines = new ArrayList<String>();
                // Check if filtered is applied.
                boolean branchNotFiltered = fbranch.contentEquals("No filter") || fbranch.contentEquals("");
                boolean locationNotFiltered = flocation.contentEquals("No filter") || flocation.contentEquals("");

                lines.add("No filter");

                for (DataSnapshot filterBranchSnapshot : snapshot.getChildren()) {
                    if (branchNotFiltered || fbranch.contentEquals(filterBranchSnapshot.getKey())) {
                        // Filtered branch OR Filter is not applied.
                        for (DataSnapshot filterLocSnapshot : filterBranchSnapshot.getChildren()) {
                            if (locationNotFiltered || flocation.contentEquals(filterBranchSnapshot.getKey())) {
                                // Filtered location OR Filter is not applied.
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
                                lineSpinner.setAdapter(lineAdapter);
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