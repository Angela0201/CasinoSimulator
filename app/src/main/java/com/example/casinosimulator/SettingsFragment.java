package com.example.casinosimulator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "casino";
    private static final String KEY_THEME = "theme";
    private static final String KEY_TEXT_SIZE = "text_size";

    private CasinoDbHelper db;

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        db = new CasinoDbHelper(requireContext());

        RadioGroup rgTheme = view.findViewById(R.id.rgTheme);
        RadioButton rbLight = view.findViewById(R.id.rbLight);
        RadioButton rbDark = view.findViewById(R.id.rbDark);

        RadioGroup rgTextSize = view.findViewById(R.id.rgTextSize);
        RadioButton rbSmall = view.findViewById(R.id.rbSmall);
        RadioButton rbMedium = view.findViewById(R.id.rbMedium);
        RadioButton rbLarge = view.findViewById(R.id.rbLarge);

        Button btnResetBalance = view.findViewById(R.id.btnResetBalance);
        Button btnClearHistory = view.findViewById(R.id.btnClearHistory);

        String theme = prefs.getString(KEY_THEME, "light");
        if (theme.equals("dark")) rbDark.setChecked(true);
        else rbLight.setChecked(true);

        String textSize = prefs.getString(KEY_TEXT_SIZE, "medium");
        if (textSize.equals("small")) rbSmall.setChecked(true);
        else if (textSize.equals("large")) rbLarge.setChecked(true);
        else rbMedium.setChecked(true);

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            String newTheme = checkedId == R.id.rbDark ? "dark" : "light";
            prefs.edit().putString(KEY_THEME, newTheme).apply();
            AppCompatDelegate.setDefaultNightMode(
                    newTheme.equals("dark")
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        rgTextSize.setOnCheckedChangeListener((group, checkedId) -> {
            String newSize;
            if (checkedId == R.id.rbSmall) newSize = "small";
            else if (checkedId == R.id.rbLarge) newSize = "large";
            else newSize = "medium";
            prefs.edit().putString(KEY_TEXT_SIZE, newSize).apply();
        });

        btnResetBalance.setOnClickListener(v -> {
            db.resetBalance();
            Toast.makeText(requireContext(),
                    getString(R.string.balance_reset_done),
                    Toast.LENGTH_SHORT).show();
        });

        btnClearHistory.setOnClickListener(v -> {
            db.clearHistory();
            Toast.makeText(requireContext(),
                    getString(R.string.history_cleared_done),
                    Toast.LENGTH_SHORT).show();
        });
    }
}