package com.example.casinosimulator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Random;

public class SlotsFragment extends Fragment {

    private static final String PREFS_NAME = "casino";
    private static final String KEY_TEXT_SIZE = "text_size";

    private static final int BET_STEP = 50;
    private static final int MIN_BET = 50;

    private static final int DIAMOND_CHANCE_PERCENT = 10;

    private final Random rnd = new Random();

    private SharedPreferences prefs;

    private TextView txtBalance;
    private TextView txtBetLabel;
    private TextView txtBetValue;
    private TextView txtResult;

    private ImageView imgReel1;
    private ImageView imgReel2;
    private ImageView imgReel3;

    private ImageButton btnBetDown;
    private ImageButton btnBetUp;

    private MaterialButton btnSpin;
    private MaterialButton btnJackpotBar;
    private CasinoDbHelper db;

    private int balance;
    private int bet = MIN_BET;

    private boolean isAnimating = false;

    private static class Symbol {
        final boolean isDiamond;
        final int drawableRes;

        Symbol(boolean isDiamond, int drawableRes) {
            this.isDiamond = isDiamond;
            this.drawableRes = drawableRes;
        }
    }

    private final int[] fruits = new int[] {
            R.drawable.slot_fruit_1,
            R.drawable.slot_fruit_2,
            R.drawable.slot_fruit_3,
            R.drawable.slot_fruit_4,
            R.drawable.slot_fruit_5,
            R.drawable.slot_fruit_6,
            R.drawable.slot_fruit_7,
            R.drawable.slot_fruit_8
    };

    public SlotsFragment() {
        super(R.layout.fragment_slots);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        db = new CasinoDbHelper(requireContext());
        balance = db.getBalance();

        txtBalance = view.findViewById(R.id.txtBalance);
        txtBetLabel = view.findViewById(R.id.txtBetLabel);
        txtBetValue = view.findViewById(R.id.txtBetValue);

        imgReel1 = view.findViewById(R.id.imgReel1);
        imgReel2 = view.findViewById(R.id.imgReel2);
        imgReel3 = view.findViewById(R.id.imgReel3);

        btnBetDown = view.findViewById(R.id.btnBetDown);
        btnBetUp = view.findViewById(R.id.btnBetUp);

        btnSpin = view.findViewById(R.id.btnSpin);
        btnJackpotBar = view.findViewById(R.id.btnJackpotBar);

        txtResult = view.findViewById(R.id.txtResult);

        if (bet > balance) bet = Math.max(MIN_BET, (balance / BET_STEP) * BET_STEP);

        applyTextSize();
        updateBalance();
        updateBet();

        if (txtResult.getText() == null || txtResult.getText().toString().trim().isEmpty()) {
            txtResult.setText(R.string.blank_space);
        }

        clearJackpotBar();

        btnBetUp.setOnClickListener(v -> {
            if (isAnimating) return;
            if (bet + BET_STEP <= balance) {
                bet += BET_STEP;
                updateBet();
            } else {
                Snackbar.make(btnSpin, R.string.not_enough_balance, Snackbar.LENGTH_SHORT).show();
            }
        });

        btnBetDown.setOnClickListener(v -> {
            if (isAnimating) return;
            if (bet - BET_STEP >= MIN_BET) {
                bet -= BET_STEP;
                updateBet();
            }
        });

        btnSpin.setOnClickListener(v -> spin());
    }

    private void spin() {
        if (isAnimating) return;

        balance = db.getBalance();
        if (bet > balance) {
            Snackbar.make(requireView(), R.string.not_enough_balance, Snackbar.LENGTH_SHORT).show();
            return;
        }

        clearJackpotBar();

        isAnimating = true;
        setClickability(false);

        Symbol s1 = pickSymbol();
        Symbol s2 = pickSymbol();
        Symbol s3 = pickSymbol();

        animateReelsVertical(s1, s2, s3, () -> {
            imgReel1.setImageResource(s1.drawableRes);
            imgReel2.setImageResource(s2.drawableRes);
            imgReel3.setImageResource(s3.drawableRes);

            int diamonds = countDiamonds(s1, s2, s3);
            int multiplier = calculateMultiplier(s1, s2, s3);

            int delta;
            if (multiplier > 0) {
                delta = bet * multiplier;
                txtResult.setText(getString(R.string.payout_win, delta));
            } else {
                delta = -bet;
                txtResult.setText(getString(R.string.payout_lose, -delta));
            }

            balance += delta;
            db.setBalance(balance);
            db.addHistory("Slots", bet, delta, System.currentTimeMillis());

            if (bet > balance) bet = Math.max(MIN_BET, (balance / BET_STEP) * BET_STEP);

            updateBalance();
            updateBet();

            if (diamonds >= 2 || (diamonds == 1 && multiplier == 4)) {
                btnJackpotBar.setText(getString(R.string.slots_jackpot_bar, Math.max(0, delta)));
            }

            isAnimating = false;
            setClickability(true);
        });
    }

