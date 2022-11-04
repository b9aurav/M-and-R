package com.cgsoft.mnr.CurrentStatus;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

// Adapter class for AP.
public class APAdapter extends RecyclerView.Adapter<APAdapter.apViewholder> {

    // Global Variables.
    private final ArrayList<String> apUnits;
    DatabaseReference mbase;

    public APAdapter(ArrayList<String> apUnits, String path) {
        // Constructor
        this.apUnits = apUnits;
        mbase = FirebaseDatabase.getInstance().getReference(path);
    }

    @NonNull
    @Override
    public apViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Integrating Front-end file to the View.
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ap_details, parent, false);
        return new apViewholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull apViewholder holder, int position) {
        mbase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // For each container in AP Category.
                for (DataSnapshot year : snapshot.getChildren()) {
                    // Year
                    for (DataSnapshot month : year.getChildren()) {
                        // Month
                        for (DataSnapshot day : month.getChildren()) {
                            // Day
                            for (DataSnapshot contSnapshot : day.getChildren()) {
                                // Container
                                // Check if container matches the current item of list.
                                if (contSnapshot.child("Container").getValue().toString().contentEquals(apUnits.get(position))) {
                                    // Fill detail values of container to Front-end widgets.
                                    holder.ContainerNo.setText(contSnapshot.child("Container").getValue().toString());
                                    holder.size.setText(contSnapshot.child("Size").getValue().toString());
                                    holder.estdate.setText(contSnapshot.child("EstdDate").getValue().toString());
                                    holder.line.setText(contSnapshot.child("Line").getValue().toString());
                                    holder.remarks.setText(contSnapshot.child("Remarks").getValue().toString());
                                    try {
                                        // Calculate date difference.
                                        String today = new SimpleDateFormat("dd/MM/yy").format(Calendar.getInstance().getTime());
                                        String date = contSnapshot.child("EstdDate").getValue().toString();
                                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
                                        Date d1 = null;
                                        Date d2 = null;
                                        d1 = format.parse(today);
                                        d2 = format.parse(date);
                                        long diff = d1.getTime() - d2.getTime();
                                        long days = diff / (24 * 60 * 60 * 1000);
                                        holder.days.setText(String.valueOf(days));
                                    } catch (Exception e) {
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
    public int getItemCount() {
        // Size of AI Category.
        return apUnits.size();
    }

    class apViewholder extends RecyclerView.ViewHolder {
        // Model class for Front-end View.
        TextView ContainerNo, size, estdate, line, days, remarks;
        public apViewholder(@NonNull View itemView){
            super(itemView);
            ContainerNo = itemView.findViewById(R.id.containerno);
            size = itemView.findViewById(R.id.size);
            estdate = itemView.findViewById(R.id.estdate);
            line = itemView.findViewById(R.id.line);
            days = itemView.findViewById(R.id.days);
            remarks = itemView.findViewById(R.id.remarks);
        }
    }

}
