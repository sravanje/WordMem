package com.example.wordmem;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "Word_List";
    public static final String COL1 = "ID";
//    public static final String COL2 = "WORD";
//    public static final String COL3 = "MEANING";
    public static final String COL2 = "word";
    public static final String COL3 = "meaning";
    public static final String COL4= "source";
    public static final String COL5 = "old_entry";

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createtable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COL2 + " TEXT, " + COL3 + "TEXT, " + COL4 + " INTEGER DEFAULT 0, " + COL5 + " INTEGER DEFAULT 0)";
        db.execSQL(createtable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }

    public boolean addData(String word, String meaning){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, word);
        contentValues.put(COL3, meaning);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if (result==-1){
            Log.v(TABLE_NAME, "Failed");
            return false;
        }
        else {
            Log.v(TABLE_NAME, "Success");
            return true;
        }
    }
}
