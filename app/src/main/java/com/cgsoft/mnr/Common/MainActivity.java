package com.cgsoft.mnr.Common;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.cgsoft.mnr.Billing.BillingFragment;
import com.cgsoft.mnr.CurrentStatus.CurrentFragment;
import com.cgsoft.mnr.Mies.MiesFragment;
import com.cgsoft.mnr.R;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.text.SimpleDateFormat;

// Main Screen Back-end.

public class MainActivity extends AppCompatActivity {

    // Global Variables.
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    public static String uniqueID = null;
    public static String userType, userBranch, userPermission;
    public static String permissionHeading, permissionMsg;
    CurrentFragment currentFragment;
    BillingFragment billingFragment;
    MiesFragment miesFragment;
    PermissionFragment permissionFragment;
    ChipNavigationBar navBar;
    String[] currentDate;
    private View mLoading;
    private int year, month, day;

    // Date picker dialogs for Billing.
    private final DatePickerDialog.OnDateSetListener fromDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            billingFragment.showFromDate(arg1 - 2000, arg2 + 1, arg3, "fromDate");
        }
    };
    private final DatePickerDialog.OnDateSetListener toDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            billingFragment.showFromDate(arg1 - 2000, arg2 + 1, arg3, "toDate");
        }
    };

    public synchronized static String id(Context context) {
        // Get unique id.
        SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
        uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
        return uniqueID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize variables.
        navBar = findViewById(R.id.navigationBar);
        navBar.setVisibility(View.INVISIBLE);
        mLoading = findViewById(R.id.prgsbar);
        currentFragment = new CurrentFragment();
        billingFragment = new BillingFragment();
        miesFragment = new MiesFragment();
        permissionFragment = new PermissionFragment();

        // Prevent touches on loading screen
        mLoading.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        showLoading();

        // Get required values from intent.
        userType = getIntent().getStringExtra("userType");
        userBranch = getIntent().getStringExtra("Branch");
        userPermission = getIntent().getStringExtra("permission");
        permissionHeading = getIntent().getStringExtra("permissionHeading");
        permissionMsg = getIntent().getStringExtra("permissionMsg");
        uniqueID = getIntent().getStringExtra("uid");

        // Set navigation bar.
        navBar.setItemSelected(R.id.currentStatus, true);
        navBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == R.id.billing) {
                    loadFragment(billingFragment, R.id.billingFrame);
                } else if (i == R.id.currentStatus) {
                    loadFragment(currentFragment, R.id.currentStatusFrame);
                } else if (i == R.id.mies) {
                    loadFragment(miesFragment, R.id.miesFrame);
                }
            }
        });

        // Load current fragment initially.
        loadFragment(currentFragment, R.id.currentStatusFrame);
        navBar.setVisibility(View.VISIBLE);

        // Set unique id to global variable.
        uniqueID = id(getApplicationContext());
        hideLoading();
    }

    public void loadFragment(Fragment fragment, int frame) {
        // Get current window.
        Window window = MainActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // Check if user has sufficient permissions.
        if (userPermission.contentEquals("true")) {
            // Validate frames.
            if (frame == R.id.billingFrame) {
                // Show billing fragment only if user is an admin or owner.
                if (userType.contentEquals("Field")) {
                    loadFragment(permissionFragment, R.id.permissionFrame);
                } else {
                    findViewById(R.id.billingFrame).setVisibility(View.VISIBLE);
                    findViewById(R.id.currentStatusFrame).setVisibility(View.INVISIBLE);
                    findViewById(R.id.miesFrame).setVisibility(View.INVISIBLE);
                    findViewById(R.id.permissionFrame).setVisibility(View.INVISIBLE);
                    navBar.setBackgroundColor(getResources().getColor(R.color.greenAlpha));
                }
                // Change theme.
                window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.green));
            } else if (frame == R.id.currentStatusFrame) {
                // Show current status fragment.
                findViewById(R.id.billingFrame).setVisibility(View.INVISIBLE);
                findViewById(R.id.currentStatusFrame).setVisibility(View.VISIBLE);
                findViewById(R.id.miesFrame).setVisibility(View.INVISIBLE);
                findViewById(R.id.permissionFrame).setVisibility(View.INVISIBLE);
                navBar.setBackgroundColor(getResources().getColor(R.color.skylApha));
                // Change theme.
                window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.sky));
            } else if (frame == R.id.miesFrame) {
                // Show M.I.S. fragment only if user is an admin or owner.
                if (userType.contentEquals("Field")) {
                    loadFragment(permissionFragment, R.id.permissionFrame);
                } else {
                    findViewById(R.id.billingFrame).setVisibility(View.INVISIBLE);
                    findViewById(R.id.currentStatusFrame).setVisibility(View.INVISIBLE);
                    findViewById(R.id.miesFrame).setVisibility(View.VISIBLE);
                    findViewById(R.id.permissionFrame).setVisibility(View.INVISIBLE);
                    navBar.setBackgroundColor(getResources().getColor(R.color.orangeAlpha));
                }
                // Change theme.
                window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.orange));
            } else if (frame == R.id.permissionFrame) {
                // Show permission fragment.
                findViewById(R.id.billingFrame).setVisibility(View.INVISIBLE);
                findViewById(R.id.currentStatusFrame).setVisibility(View.INVISIBLE);
                findViewById(R.id.miesFrame).setVisibility(View.INVISIBLE);
                findViewById(R.id.permissionFrame).setVisibility(View.VISIBLE);
            }
        } else if (frame != R.id.permissionFrame && userPermission.contentEquals("false")) {
            // Show permission fragment if user has no sufficient permissions.
            loadFragment(permissionFragment, R.id.permissionFrame);
            return;
        }
        // Switch fragments.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(frame, fragment);
        transaction.commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected Dialog onCreateDialog(int id) {
        // Set current date, min date, max date in Date picker dialog.
        currentDate = new SimpleDateFormat("dd/MM/yy").format(java.util.Calendar.getInstance().getTime()).split("/");
        int currentDay = Integer.parseInt(currentDate[0]);
        if (id == 999) {
            String[] date = billingFragment.fromDate.getText().toString().split("/");
            year = Integer.parseInt("20" + date[2]);
            month = Integer.parseInt(date[1]) - 1;
            day = Integer.parseInt(date[0]);
            Calendar min = Calendar.getInstance();
            min.set(2001, 1, 1);
            Calendar max = Calendar.getInstance();
            max.set(year, month, currentDay);
            DatePickerDialog dp = new DatePickerDialog(this, fromDateListener, year, month, day);
            dp.getDatePicker().updateDate(year, month, day);
            dp.getDatePicker().setMinDate(min.getTimeInMillis());
            dp.getDatePicker().setMaxDate(max.getTimeInMillis());
            return dp;
        } else if (id == 990) {
            String[] date = billingFragment.toDate.getText().toString().split("/");
            year = Integer.parseInt("20" + date[2]);
            month = Integer.parseInt(date[1]) - 1;
            day = Integer.parseInt(date[0]);
            Calendar min = Calendar.getInstance();
            min.set(2001, 1, 1);
            Calendar max = Calendar.getInstance();
            max.set(year, month, day);
            DatePickerDialog dp = new DatePickerDialog(this, toDateListener, year, month, day);
            dp.getDatePicker().updateDate(year, month, day);
            dp.getDatePicker().setMinDate(min.getTimeInMillis());
            dp.getDatePicker().setMaxDate(max.getTimeInMillis());
            return dp;
        }
        return null;
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