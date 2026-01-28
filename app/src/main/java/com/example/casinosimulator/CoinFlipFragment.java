package com.example.casinosimulator;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Random;

public class CoinFlipFragment extends Fragment {

    private static final String PREFS_NAME = "casino";
    private static final String KEY_TEXT_SIZE = "text_size";
    private static final String KEY_COIN_WIN_STREAK = "coin_win_streak";

    private static final int BET_STEP = 50;
    private static final int MIN_BET = 50;

    private static final int WIN_CHANCE_PERCENT = 40;

    private TextView txtBalance;
    private TextView txtResult;
    private TextView txtBetLabel;
    private TextView txtBetValue;
    private ImageView imgCoin;

    private int balance;
    private int bet = MIN_BET;
    private int winStreak = 0;

    private CasinoDbHelper db;
    private SharedPreferences prefs;
    private boolean isAnimating = false;

    public CoinFlipFragment() {
        super(R.layout.fragment_coin_flip);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        txtBalance = view.findViewById(R.id.txtBalance);
        txtResult = view.findViewById(R.id.txtResult);
        txtBetLabel = view.findViewById(R.id.txtBetLabel);
        txtBetValue = view.findViewById(R.id.txtBetValue);
        imgCoin = view.findViewById(R.id.imgCoin);

        ImageButton btnUp = view.findViewById(R.id.btnBetUp);
        ImageButton btnDown = view.findViewById(R.id.btnBetDown);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        db = new CasinoDbHelper(requireContext());
        balance = db.getBalance();

        winStreak = prefs.getInt(KEY_COIN_WIN_STREAK, 0);

        if (bet > balance) bet = Math.max(MIN_BET, (balance / BET_STEP) * BET_STEP);

        applyTextSize();
        updateBalance();
        updateBet();

        btnUp.setOnClickListener(v -> {
            if (isAnimating) return;
            if (bet + BET_STEP <= balance) {
                bet += BET_STEP;
                updateBet();
            } else {
                Snackbar.make(imgCoin, R.string.not_enough_balance, Snackbar.LENGTH_SHORT).show();
            }
        });

        btnDown.setOnClickListener(v -> {
            if (isAnimating) return;
            if (bet - BET_STEP >= MIN_BET) {
                bet -= BET_STEP;
                updateBet();
            }
        });

        imgCoin.setOnClickListener(v -> flip());
    }

    private void flip() {
        if (isAnimating) return;
        if (bet < MIN_BET) return;

        balance = db.getBalance();
        if (bet > balance) {
            Snackbar.make(requireView(), R.string.not_enough_balance, Snackbar.LENGTH_SHORT).show();
            return;
        }

        isAnimating = true;

        ObjectAnimator anim = ObjectAnimator.ofFloat(imgCoin, "rotationY", 0f, 1080f);
        anim.setDuration(800);
        anim.start();

        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                boolean win = new Random().nextInt(100) < WIN_CHANCE_PERCENT;

                int delta;
                if (win) {
                    winStreak++;
                    int payout = bet * winStreak;
                    balance += payout;
                    delta = payout;
                    txtResult.setText(getString(R.string.coin_result_win, winStreak, payout));
                } else {
                    winStreak = 0;
                    balance -= bet;
                    delta = -bet;
                    txtResult.setText(getString(R.string.coin_result_lose, bet));
                }

                db.setBalance(balance);
                db.addHistory("Coin Flip", bet, delta, System.currentTimeMillis());

                prefs.edit().putInt(KEY_COIN_WIN_STREAK, winStreak).apply();

                if (bet > balance) bet = Math.max(MIN_BET, (balance / BET_STEP) * BET_STEP);

                updateBalance();
                updateBet();
                isAnimating = false;
            }
        });
    }

    private void updateBalance() {
        txtBalance.setText(getString(R.string.balance_label, balance));
    }

    private void updateBet() {
        txtBetValue.setText(getString(R.string.bet_value, bet));
    }

    private void applyTextSize() {
        float scale = getScale();

        setSp(txtBalance, 26f * scale);
        setSp(txtBetLabel, 20f * scale);
        setSp(txtBetValue, 22f * scale);
        setSp(txtResult, 24f * scale);
    }

    private float getScale() {
        String v = prefs.getString(KEY_TEXT_SIZE, "medium");
        if ("small".equals(v)) return 0.8f;
        if ("large".equals(v)) return 1.25f;
        return 1.0f;
    }

    private void setSp(TextView tv, float sp) {
        if (tv != null) tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
    }
}