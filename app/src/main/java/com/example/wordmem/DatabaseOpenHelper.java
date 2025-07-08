package com.example.wordmem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DatabaseOpenHelper extends SQLiteAssetHelper {

//    private static final String DATABASE_NAME = "vocabularydotcom.db";
    private static final String DATABASE_NAME = "all_words.db";
//    private static final String DATABASE_NAME = "all_words_sravan_test.db";
    private static final int DATABASE_VERSION = 2; // Increment version for migration

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add score column if it doesn't exist
            try {
                db.execSQL("ALTER TABLE main_vocab ADD COLUMN score REAL DEFAULT 0.5");
            } catch (Exception e) {
                // Column might already exist, ignore error
            }
        }
    }
}
