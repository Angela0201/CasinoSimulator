package com.example.casinosimulator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

    private final ArrayList<HistoryItem> items;
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public HistoryAdapter(ArrayList<HistoryItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        HistoryItem it = items.get(position);

        String sign = it.delta >= 0 ? "+" : "-";
        int abs = Math.abs(it.delta);

        h.txtLine1.setText(
                h.itemView.getContext().getString(
                        R.string.history_line1,
                        it.game,
                        it.bet,
                        sign,
                        abs
                )
        );

        h.txtLine2.setText(fmt.format(new Date(it.time)));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView txtLine1;
        TextView txtLine2;

        public VH(@NonNull View itemView) {
            super(itemView);
            txtLine1 = itemView.findViewById(R.id.txtLine1);
            txtLine2 = itemView.findViewById(R.id.txtLine2);
        }
    }
}