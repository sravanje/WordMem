package com.example.wordmem;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;

    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null) {
            this.database.close();
        }
    }


    public List<String> getMeaning(String word) {

        List<String> Meaning = new ArrayList<>();

        Cursor cursor = database.rawQuery("SELECT meaning, source from main_vocab where word is \""+word+"\";", null);
        cursor.moveToFirst();
        if(cursor==null | cursor.getCount()==0){
            Meaning.add("Word not found.");
            Meaning.add("1");

            cursor.close();
            return Meaning;
        }

        Meaning.add(cursor.getString(0));
        Meaning.add(String.valueOf(cursor.getInt(1)));

        cursor.close();
        return Meaning;
    }


    public List<HashMap<String,String>> getMap(String searchkey){
        List<HashMap<String,String>> wordMap = new ArrayList<>();

        String query = "SELECT * FROM main_vocab where time_added is not null order by time_added DESC";
        Cursor cursor = database.rawQuery(query, null);

        while (cursor.moveToNext()){
            HashMap<String, String> resultMap = new HashMap<>();
            if (searchkey.contentEquals("")){
                resultMap.put("Word", cursor.getString(0));
                resultMap.put("Date", "Added on: " + cursor.getString(2).split(" ")[0]);
                wordMap.add(resultMap);
            }

            else{
                String word = cursor.getString(0);
                if (word.contains(searchkey)) {
                    resultMap.put("Word", word);
                    resultMap.put("Date", "Added on: " + cursor.getString(2).split(" ")[0]);
                    wordMap.add(resultMap);
                }
            }
        }

        return wordMap;
    }

    public boolean addWord(String word){

        try {
            String updatequery = "UPDATE main_vocab SET time_added = CURRENT_TIMESTAMP where word is \""+word+"\";";
            database.execSQL(updatequery);
            return true;
        } catch (SQLException e) {
            Log.e("Database Access","Update table error");
            return false;
        }

    }

    public boolean removeWord(String word) {
        try {
            String updatequery = "UPDATE main_vocab SET time_added = null where word is \""+word+"\";";
            database.execSQL(updatequery);
            return true;
        } catch (SQLException e) {
            Log.e("Database Access","Update table error");
            return false;
        }
    }


    public Integer getLength() {
        String query = "SELECT * FROM main_vocab where time_added is not null";
        Cursor cursor = database.rawQuery(query, null);
        return cursor.getCount();
    }


    public void newEntry(String word, String meaning, Integer source){
        try {
            String newentryquery = "INSERT INTO main_vocab (word, meaning, source) VALUES (\""+ word + "\", \"" + meaning.replace("\"","'") + "\", " + source.toString() + ");";
            database.execSQL(newentryquery);
        }
        catch (Exception e){
            Log.e("Database Access insert",e.toString());
        }

    }


    public String date_added(String word) {

        String time_added;
        String query = "SELECT time_added FROM main_vocab where word is \"" + word +"\"";

        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToNext();
        time_added = cursor.getString(0);

        if (time_added==null)
            return "";
        else {
            Log.v("TIME_ADDED: ", time_added);
//            Log.v("DATE_ADDED: ", time_added.split(" ")[0]);
            return time_added.split(" ")[0];
        }

    }


    public List<String> randomWord() {

        List<String> random= new ArrayList<>();

        Cursor cursor = database.rawQuery("SELECT word, meaning, source from main_vocab where time_added is not null ORDER BY RANDOM() LIMIT 1", null);
        cursor.moveToFirst();
        if(cursor==null | cursor.getCount()==0){
            random.add("No words added yet");
            random.add("-1");
            random.add("-1");

            cursor.close();
            return random;
        }

        random.add(cursor.getString(0));
        random.add(cursor.getString(1));
        random.add(String.valueOf(cursor.getInt(2)));

        cursor.close();
        return random;
    }

}
