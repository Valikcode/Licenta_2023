package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    // Firebase auth
    FirebaseAuth firebaseAuth;

    // Views
    TextView textViewProfil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        // Init
        firebaseAuth = FirebaseAuth.getInstance();

        textViewProfil = findViewById(R.id.textViewProfile);
    }

    private  void checkUserStatus(){
        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user != null){
            // User is signed in => stay here
            // Set email of logged in user
            textViewProfil.setText(user.getEmail());
        }else {
            // User is not signed in => go to main activity
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        // Check on start of app
        checkUserStatus();
        super.onStart();
    }

    // Inflate options menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflating menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Handle menu item click

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       // get item it
        int id = item.getItemId();
        if(id == R.id.action_logut){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}