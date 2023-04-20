package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.myapplication.fragments.ChatListFragment;
import com.example.myapplication.fragments.HomeFragment;
import com.example.myapplication.fragments.ProfileFragment;
import com.example.myapplication.fragments.UsersFragment;
import com.example.myapplication.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

public class DashboardActivity extends AppCompatActivity {

    // Firebase auth
    FirebaseAuth firebaseAuth;

    // Views
    BottomNavigationView navigationView;

    ActionBar actionBar;

    String mUID;

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

        checkUserStatus();

        // Update token
        updateToken(String.valueOf(FirebaseMessaging.getInstance().getToken()));
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        reference.child(mUID).setValue(mToken);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_profile){
            // profile fragment transaction
            actionBar.setTitle("Profile"); // Change actionbar title
            ProfileFragment profileFragment = new ProfileFragment();
            FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
            fragmentTransaction2.replace(R.id.content, profileFragment, "");
            fragmentTransaction2.commit();
        }
        return super.onOptionsItemSelected(item);
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
                        case R.id.nav_users:
                            // users fragment transaction
                            actionBar.setTitle("Users"); // Change actionbar title
                            UsersFragment usersFragment = new UsersFragment();
                            FragmentTransaction fragmentTransaction3 = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction3.replace(R.id.content, usersFragment, "");
                            fragmentTransaction3.commit();
                            return true;
                        case R.id.nav_chat:
                            // users fragment transaction
                            actionBar.setTitle("Chats"); // Change actionbar title
                            ChatListFragment chatListFragment = new ChatListFragment();
                            FragmentTransaction fragmentTransaction4 = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction4.replace(R.id.content, chatListFragment, "");
                            fragmentTransaction4.commit();
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
            mUID = user.getUid();

            // Save the uid of currently signed in user in shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();
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