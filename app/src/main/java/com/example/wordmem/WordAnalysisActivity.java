package com.example.wordmem;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.List;

public class WordAnalysisActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    ListView wordsList;
    TextView tvInfo, tvAverageScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_analysis);

        Toolbar toolbar = findViewById(R.id.toolb2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Mem Analysis");

        drawerLayout = findViewById(R.id.analysis_drawer);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        // Initialize views
        tvInfo = findViewById(R.id.tvinfo);
        tvAverageScore = findViewById(R.id.tv_average_score);
        wordsList = findViewById(R.id.words_list);

        tvAverageScore.setText("Average Score: Calculating...");

        // Load and display analysis data
        loadAnalysisData();
    }

    private void loadAnalysisData() {
        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        
        // Get total words count
        Integer wordsLearnt = databaseAccess.getLength();
        
        // Get words with scores
        List<HashMap<String, String>> wordsWithScores = databaseAccess.getWordsWithScores();
        
        databaseAccess.close();

        // Update summary information
        tvInfo.setText(wordsLearnt.toString() + " words learned");
        
        // Calculate and display average score
        if (!wordsWithScores.isEmpty()) {
            double totalScore = 0.0;
            for (HashMap<String, String> word : wordsWithScores) {
                totalScore += Double.parseDouble(word.get("score"));
            }
            double averageScore = totalScore / wordsWithScores.size();
            tvAverageScore.setText(String.format("Average Score: %.2f / 1.00", 
                                               averageScore, averageScore * 100));
        } else {
            tvAverageScore.setText("Average Score: No words added yet");
        }

        // Set up the list adapter
        if (!wordsWithScores.isEmpty()) {
            SimpleAdapter adapter = new SimpleAdapter(
                this,
                wordsWithScores,
                R.layout.word_analysis_item,
                new String[]{"word", "category", "score", "source"},
                new int[]{R.id.word_text, R.id.category_text, R.id.score_text, R.id.percentage_text}
            );
            wordsList.setAdapter(adapter);
        } else {
            // Show empty state
            tvInfo.setText("No words added yet");
            tvAverageScore.setText("Start learning words (Mem Cards) to see your progress!");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.home:
                Intent homeIntent = new Intent(this, MainActivity.class);
                this.startActivity(homeIntent);
                break;

            case R.id.analysis:
                // Already in analysis, refresh data
                loadAnalysisData();
                break;

            case R.id.flash_cards:
                Intent flashcardIntent = new Intent(this, Flashcard.class);
                this.startActivity(flashcardIntent );
                break;

            case R.id.about_developer:
                // Toast.makeText(this,"This section is yet to be added", Toast.LENGTH_SHORT).show();
                Intent devIntent = new Intent(this, AboutActivity.class);
                this.startActivity(devIntent);
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(this, MainActivity.class);
        this.startActivity(homeIntent);
    }
}
