package com.example.casinosimulator;

public class HistoryItem {
    public final String game;
    public final int bet;
    public final int delta;
    public final long time;

    public HistoryItem(String game, int bet, int delta, long time) {
        this.game = game;
        this.bet = bet;
        this.delta = delta;
        this.time = time;
    }
}