package com.example.wordmem;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DatabaseOpenHelper extends SQLiteAssetHelper {

//    private static final String DATABASE_NAME = "vocabularydotcom.db";
    private static final String DATABASE_NAME = "all_words.db";
//    private static final String DATABASE_NAME = "all_words_sravan_test.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