    private void setClickability(boolean enabled) {
        btnSpin.setClickable(enabled);
        btnBetUp.setClickable(enabled);
        btnBetDown.setClickable(enabled);
    }

    private void clearJackpotBar() {
        btnJackpotBar.setText(R.string.blank_space);
    }

    private int calculateMultiplier(Symbol a, Symbol b, Symbol c) {
        int diamonds = countDiamonds(a, b, c);

        if (diamonds == 3) return 10;
        if (diamonds == 2) return 5;

        if (diamonds == 1) {
            int f1 = a.isDiamond ? -1 : a.drawableRes;
            int f2 = b.isDiamond ? -1 : b.drawableRes;
            int f3 = c.isDiamond ? -1 : c.drawableRes;

            if (f1 != -1 && f1 == f2) return 4;
            if (f1 != -1 && f1 == f3) return 4;
            if (f2 != -1 && f2 == f3) return 4;

            return 0;
        }

        int r1 = a.drawableRes;
        int r2 = b.drawableRes;
        int r3 = c.drawableRes;

        if (r1 == r2 && r2 == r3) return 3;
        if (r1 == r2 || r1 == r3 || r2 == r3) return 2;

        return 0;
    }

    private int countDiamonds(Symbol a, Symbol b, Symbol c) {
        int d = 0;
        if (a.isDiamond) d++;
        if (b.isDiamond) d++;
        if (c.isDiamond) d++;
        return d;
    }

    private Symbol pickSymbol() {
        int roll = rnd.nextInt(100);
        if (roll < DIAMOND_CHANCE_PERCENT) {
            return new Symbol(true, R.drawable.slot_diamond);
        }
        int idx = rnd.nextInt(fruits.length);
        return new Symbol(false, fruits[idx]);
    }

    private void animateReelsVertical(Symbol s1, Symbol s2, Symbol s3, Runnable onEnd) {
        Animator a = buildReelAnimator(imgReel1, s1.drawableRes, 520);
        Animator b = buildReelAnimator(imgReel2, s2.drawableRes, 620);
        Animator c = buildReelAnimator(imgReel3, s3.drawableRes, 720);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(a, b, c);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onEnd.run();
            }
        });
        set.start();
    }

    private Animator buildReelAnimator(ImageView reel, int finalRes, long duration) {
        float offset = 160f;

        ObjectAnimator drop = ObjectAnimator.ofFloat(reel, "translationY", -offset, 0f);
        drop.setDuration(duration);

        ValueAnimator shuffle = ValueAnimator.ofInt(0, 1);
        shuffle.setDuration(duration);
        shuffle.addUpdateListener(anim -> reel.setImageResource(randomDisplaySymbol()));

        AnimatorSet set = new AnimatorSet();
        set.playTogether(drop, shuffle);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                reel.setTranslationY(0f);
                reel.setImageResource(finalRes);
            }
        });

        return set;
    }

    private int randomDisplaySymbol() {
        int roll = rnd.nextInt(100);
        if (roll < 15) return R.drawable.slot_diamond;
        return fruits[rnd.nextInt(fruits.length)];
    }

    private void updateBalance() {
        txtBalance.setText(getString(R.string.balance_label, balance));
    }

    private void updateBet() {
        txtBetValue.setText(getString(R.string.bet_value, bet));
    }

    private void applyTextSize() {
        float scale = getScale(prefs);

        setSp(txtBalance, 30f * scale);
        setSp(txtBetLabel, 16f * scale);
        setSp(txtBetValue, 20f * scale);

        if (txtResult != null) setSp(txtResult, 20f * scale);

        btnSpin.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f * scale);
        btnJackpotBar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f * scale);
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