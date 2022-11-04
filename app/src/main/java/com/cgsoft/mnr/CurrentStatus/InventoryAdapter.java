package com.cgsoft.mnr.CurrentStatus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

// Adapter class for Inventory.
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.invViewholder> {

    // Global Variables.
    private final ArrayList<String> invUnits;
    DatabaseReference mbase;

    public InventoryAdapter(ArrayList<String> invUnits, String path) {
        // Constructor
        this.invUnits = invUnits;
        mbase = FirebaseDatabase.getInstance().getReference(path);
    }

    @NonNull
    @Override
    public invViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Integrating Front-end file to the View.
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_details, parent, false);
        return new invViewholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull invViewholder holder, int position) {
        // For each container in inventory.
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
                                // Check if container matches the current item of list.
                                if (contSnapshot.child("Container").getValue().toString().contentEquals(invUnits.get(position))) {
                                    // Fill detail values of container to Front-end widgets.
                                    holder.ContainerNo.setText(contSnapshot.child("Container").getValue().toString());
                                    holder.size.setText(contSnapshot.child("Size").getValue().toString());
                                    holder.line.setText(contSnapshot.child("Line").getValue().toString());
                                    holder.status.setText(contSnapshot.child("Status").getValue().toString());
                                    holder.remarks.setText(contSnapshot.child("Remarks").getValue().toString());
                                    if (contSnapshot.child("Status").getValue().toString().contentEquals("AI")) {
                                        holder.img.setImageDrawable(holder.c.getDrawable(R.drawable.red_container));
                                    } else if (contSnapshot.child("Status").getValue().toString().contentEquals("AR")) {
                                        holder.img.setImageDrawable(holder.c.getDrawable(R.drawable.orange_container));
                                    } else if (contSnapshot.child("Status").getValue().toString().contentEquals("AV")) {
                                        holder.img.setImageDrawable(holder.c.getDrawable(R.drawable.green_container));
                                    } else if (contSnapshot.child("Status").getValue().toString().contentEquals("AP")) {
                                        holder.img.setImageDrawable(holder.c.getDrawable(R.drawable.blue_container));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public int getItemCount() {
        // Size of inventory.
        return invUnits.size();
    }

    class invViewholder extends RecyclerView.ViewHolder {
        // Model class for Front-end View.
        TextView ContainerNo, size, line, status, remarks;
        ImageView img;
        Context c;

        public invViewholder(@NonNull View itemView) {
            // Constructor.
            super(itemView);
            ContainerNo = itemView.findViewById(R.id.containerno);
            size = itemView.findViewById(R.id.size);
            line = itemView.findViewById(R.id.line);
            status = itemView.findViewById(R.id.status);
            remarks = itemView.findViewById(R.id.remarks);
            img = itemView.findViewById(R.id.ContainerImgView);
            c = itemView.getContext();
        }
    }
}