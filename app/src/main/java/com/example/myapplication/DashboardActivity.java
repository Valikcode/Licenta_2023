package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    // Firebase auth
    FirebaseAuth firebaseAuth;

    // Views
    BottomNavigationView navigationView;


    ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Actionbar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        // Init
        firebaseAuth = FirebaseAuth.getInstance();

        // Bottom Navigation
        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListner);

        // home fragment transaction (default, on start)
        actionBar.setTitle("Home"); // Change actionbar title
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, homeFragment, "");
        fragmentTransaction.commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListner =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    // handle item clicks
                    switch (item.getItemId()){
                        case R.id.nav_home:
                            // home fragment transaction
                            actionBar.setTitle("Home"); // Change actionbar title
                            HomeFragment homeFragment = new HomeFragment();
                            FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction1.replace(R.id.content, homeFragment, "");
                            fragmentTransaction1.commit();
                            return true;
                        case R.id.nav_profile:
                            // profile fragment transaction
                            actionBar.setTitle("Profile"); // Change actionbar title
                            ProfileFragment profileFragment = new ProfileFragment();
                            FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction2.replace(R.id.content, profileFragment, "");
                            fragmentTransaction2.commit();
                            return true;
                        case R.id.nav_users:
                            // users fragment transaction
                            actionBar.setTitle("Users"); // Change actionbar title
                            UsersFragment usersFragment = new UsersFragment();
                            FragmentTransaction fragmentTransaction3 = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction3.replace(R.id.content, usersFragment, "");
                            fragmentTransaction3.commit();
                            return true;
                    }
                    return false;
                }
            };

    private  void checkUserStatus(){
        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user != null){
            // User is signed in => stay here
            // Set email of logged in user
            //textViewProfil.setText(user.getEmail());
        }else {
            // User is not signed in => go to main activity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        // Check on start of app
        checkUserStatus();
        super.onStart();
    }

}