package com.cgsoft.mnr.Registration;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cgsoft.mnr.Common.MainActivity;
import com.cgsoft.mnr.Common.User;
import com.cgsoft.mnr.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.android.widget.AnimatedSvgView;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// Registration Screen Back-end.

public class RegistrationActivity extends AppCompatActivity {

    // Global Variables.
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    private static String uniqueID = null;
    EditText nameET, phoneET, otpET;
    Spinner userTypeSp, branchSp;
    MaterialButton submitBtn, verifyBtn;
    RelativeLayout otpLayout, inputLayout;
    FirebaseDatabase firebaseDatabase;
    String mVerificationId;
    TextView prgsMsg;
    private FirebaseAuth mAuth;
    private View mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize variables.
        firebaseDatabase = FirebaseDatabase.getInstance();
        AnimatedSvgView svgView = (AnimatedSvgView) findViewById(R.id.animated_svg_view);
        svgView.start();
        mLoading = findViewById(R.id.prgsbar);
        prgsMsg = findViewById(R.id.prgsMsg);
        prgsMsg.setText("Collecting info...");

        // Prevent touches on loading screen.
        mLoading.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        showLoading();

        // Fetch unique id of device.
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
        uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
        //uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, "6b9cbf7f-e058-44f7-a0f8-29808cd3d156");

        if (uniqueID == null) {
            // New user.
            // Initialize registration page widget variables.
            mAuth = FirebaseAuth.getInstance();
            branchSp = findViewById(R.id.branchSpinner);
            userTypeSp = findViewById(R.id.userTypeSpinner);
            nameET = findViewById(R.id.nameEditText);
            phoneET = findViewById(R.id.phoneEditText);
            otpET = findViewById(R.id.otpEditText);
            submitBtn = findViewById(R.id.submitBtn);
            verifyBtn = findViewById(R.id.verifyBtn);
            otpLayout = findViewById(R.id.otpLayout);
            inputLayout = findViewById(R.id.inputLayout);
            inputLayout.setVisibility(View.VISIBLE);
            DatabaseReference branchRef = firebaseDatabase.getReference("Data/");
            branchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Fetch branches from database and show in spinner.
                    final ArrayList<String> branches = new ArrayList<String>();
                    for (DataSnapshot branchSnapshot : snapshot.getChildren()) {
                        String branch = branchSnapshot.getKey();
                        branches.add(branch);
                    }
                    ArrayAdapter<String> branchAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, branches);
                    branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    branchSp.setAdapter(branchAdapter);
                    nameET.setEnabled(true);
                    phoneET.setEnabled(true);
                    hideLoading();
                    prgsMsg.setText("");
                    svgView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            userTypeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // If user is an owner, Disable branch spinner.
                    branchSp.setEnabled(!userTypeSp.getSelectedItem().toString().contentEquals("H.O."));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            submitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Verify details and send OTP to mobile number.
                    if (!nameET.getText().toString().contentEquals("") && !phoneET.getText().toString().contentEquals("") && phoneET.getText().toString().length() == 10) {
                        sendOTP("+91" + phoneET.getText().toString());
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Enter valid details!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            verifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Verify OTP.
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otpET.getText().toString());
                    signInWithPhoneAuthCredential(credential);
                }
            });
        } else {

            // Existed user.
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users/" + uniqueID);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Switch to Main Screen.
                    Intent i = new Intent(RegistrationActivity.this, MainActivity.class);
                    i.putExtra("Branch", snapshot.child("branch").getValue().toString());
                    i.putExtra("permission", snapshot.child("permission").getValue().toString());
                    i.putExtra("userType", snapshot.child("userType").getValue().toString());
                    i.putExtra("uid", uniqueID);
                    String heading, msg;
                    if (snapshot.child("userType").getValue().toString().contentEquals("Field")) {
                        heading = "Access Denied!";
                        msg = "Only admin can access these details. Permission required to continue!";
                    } else {
                        heading = "";
                        msg = "";
                    }

                    if (snapshot.child("permission").getValue().toString().contentEquals("false")) {
                        // User needs permission to view data!
                        heading = "Verification Pending";
                        msg = "Registration is successful, please apply for account verification using unique id.";
                    }
                    i.putExtra("permissionHeading", heading);
                    i.putExtra("permissionMsg", msg);
                    startActivity(i);
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        showLoading();
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Registration success.
                    // Generate new Unique id.
                    hideLoading();
                    uniqueID = UUID.randomUUID().toString();
                    SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(PREF_UNIQUE_ID, uniqueID);
                    editor.commit();

                    // Create user.
                    User user = new User();
                    user.setUid(uniqueID);
                    user.setPermission("false");
                    user.setUserType(userTypeSp.getSelectedItem().toString());
                    if (userTypeSp.getSelectedItem().toString().contentEquals("H.O.")) {
                        user.setBranch("");
                    } else {
                        user.setBranch(branchSp.getSelectedItem().toString());
                    }
                    user.setName(nameET.getText().toString());
                    user.setPhone("+91" + phoneET.getText().toString());

                    // Save user to database.
                    DatabaseReference mbase = FirebaseDatabase.getInstance().getReference("Users/" + uniqueID);
                    mbase.setValue(user);

                    // Switch to Main Screen.
                    Intent i = new Intent(RegistrationActivity.this, MainActivity.class);
                    i.putExtra("userType", userTypeSp.getSelectedItem().toString());
                    if (userTypeSp.getSelectedItem().toString().contentEquals("H.O.")) {
                        i.putExtra("Branch", "");
                    } else {
                        i.putExtra("Branch", branchSp.getSelectedItem().toString());
                    }
                    i.putExtra("uid", uniqueID);
                    i.putExtra("permission", "false");
                    i.putExtra("permissionHeading", "Verification Pending");
                    i.putExtra("permissionMsg", "Registration is successful, please apply for account verification using unique id.");
                    startActivity(i);
                    hideLoading();
                    hideLoading();
                    Toast.makeText(RegistrationActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // Registration Failed.
                    hideLoading();
                    Toast.makeText(RegistrationActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void sendOTP(String phoneNo) {
        showLoading();
        long timeout = 60;
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // Verification Success.
                Toast.makeText(RegistrationActivity.this, "Success", Toast.LENGTH_SHORT).show();
                hideLoading();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // Verification Failed.
                Toast.makeText(RegistrationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                hideLoading();
            }

            @Override
            public void onCodeSent(@NonNull String verificationID, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationID, forceResendingToken);
                // Disable editing registration fields and ask for OTP.
                mVerificationId = verificationID;
                nameET.setEnabled(false);
                phoneET.setEnabled(false);
                branchSp.setEnabled(false);
                userTypeSp.setEnabled(false);
                submitBtn.setEnabled(false);
                otpLayout.setVisibility(View.VISIBLE);
                hideLoading();
            }
        };
        // Setting variables and send an OTP.
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNo)
                .setTimeout(timeout, TimeUnit.SECONDS)
                .setActivity(RegistrationActivity.this)
                .setCallbacks(mCallBacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
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