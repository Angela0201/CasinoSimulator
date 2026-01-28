package com.example.casinosimulator;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private static final String PREFS_NAME = "casino";
    private static final String KEY_TEXT_SIZE = "text_size";

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        CasinoDbHelper db = new CasinoDbHelper(requireContext());

        TextView txtBalanceTitle = view.findViewById(R.id.txtBalanceTitle);
        TextView txtBalanceValue = view.findViewById(R.id.txtBalanceValue);

        TextView txtDiceEarned = view.findViewById(R.id.txtDiceEarned);
        TextView txtSlotsEarned = view.findViewById(R.id.txtSlotsEarned);
        TextView txtCoinEarned = view.findViewById(R.id.txtCoinEarned);

        TextView txtDiceLost = view.findViewById(R.id.txtDiceLost);
        TextView txtSlotsLost = view.findViewById(R.id.txtSlotsLost);
        TextView txtCoinLost = view.findViewById(R.id.txtCoinLost);

        float scale = getScale(prefs);

        setSp(view.findViewById(R.id.txtHomeTitle), 30f * scale);
        setSp(txtBalanceTitle, 18f * scale);
        setSp(txtBalanceValue, 30f * scale);
        setSp(view.findViewById(R.id.txtStatsTitle), 20f * scale);

        setSp(view.findViewById(R.id.txtColDice), 16f * scale);
        setSp(view.findViewById(R.id.txtColSlots), 16f * scale);
        setSp(view.findViewById(R.id.txtColCoin), 16f * scale);

        setSp(view.findViewById(R.id.txtRowEarned), 16f * scale);
        setSp(view.findViewById(R.id.txtRowLost), 16f * scale);

        setSp(txtDiceEarned, 16f * scale);
        setSp(txtSlotsEarned, 16f * scale);
        setSp(txtCoinEarned, 16f * scale);

        setSp(txtDiceLost, 16f * scale);
        setSp(txtSlotsLost, 16f * scale);
        setSp(txtCoinLost, 16f * scale);

        int night = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDark = night == Configuration.UI_MODE_NIGHT_YES;

        int titleColor = isDark ? 0xFFFFFFFF : 0xFF000000;
        int valueColor = isDark ? 0xFFE0E0E0 : 0xFF6B6B6B;

        if (txtBalanceTitle != null) txtBalanceTitle.setTextColor(titleColor);
        if (txtBalanceValue != null) txtBalanceValue.setTextColor(valueColor);

        int balance = db.getBalance();
        if (txtBalanceValue != null) txtBalanceValue.setText(String.valueOf(balance));

        CasinoDbHelper.GameStats dice = db.getStatsForGame("Dice");
        CasinoDbHelper.GameStats slots = db.getStatsForGame("Slots");
        CasinoDbHelper.GameStats coin = db.getStatsForGame("Coin Flip");

        txtDiceEarned.setText(String.valueOf(dice.earned));
        txtSlotsEarned.setText(String.valueOf(slots.earned));
        txtCoinEarned.setText(String.valueOf(coin.earned));

        txtDiceLost.setText(String.valueOf(dice.lost));
        txtSlotsLost.setText(String.valueOf(slots.lost));
        txtCoinLost.setText(String.valueOf(coin.lost));
    }

    private float getScale(SharedPreferences prefs) {
        String v = prefs.getString(KEY_TEXT_SIZE, "medium");
        if ("small".equals(v)) return 0.8f;
        if ("large".equals(v)) return 1.25f;
        return 1.0f;
    }

    private void setSp(TextView tv, float sp) {
        if (tv != null) tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
    }
}