package com.example.wordmem;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class WordAnalysisActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    Integer backpress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_analysis);


        Toolbar toolbar = findViewById(R.id.toolb2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Word Mem Analysis");
//        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawerLayout = findViewById(R.id.analysis_drawer);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();


        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        Integer words_learnt = databaseAccess.getLength();
        databaseAccess.close();

        TextView tvinfo = findViewById(R.id.tvinfo);
        tvinfo.setText(words_learnt.toString() + " words learnt");

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


        switch (menuItem.getItemId()){
            case R.id.home:
                Intent homeIntent = new Intent(this, MainActivity.class);
                this.startActivity(homeIntent);
                break;

            case R.id.analysis:
                Toast.makeText(this,"This section is yet to be added", Toast.LENGTH_SHORT).show();
                Intent anIntent = new Intent(this, WordAnalysisActivity.class);
                this.startActivity(anIntent);
                break;

            case R.id.flash_cards:
                Intent flashcardIntent = new Intent(this, Flashcard.class);
                this.startActivity(flashcardIntent );
                break;

            case R.id.about_app:
                Toast.makeText(this,"This section is yet to be added", Toast.LENGTH_SHORT).show();
                break;

            case R.id.about_developer:
                Toast.makeText(this,"This section is yet to be added", Toast.LENGTH_SHORT).show();
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
