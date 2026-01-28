package com.example.casinosimulator;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.navigation.Navigation;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GamesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_games, container, false);

        View btnDice = root.findViewById(R.id.btnDice);
        View btnSlots = root.findViewById(R.id.btnSlots);
        View btnCoinFlip = root.findViewById(R.id.btnCoinFlip);

        btnDice.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.diceFragment)
        );

        btnSlots.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.slotsFragment)
        );

        btnCoinFlip.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.coinFlipFragment)
        );

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnDice).setOnClickListener(v ->
                androidx.navigation.Navigation.findNavController(v)
                        .navigate(R.id.action_gamesFragment_to_diceFragment));

        view.findViewById(R.id.btnSlots).setOnClickListener(v ->
                androidx.navigation.Navigation.findNavController(v)
                        .navigate(R.id.action_gamesFragment_to_slotsFragment));

        view.findViewById(R.id.btnCoinFlip).setOnClickListener(v ->
                androidx.navigation.Navigation.findNavController(v)
                        .navigate(R.id.action_gamesFragment_to_coinFlipFragment));
    }
}