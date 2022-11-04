package com.cgsoft.mnr.Common;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.cgsoft.mnr.R;
import com.google.android.material.button.MaterialButton;

// Permission Fragment Back-end.

public class PermissionFragment extends Fragment {

    public PermissionFragment() {
        // Constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize widgets.
        View v = inflater.inflate(R.layout.fragment_permission, container, false);
        TextView uidTV = v.findViewById(R.id.uid);
        TextView errorMsg = v.findViewById(R.id.errormsg);
        TextView errorHeading = v.findViewById(R.id.errorHeading);
        MaterialButton copyBtn = v.findViewById(R.id.copyBtn);

        // Show device unique id.
        uidTV.setText("Unique ID : " + MainActivity.uniqueID);

        // Show message for permission.
        errorMsg.setText(MainActivity.permissionMsg);
        errorHeading.setText(MainActivity.permissionHeading);

        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Copy device id.
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Unique ID", MainActivity.uniqueID);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Unique ID copied", Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }
}