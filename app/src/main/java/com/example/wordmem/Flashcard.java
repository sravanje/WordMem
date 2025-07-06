package com.example.wordmem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        Toolbar toolbar = findViewById(R.id.toolb2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Flash Cards");

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
            // add stuff for flash cards filter


            }
        });

    }

    public void newWord(View view){
        TextView front = findViewById(R.id.front);
        TextView back = findViewById(R.id.back);

        EasyFlipView easyFlipView = (EasyFlipView) findViewById(R.id.main_card);
        if (easyFlipView.isBackSide())
            easyFlipView.flipTheView();

        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        List<String> random = databaseAccess.randomWord();
        databaseAccess.close();

        if (random.get(1).contentEquals("-1")){
            front.setText("No words added yet.");
            back.setText("No words added yet. Add words from the home screen to display words that you've learnt as flash cards over here.");
        }
        else {
            front.setText(random.get(0));
            back.setText(random.get(1));
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
