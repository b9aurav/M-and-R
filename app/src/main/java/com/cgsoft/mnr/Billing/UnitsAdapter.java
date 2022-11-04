package com.cgsoft.mnr.Billing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cgsoft.mnr.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// Adapter class for Units in billing.

public class UnitsAdapter extends RecyclerView.Adapter<UnitsAdapter.unitsViewholder> {
    // Global Variables.
    private final ArrayList<String> units;
    DatabaseReference mbase;

    public UnitsAdapter(ArrayList<String> units, String path) {
        // Constructor
        this.units = units;
        mbase = FirebaseDatabase.getInstance().getReference(path);
    }

    @NonNull
    @Override
    public unitsViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Integrating Front-end file to the View.
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.units_details, parent, false);
        return new unitsViewholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull unitsViewholder holder, int position) {
        mbase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // For each container on path.
                for (DataSnapshot year : snapshot.getChildren()) {
                    // Year
                    for (DataSnapshot month : year.getChildren()) {
                        // Month
                        for (DataSnapshot day : month.getChildren()) {
                            // Day
                            for (DataSnapshot contSnapshot : day.getChildren()) {
                                // Container
                                // Check if container matches the current item of list.
                                if (contSnapshot.child("Container").getValue().toString().contentEquals(units.get(position))) {
                                    // Fill detail values of container to Front-end widgets.
                                    holder.ContainerNo.setText(contSnapshot.child("Container").getValue().toString());
                                    holder.size.setText(contSnapshot.child("Size").getValue().toString());
                                    holder.repdate.setText(contSnapshot.child("RepairDate").getValue().toString());
                                    holder.line.setText(contSnapshot.child("Line").getValue().toString());
                                    holder.amount.setText(contSnapshot.child("TotalCost").getValue().toString());
                                    holder.remarks.setText(contSnapshot.child("Remarks").getValue().toString());
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
    public int getItemCount() {
        // Size of AI Category.
        return units.size();
    }

    class unitsViewholder extends RecyclerView.ViewHolder {
        // Model class for Front-end View.
        TextView ContainerNo, size, repdate, line, amount, remarks;
        public unitsViewholder(@NonNull View itemView){
            super(itemView);
            ContainerNo = itemView.findViewById(R.id.containerno);
            size = itemView.findViewById(R.id.size);
            repdate = itemView.findViewById(R.id.repdate);
            line = itemView.findViewById(R.id.line);
            amount = itemView.findViewById(R.id.amount);
            remarks = itemView.findViewById(R.id.remarks);
        }
    }
}
