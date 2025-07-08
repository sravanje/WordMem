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

        // Order by source priority: Own Definition (1) -> Dictionary API (2) -> Vocabulary.com (0)
        Cursor cursor = database.rawQuery("SELECT meaning, source from main_vocab where word is \""+word+"\" ORDER BY CASE source WHEN 1 THEN 1 WHEN 0 THEN 2 WHEN 2 THEN 3 END;", null);
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

    public List<String> getMeaningBySource(String word, int sourceFilter) {
        List<String> Meaning = new ArrayList<>();

        Cursor cursor = database.rawQuery("SELECT meaning, source from main_vocab where word is \""+word+"\" AND source = "+sourceFilter+";", null);
        cursor.moveToFirst();
        if(cursor==null | cursor.getCount()==0){
            Meaning.add("Word not found in selected source.");
            Meaning.add(String.valueOf(sourceFilter));

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
    }    public List<HashMap<String,String>> getMapBySource(String searchkey, int sourceFilter){
        List<HashMap<String,String>> wordMap = new ArrayList<>();

        String query;
        if (sourceFilter == -1) {
            // Show all sources
            query = "SELECT word, meaning, time_added, source FROM main_vocab where time_added is not null order by time_added DESC";
        } else {
            // Filter by specific source
            query = "SELECT word, meaning, time_added, source FROM main_vocab where time_added is not null AND source = " + sourceFilter + " order by time_added DESC";
        }
        
        Cursor cursor = database.rawQuery(query, null);

        while (cursor.moveToNext()){
            HashMap<String, String> resultMap = new HashMap<>();
            String word = cursor.getString(0);
            String timeAdded = cursor.getString(2);
            int source = cursor.getInt(3);
            
            if (searchkey.contentEquals("")){
                resultMap.put("Word", word);
                String sourceText = getSourceDisplayText(source);
                resultMap.put("Date", "Added on: " + timeAdded.split(" ")[0] + " (" + sourceText + ")");
                resultMap.put("Source", String.valueOf(source)); // Store source for later retrieval
                wordMap.add(resultMap);
            }
            else{
                if (word.contains(searchkey)) {
                    resultMap.put("Word", word);
                    String sourceText = getSourceDisplayText(source);
                    resultMap.put("Date", "Added on: " + timeAdded.split(" ")[0] + " (" + sourceText + ")");
                    resultMap.put("Source", String.valueOf(source)); // Store source for later retrieval
                    wordMap.add(resultMap);
                }
            }
        }
        cursor.close();
        return wordMap;
    }

    private String getSourceDisplayText(int source) {
        switch (source) {
            case 0: return "Vocabulary.com";
            case 1: return "Own Definition";
            case 2: return "Dictionary API";
            default: return "Unknown";
        }
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

    public boolean addWordWithSource(String word, int source){
        try {
            String updatequery = "UPDATE main_vocab SET time_added = CURRENT_TIMESTAMP where word is \""+word+"\" AND source = " + source + ";";
            database.execSQL(updatequery);
            return true;
        } catch (SQLException e) {
            Log.e("Database Access","Update table with source error");
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

    public boolean removeWordWithSource(String word, int source) {
        try {
            String updatequery = "UPDATE main_vocab SET time_added = null where word is \""+word+"\" AND source = " + source + ";";
            database.execSQL(updatequery);
            return true;
        } catch (SQLException e) {
            Log.e("Database Access","Remove word with source error");
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

    public String date_added_with_source(String word, int source) {
        String time_added;
        String query = "SELECT time_added FROM main_vocab where word is \"" + word +"\" AND source = " + source;

        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToNext()) {
            time_added = cursor.getString(0);
            cursor.close();
            
            if (time_added==null)
                return "";
            else {
                Log.v("TIME_ADDED: ", time_added);
                return time_added.split(" ")[0];
            }
        } else {
            cursor.close();
            return "";
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

    // Score management methods
    public boolean updateWordScore(String word, double scoreChange) {
        try {
            // First, get current score or set default to 0.5 if null
            String getScoreQuery = "SELECT score FROM main_vocab WHERE word = \"" + word + "\"";
            Cursor cursor = database.rawQuery(getScoreQuery, null);
            
            double currentScore = 0.5; // Default score
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                currentScore = cursor.getDouble(0);
            }
            cursor.close();
            
            // Calculate new score with bounds checking
            double newScore = Math.max(0.0, Math.min(1.0, currentScore + scoreChange));
            
            String updateQuery = "UPDATE main_vocab SET score = " + newScore + " WHERE word = \"" + word + "\"";
            database.execSQL(updateQuery);
            return true;
        } catch (SQLException e) {
            Log.e("Database Access", "Update score error: " + e.getMessage());
            return false;
        }
    }
    
    public double getWordScore(String word) {
        try {
            String query = "SELECT score FROM main_vocab WHERE word = \"" + word + "\"";
            Cursor cursor = database.rawQuery(query, null);
            
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                double score = cursor.getDouble(0);
                cursor.close();
                return score;
            }
            cursor.close();
            return 0.5; // Default score if not found or null
        } catch (Exception e) {
            Log.e("Database Access", "Get score error: " + e.getMessage());
            return 0.5;
        }
    }

    public void initializeScoreColumn() {
        try {
            // Add score column if it doesn't exist
            database.execSQL("ALTER TABLE main_vocab ADD COLUMN score REAL DEFAULT 0.5");
        } catch (Exception e) {
            // Column already exists, update null values to default
            try {
                database.execSQL("UPDATE main_vocab SET score = 0.5 WHERE score IS NULL");
            } catch (Exception ex) {
                Log.e("Database Access", "Error initializing scores: " + ex.getMessage());
            }
        }
    }

    public List<String> weightedRandomWord() {
        List<String> result = new ArrayList<>();
        
        try {
            // Get all words with their scores (lower scores = higher priority)
            String query = "SELECT word, meaning, source, COALESCE(score, 0.5) as word_score " +
                          "FROM main_vocab WHERE time_added IS NOT NULL";
            Cursor cursor = database.rawQuery(query, null);
            
            if (cursor == null || cursor.getCount() == 0) {
                result.add("No words added yet");
                result.add("-1");
                result.add("-1");
                cursor.close();
                return result;
            }
            
            // Collect all words with their weights
            List<String> words = new ArrayList<>();
            List<String> meanings = new ArrayList<>();
            List<String> sources = new ArrayList<>();
            List<Double> weights = new ArrayList<>();
            double totalWeight = 0.0;
            
            while (cursor.moveToNext()) {
                String word = cursor.getString(0);
                String meaning = cursor.getString(1);
                String source = cursor.getString(2);
                double score = cursor.getDouble(3);
                
                // Calculate weight: lower scores get higher weights
                // Score range is 0-1, so weight = (1 - score) + 0.1 (minimum weight)
                double weight = (1.0 - score) + 0.1;
                
                words.add(word);
                meanings.add(meaning);
                sources.add(source);
                weights.add(weight);
                totalWeight += weight;
            }
            cursor.close();
            
            // Select random word based on weights
            double randomValue = Math.random() * totalWeight;
            double currentWeight = 0.0;
            
            for (int i = 0; i < words.size(); i++) {
                currentWeight += weights.get(i);
                if (randomValue <= currentWeight) {
                    result.add(words.get(i));
                    result.add(meanings.get(i));
                    result.add(sources.get(i));
                    return result;
                }
            }
            
            // Fallback: return last word if something goes wrong
            if (!words.isEmpty()) {
                int lastIndex = words.size() - 1;
                result.add(words.get(lastIndex));
                result.add(meanings.get(lastIndex));
                result.add(sources.get(lastIndex));
            }
            
        } catch (Exception e) {
            Log.e("Database Access", "Weighted random word error: " + e.getMessage());
            // Fallback to regular random
            return randomWord();
        }
        
        return result;
    }

    public List<HashMap<String, String>> getWordsWithScores() {
        List<HashMap<String, String>> wordsList = new ArrayList<>();
        
        try {
            String query = "SELECT word, COALESCE(score, 0.5) as word_score, source " +
                          "FROM main_vocab WHERE time_added IS NOT NULL " +
                          "ORDER BY word_score DESC, word ASC";
            Cursor cursor = database.rawQuery(query, null);
            
            while (cursor.moveToNext()) {
                HashMap<String, String> wordData = new HashMap<>();
                String word = cursor.getString(0);
                double score = cursor.getDouble(1);
                int source = cursor.getInt(2);
                
                wordData.put("word", word);
                wordData.put("score", String.format("%.2f", score));
                
                // Add source information
                switch (source) {
                    case 0:
                        wordData.put("source", "Vocabulary.com");
                        break;
                    case 1:
                        wordData.put("source", "Own Definition");
                        break;
                    case 2:
                        wordData.put("source", "Dictionary API");
                        break;
                    default:
                        wordData.put("source", "Unknown");
                        break;
                }
                
                // Add performance category
                if (score >= 0.8) {
                    wordData.put("category", "Excellent");
                } else if (score >= 0.6) {
                    wordData.put("category", "Good");
                } else if (score >= 0.4) {
                    wordData.put("category", "Fair");
                } else {
                    wordData.put("category", "Needs Practice");
                }
                
                wordsList.add(wordData);
            }
            cursor.close();
            
        } catch (Exception e) {
            Log.e("Database Access", "Error getting words with scores: " + e.getMessage());
        }
        
        return wordsList;
    }

    public List<String> getFilteredRandomWord(double minScore, double maxScore) {
        List<String> result = new ArrayList<>();
        
        try {
            // Get words within score range - use score directly since we've initialized all scores
            String query;
            String[] queryParams;
            
            if (minScore == 0.4 && maxScore == 0.6) {
                // Special case for "Fair" to handle boundary conditions
                query = "SELECT word, meaning, score as word_score " +
                       "FROM main_vocab WHERE time_added IS NOT NULL " +
                       "AND score >= ? AND score < ?";
                queryParams = new String[]{"0.4", "0.6"};
            } else if (minScore == 0.0 && maxScore == 0.4) {
                // "Needs Practice"
                query = "SELECT word, meaning, score as word_score " +
                       "FROM main_vocab WHERE time_added IS NOT NULL " +
                       "AND score < ?";
                queryParams = new String[]{"0.4"};
            } else if (minScore == 0.6 && maxScore == 0.8) {
                // "Good"
                query = "SELECT word, meaning, score as word_score " +
                       "FROM main_vocab WHERE time_added IS NOT NULL " +
                       "AND score >= ? AND score < ?";
                queryParams = new String[]{"0.6", "0.8"};
            } else if (minScore == 0.8 && maxScore == 1.0) {
                // "Excellent"
                query = "SELECT word, meaning, score as word_score " +
                       "FROM main_vocab WHERE time_added IS NOT NULL " +
                       "AND score >= ?";
                queryParams = new String[]{"0.8"};
            } else {
                // Generic case
                query = "SELECT word, meaning, score as word_score " +
                       "FROM main_vocab WHERE time_added IS NOT NULL " +
                       "AND score >= ? AND score <= ?";
                queryParams = new String[]{String.valueOf(minScore), String.valueOf(maxScore)};
            }
            
            Cursor cursor = database.rawQuery(query, queryParams);
            
            if (cursor.getCount() == 0) {
                result.add("No words found");
                result.add("-1");
                cursor.close();
                return result;
            }
            
            // Create weighted list for random selection
            List<String> words = new ArrayList<>();
            List<String> meanings = new ArrayList<>();
            List<Double> weights = new ArrayList<>();
            double totalWeight = 0;
            
            while (cursor.moveToNext()) {
                String word = cursor.getString(0);
                String meaning = cursor.getString(1);
                double score = cursor.getDouble(2);
                
                // Lower scores get higher weights (more likely to be selected)
                double weight = Math.max(0.1, 1.0 - score);
                
                words.add(word);
                meanings.add(meaning);
                weights.add(weight);
                totalWeight += weight;
            }
            cursor.close();
            
            // Select weighted random word
            double randomValue = Math.random() * totalWeight;
            double cumulativeWeight = 0;
            
            for (int i = 0; i < weights.size(); i++) {
                cumulativeWeight += weights.get(i);
                if (randomValue <= cumulativeWeight) {
                    result.add(words.get(i));
                    result.add(meanings.get(i));
                    return result;
                }
            }
            
            // Fallback: return first word
            if (!words.isEmpty()) {
                result.add(words.get(0));
                result.add(meanings.get(0));
                return result;
            }
            
        } catch (Exception e) {
            Log.e("Database Access", "Error getting filtered random word: " + e.getMessage());
        }
        
        // Fallback: no words found
        result.add("No words found");
        result.add("-1");
        return result;
    }

    public List<String> getFilteredRandomWordByCategory(String category) {
        double minScore, maxScore;
        
        switch (category.toLowerCase()) {
            case "needs practice":
                minScore = 0.0;
                maxScore = 0.4;
                break;
            case "fair":
                minScore = 0.4;
                maxScore = 0.6;
                break;
            case "good":
                minScore = 0.6;
                maxScore = 0.8;
                break;
            case "excellent":
                minScore = 0.8;
                maxScore = 1.0;
                break;
            default: // "all words"
                return weightedRandomWord();
        }
        
        return getFilteredRandomWord(minScore, maxScore);
    }

    public int getFilteredWordCount(double minScore, double maxScore) {
        try {
            String query;
            String[] queryParams;
            
            if (minScore == 0.4 && maxScore == 0.6) {
                // Special case for "Fair" to handle boundary conditions
                query = "SELECT COUNT(*) FROM main_vocab WHERE time_added IS NOT NULL " +
                       "AND score >= ? AND score < ?";
                queryParams = new String[]{"0.4", "0.6"};
            } else if (minScore == 0.0 && maxScore == 0.4) {
                // "Needs Practice"
                query = "SELECT COUNT(*) FROM main_vocab WHERE time_added IS NOT NULL " +
                       "AND score < ?";
                queryParams = new String[]{"0.4"};
            } else if (minScore == 0.6 && maxScore == 0.8) {
                // "Good"
                query = "SELECT COUNT(*) FROM main_vocab WHERE time_added IS NOT NULL " +
                       "AND score >= ? AND score < ?";
                queryParams = new String[]{"0.6", "0.8"};
            } else if (minScore == 0.8 && maxScore == 1.0) {
                // "Excellent"
                query = "SELECT COUNT(*) FROM main_vocab WHERE time_added IS NOT NULL " +
                       "AND score >= ?";
                queryParams = new String[]{"0.8"};
            } else {
                // Generic case
                query = "SELECT COUNT(*) FROM main_vocab WHERE time_added IS NOT NULL " +
                       "AND score >= ? AND score <= ?";
                queryParams = new String[]{String.valueOf(minScore), String.valueOf(maxScore)};
            }
            
            Cursor cursor = database.rawQuery(query, queryParams);
            
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                cursor.close();
                return count;
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("Database Access", "Error getting filtered word count: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }



}
