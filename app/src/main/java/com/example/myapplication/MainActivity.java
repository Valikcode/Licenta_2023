package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient GoogleSignInClient;

    // Declare an instance of FirebeaseAuth
    private FirebaseAuth mAuth;

    // Views
    TextView mRegisterButton;
    Button mLoginButton, mGoogleLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set FullScreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ascund Action Bar de pe Splah Screen
        getSupportActionBar().hide();

        // Before mAuth
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient = GoogleSignIn.getClient(this,gso);


        // Initialize the FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Init Views
        mRegisterButton = findViewById(R.id.textViewRegister);
        mLoginButton = findViewById(R.id.buttonLogin);
        mGoogleLoginButton = findViewById(R.id.buttonLoginGoogle);

        // Register button onClick Handler
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start RegisterActivity
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
            }
        });

        // Login button onClick Handler
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start LoginActivity
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });

        // Google Login button onClick Handler
        mGoogleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Begin google login process
                Intent signInIntent = GoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent,RC_SIGN_IN);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSingInApi.getSignInIntent(...);
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e){
                // Google sign in failed, update UI appropriately
                Toast.makeText(this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Get user email and uid from auth
                            String email = user.getEmail();
                            String uid = user.getUid();
                            // When user is registred store user info in firebase realtime database
                            // too using HashMap
                            HashMap<Object, String> hashMap = new HashMap<>();
                            // Put info in hashMap
                            hashMap.put("email",email);
                            hashMap.put("uid",uid);
                            hashMap.put("name",""); // will add later ( e.g. edit profile)
                            hashMap.put("phone",""); // will add later ( e.g. edit profile)
                            hashMap.put("image",""); // will add later ( e.g. edit profile)
                            // Firebase DataBase instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            // Path to store user data named "Users"
                            DatabaseReference reference = database.getReference("Users");
                            // Put data within hashMap in database
                            reference.child(uid).setValue(hashMap);

                            // Show user email in toast
                            Toast.makeText(MainActivity.this, "Login Succesfull: " +user.getEmail(), Toast.LENGTH_SHORT).show();

                            // User is logged in, so start ProfileActivity
                            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Get an show error message
                        Toast.makeText(MainActivity.this, ""+ e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}