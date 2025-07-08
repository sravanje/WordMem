package com.example.wordmem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import com.google.android.material.navigation.NavigationView;

public class AboutActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolb2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("App Dev");

        drawerLayout = findViewById(R.id.about_drawer);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        TextView tvinfo= findViewById(R.id.tvinfo);
        tvinfo.setText("Sravan Jayati");
        tvinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebsite("https://www.sravanjayati.com");
            }
        });

        TextView tvWebsite = findViewById(R.id.tv_website);
        tvWebsite.setText("www.sravanjayati.com");
        tvWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebsite("https://www.sravanjayati.com");
            }
        });

    }

    private void openWebsite(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open website", Toast.LENGTH_SHORT).show();
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
}
