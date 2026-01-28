package com.example.casinosimulator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    private static final String PREFS_NAME = "casino";
    private static final String KEY_TEXT_SIZE = "text_size";

    public HistoryFragment() {
        super(R.layout.fragment_history);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);

        TextView title = view.findViewById(R.id.txtHistoryTitle);
        TextView empty = view.findViewById(R.id.txtEmpty);
        RecyclerView rv = view.findViewById(R.id.rvHistory);

        float scale = getScale(prefs);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f * scale);
        empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f * scale);

        CasinoDbHelper db = new CasinoDbHelper(requireContext());
        ArrayList<HistoryItem> items = db.getHistory();

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new HistoryAdapter(items));

        empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private float getScale(SharedPreferences prefs) {
        String v = prefs.getString(KEY_TEXT_SIZE, "medium");
        if ("small".equals(v)) return 0.8f;
        if ("large".equals(v)) return 1.25f;
        return 1.0f;
    }
}