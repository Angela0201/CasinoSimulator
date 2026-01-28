package com.example.casinosimulator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.Random;

public class DiceFragment extends Fragment {
    private CasinoDbHelper db;
    private static final String PREFS_NAME = "casino";
    private static final String KEY_TEXT_SIZE = "text_size";
    private static final int JACKPOT_VALUE = 500;

    private TextView txtBalance;
    private TextView txtPickLabel;
    private TextView txtRolled;
    private TextView txtResult;
    private TextView txtBetTitle;

    private LinearLayout jackpotBanner;
    private TextView txtJackpotTitle;
    private TextView txtJackpotAmount;

    private ImageView imgDice;

    private SharedPreferences prefs;
    private int balance;

    private int picked = 1;
    private int bet = 50;

    private boolean isAnimating = false;
    private boolean syncing = false;

    public DiceFragment() {
        super(R.layout.fragment_dice);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        db = new CasinoDbHelper(requireContext());
        balance = db.getBalance();

        txtBalance = view.findViewById(R.id.txtBalance);
        txtPickLabel = view.findViewById(R.id.txtPickLabel);
        txtRolled = view.findViewById(R.id.txtRolled);
        txtResult = view.findViewById(R.id.txtResult);
        txtBetTitle = view.findViewById(R.id.txtBetTitle);

        jackpotBanner = view.findViewById(R.id.jackpotBanner);
        txtJackpotTitle = view.findViewById(R.id.txtJackpotTitle);
        txtJackpotAmount = view.findViewById(R.id.txtJackpotAmount);

        imgDice = view.findViewById(R.id.imgDice);

        MaterialButtonToggleGroup numbersRow1 = view.findViewById(R.id.toggleNumbersRow1);
        MaterialButtonToggleGroup numbersRow2 = view.findViewById(R.id.toggleNumbersRow2);

        MaterialButtonToggleGroup betRow1 = view.findViewById(R.id.toggleBetRow1);
        MaterialButtonToggleGroup betRow2 = view.findViewById(R.id.toggleBetRow2);

        numbersRow1.check(R.id.btnN1);
        numbersRow2.clearChecked();

        betRow1.check(R.id.btnBet50);
        betRow2.clearChecked();

        applyToggleColors(numbersRow1);
        applyToggleColors(numbersRow2);
        applyToggleColors(betRow1);
        applyToggleColors(betRow2);

        numbersRow1.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked || syncing) return;
            syncing = true;
            numbersRow2.clearChecked();
            syncing = false;
            setPickedById(checkedId);
            applyToggleColors(numbersRow1);
            applyToggleColors(numbersRow2);
        });

        numbersRow2.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked || syncing) return;
            syncing = true;
            numbersRow1.clearChecked();
            syncing = false;
            setPickedById(checkedId);
            applyToggleColors(numbersRow1);
            applyToggleColors(numbersRow2);
        });

        betRow1.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked || syncing) return;
            syncing = true;
            betRow2.clearChecked();
            syncing = false;
            setBetById(checkedId);
            applyToggleColors(betRow1);
            applyToggleColors(betRow2);
        });

        betRow2.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked || syncing) return;
            syncing = true;
            betRow1.clearChecked();
            syncing = false;
            setBetById(checkedId);
            applyToggleColors(betRow1);
            applyToggleColors(betRow2);
        });

        applyTextSize();
        updateBalance();

        imgDice.setOnClickListener(v -> roll());
    }

    private void setPickedById(int id) {
        if (id == R.id.btnN1) picked = 1;
        else if (id == R.id.btnN2) picked = 2;
        else if (id == R.id.btnN3) picked = 3;
        else if (id == R.id.btnN4) picked = 4;
        else if (id == R.id.btnN5) picked = 5;
        else if (id == R.id.btnN6) picked = 6;
    }

    private void setBetById(int id) {
        if (id == R.id.btnBet50) bet = 50;
        else if (id == R.id.btnBet100) bet = 100;
        else if (id == R.id.btnBet200) bet = 200;
        else if (id == R.id.btnBet500) bet = 500;
    }

    private void roll() {
        if (isAnimating) return;

        balance = db.getBalance();
        if (bet > balance) {
            Snackbar.make(requireView(), getString(R.string.not_enough_balance), Snackbar.LENGTH_SHORT).show();
            return;
        }

        isAnimating = true;
        jackpotBanner.setVisibility(View.GONE);

        ObjectAnimator spin = ObjectAnimator.ofFloat(imgDice, "rotation", 0f, 1080f);
        spin.setDuration(700);
        spin.start();

        spin.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                int rolled = new Random().nextInt(6) + 1;

                imgDice.setImageResource(getDiceRes(rolled));
                txtRolled.setText(getString(R.string.rolled, rolled));

                int delta;
                if (rolled == picked) delta = bet * 4;
                else delta = -bet;

                if (rolled == 6) {
                    delta += JACKPOT_VALUE;
                    showJackpot();
                }

                balance += delta;

                db.setBalance(balance);
                db.addHistory("Dice", bet, delta, System.currentTimeMillis());
                updateBalance();

                if (delta >= 0) txtResult.setText(getString(R.string.payout_win, delta));
                else txtResult.setText(getString(R.string.payout_lose, -delta));

                bounceDice();
                isAnimating = false;
            }
        });
    }

    private void bounceDice() {
        ObjectAnimator sxUp = ObjectAnimator.ofFloat(imgDice, "scaleX", 1f, 1.12f);
        ObjectAnimator syUp = ObjectAnimator.ofFloat(imgDice, "scaleY", 1f, 1.12f);
        sxUp.setDuration(140);
        syUp.setDuration(140);

        ObjectAnimator sxDown = ObjectAnimator.ofFloat(imgDice, "scaleX", 1.12f, 1f);
        ObjectAnimator syDown = ObjectAnimator.ofFloat(imgDice, "scaleY", 1.12f, 1f);
        sxDown.setDuration(180);
        syDown.setDuration(180);

        AnimatorSet set = new AnimatorSet();
        set.play(sxUp).with(syUp);
        set.play(sxDown).with(syDown).after(sxUp);
        set.start();
    }

    private void showJackpot() {
        txtJackpotTitle.setText(getString(R.string.jackpot));
        txtJackpotAmount.setText(getString(R.string.jackpot_amount, JACKPOT_VALUE));
        jackpotBanner.setAlpha(1f);
        jackpotBanner.setVisibility(View.VISIBLE);
    }

    private void applyToggleColors(MaterialButtonToggleGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (!(child instanceof MaterialButton)) continue;

            MaterialButton b = (MaterialButton) child;
            boolean checked = group.getCheckedButtonId() == b.getId();

            if (checked) {
                b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFB39DFF));
                b.setTextColor(0xFF1B1B1B);
            } else {
                b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF5E55B3));
                b.setTextColor(0xFFFFFFFF);
            }
        }
    }

    private int getDiceRes(int value) {
        if (value == 1) return R.drawable.dice_1;
        if (value == 2) return R.drawable.dice_2;
        if (value == 3) return R.drawable.dice_3;
        if (value == 4) return R.drawable.dice_4;
        if (value == 5) return R.drawable.dice_5;
        return R.drawable.dice_6;
    }

    private void updateBalance() {
        txtBalance.setText(getString(R.string.balance_label, balance));
    }

    private void applyTextSize() {
        String v = prefs.getString(KEY_TEXT_SIZE, "medium");

        if ("small".equals(v)) {
            setSp(txtBalance, 20f);
            setSp(txtPickLabel, 16f);
            setSp(txtRolled, 14f);
            setSp(txtResult, 18f);
            setSp(txtBetTitle, 16f);
            setSp(txtJackpotTitle, 20f);
            setSp(txtJackpotAmount, 18f);
        } else if ("large".equals(v)) {
            setSp(txtBalance, 34f);
            setSp(txtPickLabel, 22f);
            setSp(txtRolled, 18f);
            setSp(txtResult, 30f);
            setSp(txtBetTitle, 22f);
            setSp(txtJackpotTitle, 30f);
            setSp(txtJackpotAmount, 24f);
        } else {
            setSp(txtBalance, 28f);
            setSp(txtPickLabel, 18f);
            setSp(txtRolled, 18f);
            setSp(txtResult, 24f);
            setSp(txtBetTitle, 18f);
            setSp(txtJackpotTitle, 24f);
            setSp(txtJackpotAmount, 20f);
        }
    }

    private void setSp(TextView tv, float sp) {
        if (tv != null) tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
    }
}