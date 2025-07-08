package com.example.wordmem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import java.util.List;

public class Flashcard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    private String currentWord = ""; // Track current word for scoring
    private String currentDefinition = ""; // Store current word definition
    private FilterManager.FilterSettings currentFilter = new FilterManager.FilterSettings();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        Toolbar toolbar = findViewById(R.id.toolb2);
        setSupportActionBar(toolbar);
        updateTitle(); // Set initial title

        drawerLayout = findViewById(R.id.flashcard_drawer);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        newWord(navigationView);

        FloatingActionButton fab = findViewById(R.id.fab_filter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFilterDialog();
            }
        });

        // Set up scoring buttons
        setupScoringButtons();

        // Initialize score column in database
        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        databaseAccess.initializeScoreColumn();
        databaseAccess.close();

    }

    public void newWord(View view){
        TextView front = findViewById(R.id.front);
        TextView back = findViewById(R.id.back);

        EasyFlipView easyFlipView = (EasyFlipView) findViewById(R.id.main_card);

        if (!easyFlipView.isBackSide()) {
            easyFlipView.flipTheView();
        }

        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        
        List<String> random;
        if (currentFilter.isFiltered) {
            Log.d("Flashcard", "Filter is active - filterType: " + currentFilter.filterType + ", category: " + currentFilter.category);
            if (currentFilter.filterType.equals("category")) {
                Log.d("Flashcard", "Calling getFilteredRandomWordByCategory with: " + currentFilter.category);
                random = databaseAccess.getFilteredRandomWordByCategory(currentFilter.category);
            } else {
                Log.d("Flashcard", "Calling getFilteredRandomWord with: " + currentFilter.minScore + " to " + currentFilter.maxScore);
                random = databaseAccess.getFilteredRandomWord(currentFilter.minScore, currentFilter.maxScore);
            }
        } else {
            Log.d("Flashcard", "No filter active, using weightedRandomWord");
            random = databaseAccess.weightedRandomWord(); // Use weighted selection
        }
        
        databaseAccess.close();
        
        Log.d("Flashcard", "Random word result: " + random.toString());

        if (random.get(1).contentEquals("-1")){
            front.setText("No words found.");
            back.setText("No words match the current filter. Try adjusting your filter settings.");
            currentWord = ""; // No current word
            currentDefinition = ""; // No definition
        }
        else {
            currentWord = random.get(0); // Store current word for scoring
            currentDefinition = random.get(1); // Store definition
            front.setText(random.get(0));
            back.setText(currentDefinition);
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
                // Toast.makeText(this,"This section is yet to be added", Toast.LENGTH_SHORT).show();
                Intent anIntent = new Intent(this, WordAnalysisActivity.class);
                this.startActivity(anIntent);
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

    private void setupScoringButtons() {
        Button btnDontKnow = findViewById(R.id.btn_dont_know);
        Button btnNeutral = findViewById(R.id.btn_neutral);
        Button btnKnowIt = findViewById(R.id.btn_know_it);

        btnDontKnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateScore(-0.2); // Decrease score significantly
            }
        });

        btnNeutral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateScore(-0.05); // Slight decrease towards neutral
            }
        });

        btnKnowIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateScore(0.15); // Increase score
            }
        });
    }

    private void updateScore(double scoreChange) {
        if (!currentWord.isEmpty()) {
            final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
            databaseAccess.open();
            boolean success = databaseAccess.updateWordScore(currentWord, scoreChange);
            databaseAccess.close();

            // if (success) {
            //     // Show feedback to user
            //     String feedback = "";
            //     if (scoreChange > 0) {
            //         feedback = "Great! Score improved.";
            //     } else if (scoreChange < -0.1) {
            //         feedback = "Keep practicing this word.";
            //     } else {
            //         feedback = "Score updated.";
            //     }
            //     Toast.makeText(this, feedback, Toast.LENGTH_SHORT).show();
            // }
        }
        
        // Load next word
        newWord(findViewById(android.R.id.content));
    }

    private void showFilterDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_filter_flashcards);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Get dialog elements
        RadioGroup rgFilterCategory = dialog.findViewById(R.id.rg_filter_category);
        RadioButton rbAllWords = dialog.findViewById(R.id.rb_all_words);
        RadioButton rbNeedsPractice = dialog.findViewById(R.id.rb_needs_practice);
        RadioButton rbFair = dialog.findViewById(R.id.rb_fair);
        RadioButton rbGood = dialog.findViewById(R.id.rb_good);
        RadioButton rbExcellent = dialog.findViewById(R.id.rb_excellent);

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnApplyFilter = dialog.findViewById(R.id.btn_apply_filter);

        // Set current filter state
        if (currentFilter.isFiltered && currentFilter.filterType.equals("category")) {
            switch (currentFilter.category.toLowerCase()) {
                case "needs practice":
                    rbNeedsPractice.setChecked(true);
                    break;
                case "fair":
                    rbFair.setChecked(true);
                    break;
                case "good":
                    rbGood.setChecked(true);
                    break;
                case "excellent":
                    rbExcellent.setChecked(true);
                    break;
                default:
                    rbAllWords.setChecked(true);
            }
        } else {
            rbAllWords.setChecked(true);
        }

        // Handle radio button changes (simplified)
        rgFilterCategory.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Radio button selection handles the filtering logic
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilter(dialog, rgFilterCategory);
            }
        });

        dialog.show();
    }

    private void applyFilter(Dialog dialog, RadioGroup rgFilterCategory) {
        int selectedId = rgFilterCategory.getCheckedRadioButtonId();
        
        if (selectedId == R.id.rb_all_words) {
            // Clear filter
            currentFilter.reset();
            updateTitle();
            Toast.makeText(this, "Filter cleared - showing all words", Toast.LENGTH_SHORT).show();
        } else {
            // Apply filter
            currentFilter.isFiltered = true;
            currentFilter.filterType = "category";

            if (selectedId == R.id.rb_needs_practice) {
                currentFilter.category = "Needs Practice";
                currentFilter.minScore = 0.0;
                currentFilter.maxScore = 0.4;
            } else if (selectedId == R.id.rb_fair) {
                currentFilter.category = "Fair";
                currentFilter.minScore = 0.4;
                currentFilter.maxScore = 0.6;
            } else if (selectedId == R.id.rb_good) {
                currentFilter.category = "Good";
                currentFilter.minScore = 0.6;
                currentFilter.maxScore = 0.8;
            } else if (selectedId == R.id.rb_excellent) {
                currentFilter.category = "Excellent";
                currentFilter.minScore = 0.8;
                currentFilter.maxScore = 1.0;
            }

            // Check how many words match the filter
            final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
            databaseAccess.open();
            int wordCount = databaseAccess.getFilteredWordCount(currentFilter.minScore, currentFilter.maxScore);
            databaseAccess.close();

            if (wordCount == 0) {
                Toast.makeText(this, "No words match this filter. Try a different range.", Toast.LENGTH_SHORT).show();
                return;
            }

            updateTitle();
            Toast.makeText(this, String.format("Filter applied - %d words available", wordCount), Toast.LENGTH_SHORT).show();
        }

        // Load a new word with the filter
        newWord(findViewById(android.R.id.content));
        dialog.dismiss();
    }

    private void updateTitle() {
        String title = "Mem Cards";
        if (currentFilter.isFiltered) {
            title += " - " + currentFilter.getDisplayText();
        }
        getSupportActionBar().setTitle(title);
    }
}
