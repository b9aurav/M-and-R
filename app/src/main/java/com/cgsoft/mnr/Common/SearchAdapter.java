package com.cgsoft.mnr.Common;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.cgsoft.mnr.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

// Adapter class for Search.

public class SearchAdapter extends FirebaseRecyclerAdapter<ContainerDetails, SearchAdapter.searchViewholder> {

    // Global Variable.
    public static int count;

    public SearchAdapter(@NonNull FirebaseRecyclerOptions<ContainerDetails> options){
        // Constructor
        super(options);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onBindViewHolder(@NonNull searchViewholder holder, int position, @NonNull ContainerDetails model) {
        // Fill detail values of container to Front-end widgets.
        holder.ContainerNo.setText(model.getContainer());
        holder.size.setText(model.getSize());
        holder.repdate.setText(model.getRepairDate());
        holder.amount.setText(model.getTotalCost());
        holder.line.setText(model.getLine());
        holder.remarks.setText(model.getRemarks());
        holder.estdate.setText(model.getEstdDate());
        holder.branch.setText(model.getBranch());
        holder.appdate.setText(model.getAppDate());
        holder.date.setText(model.getDate());
        holder.location.setText(model.getLocation());
        holder.status.setText(model.getStatus());
        if (model.getStatus().contentEquals("AI")) {
            holder.img.setImageDrawable(holder.c.getDrawable(R.drawable.red_container));
        } else if (model.getStatus().contentEquals("AR")) {
            holder.img.setImageDrawable(holder.c.getDrawable(R.drawable.orange_container));
        } else if (model.getStatus().contentEquals("AV")) {
            holder.img.setImageDrawable(holder.c.getDrawable(R.drawable.green_container));
        } else if (model.getStatus().contentEquals("AP")) {
            holder.img.setImageDrawable(holder.c.getDrawable(R.drawable.blue_container));
        }
        // Calculate counts.
        count++;
        FindActivity.showLoading();
        if (count == 0) {
            FindActivity.records.setText("No Records found!");
        } else {
            FindActivity.records.setText("Total "  + count + " Record(s) found.");
        }
        FindActivity.hideLoading();
    }

    @NonNull
    @Override
    public searchViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        // Integrating Front-end file to the View.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_container, parent, false);
        return new searchViewholder(view);
    }

    class searchViewholder extends RecyclerView.ViewHolder {
        // Model class for Front-end View.
        TextView ContainerNo, size, repdate, line, amount, remarks, date, estdate, appdate, branch, location, status;
        ImageView img;
        Context c;
        public searchViewholder(@NonNull View itemView){
            // Constructor
            super(itemView);
            ContainerNo = itemView.findViewById(R.id.containerno);
            size = itemView.findViewById(R.id.size);
            repdate = itemView.findViewById(R.id.repairdate);
            line = itemView.findViewById(R.id.line);
            amount = itemView.findViewById(R.id.totalcost);
            remarks = itemView.findViewById(R.id.remarks);
            estdate = itemView.findViewById(R.id.estdate);
            branch = itemView.findViewById(R.id.branch);
            appdate = itemView.findViewById(R.id.appdate);
            date = itemView.findViewById(R.id.date);
            location = itemView.findViewById(R.id.location);
            status = itemView.findViewById(R.id.status);
            img = itemView.findViewById(R.id.ContainerImgView);
            c = itemView.getContext();
        }
    }
}