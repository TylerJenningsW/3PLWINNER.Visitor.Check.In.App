package com.example.a3plwinnervisitorcheckinapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class VisitorRecyclerViewAdapter extends RecyclerView.Adapter<VisitorRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Visitor> mVisitorArrayList;
    private Context mContext;
    private VisitorRecyclerViewAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(VisitorRecyclerViewAdapter.OnItemClickListener clickListener) {
        listener = clickListener;
    }

    public VisitorRecyclerViewAdapter(ArrayList<Visitor> _visitorArrayList, Context _context) {
        mVisitorArrayList = _visitorArrayList;
        mContext = _context;
    }

    @NonNull
    @Override
    public VisitorRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View v = layoutInflater.inflate(R.layout.visitor_card, parent, false);

        return new VisitorRecyclerViewAdapter.ViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitorRecyclerViewAdapter.ViewHolder holder, int position) {
        Visitor visitor = mVisitorArrayList.get(position);
        holder.tvName.setText(visitor.getFirstName() + " " + visitor.getLastName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visitor.setCheckOutTime(getCurrentTime());
                visitor.setCheckedIn(false);
            }
        });
    }

    private String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(("MM/dd/yyyy HH:mm:ss"));
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    @Override
    public int getItemCount() {
        return mVisitorArrayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final Button btnTapToCheckout;
        public ViewHolder(@NonNull View itemView, VisitorRecyclerViewAdapter.OnItemClickListener listener) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvLocationName);
            btnTapToCheckout = itemView.findViewById(R.id.btnTapToCheckout);

            btnTapToCheckout.setOnClickListener(v ->{
                listener.onItemClick(getAdapterPosition());
            });
        }
    }

}
