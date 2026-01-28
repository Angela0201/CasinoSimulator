package com.example.casinosimulator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class CasinoDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "casino.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_STATE = "state";
    private static final String COL_STATE_KEY = "stateKey";
    private static final String COL_INT_VALUE = "intValue";

    private static final String TABLE_HISTORY = "history";
    private static final String COL_ID = "id";
    private static final String COL_GAME = "game";
    private static final String COL_BET = "bet";
    private static final String COL_DELTA = "delta";
    private static final String COL_CREATED_AT = "createdAt";

    private static final String KEY_BALANCE = "balance";
    private static final int START_BALANCE = 1000;

    public CasinoDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_STATE + " (" +
                        COL_STATE_KEY + " TEXT PRIMARY KEY, " +
                        COL_INT_VALUE + " INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_HISTORY + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_GAME + " TEXT NOT NULL, " +
                        COL_BET + " INTEGER NOT NULL, " +
                        COL_DELTA + " INTEGER NOT NULL, " +
                        COL_CREATED_AT + " INTEGER NOT NULL" +
                        ")"
        );

        ContentValues cv = new ContentValues();
        cv.put(COL_STATE_KEY, KEY_BALANCE);
        cv.put(COL_INT_VALUE, START_BALANCE);
        db.insert(TABLE_STATE, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATE);
        onCreate(db);
    }

    public int getBalance() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " + COL_INT_VALUE +
                        " FROM " + TABLE_STATE +
                        " WHERE " + COL_STATE_KEY + "=?",
                new String[]{KEY_BALANCE}
        );

        if (c.moveToFirst()) {
            int balance = c.getInt(0);
            c.close();
            return balance;
        }

        c.close();

        SQLiteDatabase wdb = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_STATE_KEY, KEY_BALANCE);
        cv.put(COL_INT_VALUE, START_BALANCE);
        wdb.insert(TABLE_STATE, null, cv);

        return START_BALANCE;
    }

    public void setBalance(int value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_INT_VALUE, value);
        int updated = db.update(TABLE_STATE, cv, COL_STATE_KEY + "=?", new String[]{KEY_BALANCE});
        if (updated == 0) {
            cv.put(COL_STATE_KEY, KEY_BALANCE);
            db.insert(TABLE_STATE, null, cv);
        }
    }

    public void addHistory(String game, int bet, int delta, long createdAt) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_GAME, game);
        cv.put(COL_BET, bet);
        cv.put(COL_DELTA, delta);
        cv.put(COL_CREATED_AT, createdAt);
        db.insert(TABLE_HISTORY, null, cv);
    }

    public ArrayList<HistoryItem> getHistory() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " + COL_GAME + ", " + COL_BET + ", " + COL_DELTA + ", " + COL_CREATED_AT +
                        " FROM " + TABLE_HISTORY +
                        " ORDER BY " + COL_CREATED_AT + " DESC",
                null
        );

        ArrayList<HistoryItem> list = new ArrayList<>();

        while (c.moveToNext()) {
            String game = c.getString(0);
            int bet = c.getInt(1);
            int delta = c.getInt(2);
            long time = c.getLong(3);
            list.add(new HistoryItem(game, bet, delta, time));
        }

        c.close();
        return list;
    }

    public void resetBalance() {
        setBalance(START_BALANCE);
    }

    public void clearHistory() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_HISTORY, null, null);
    }

    public static class GameStats {
        public int earned;
        public int lost;

        public GameStats(int earned, int lost) {
            this.earned = earned;
            this.lost = lost;
        }
    }

    public GameStats getStatsForGame(String game) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " +
                        "SUM(CASE WHEN " + COL_DELTA + " > 0 THEN " + COL_DELTA + " ELSE 0 END) AS earned, " +
                        "SUM(CASE WHEN " + COL_DELTA + " < 0 THEN -" + COL_DELTA + " ELSE 0 END) AS lost " +
                        "FROM " + TABLE_HISTORY + " WHERE " + COL_GAME + "=?",
                new String[]{game}
        );

        int earned = 0;
        int lost = 0;

        if (c.moveToFirst()) {
            if (!c.isNull(0)) earned = c.getInt(0);
            if (!c.isNull(1)) lost = c.getInt(1);
        }

        c.close();
        return new GameStats(earned, lost);
    }
}