package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    // Views
    EditText editTextEmail, editTextPassword;
    Button buttonRegisterSec;
    TextView textViewHaveAccount;

    // ProgressBar to display while registering user
    ProgressDialog progressDialog;

    // Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");

        // Enable Back Button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Views init
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegisterSec = findViewById(R.id.buttonRegisterSec);
        textViewHaveAccount = findViewById(R.id.textViewAlreadyUser);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");

        // Initialize the FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Register button onClick Handler
        buttonRegisterSec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Input email, pass
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Validate
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    // Set error and focuss to Email EditText
                    editTextEmail.setError("Invalid Email");
                    editTextEmail.setFocusable(true);
                } else if(password.length() < 6){
                    // Set error and focuss to Email EditText
                    editTextPassword.setError("Invalid Password");
                    editTextPassword.setFocusable(true);
                } else {
                    registerUser(email, password); // register the user
                }
            }
        });

        // Already user Handler
        textViewHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String email, String password){
        // Email and Password pattern is valid, show progress dialog and start
        // registering the user
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog and start register activity
                            progressDialog.dismiss();

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
                            hashMap.put("onlineStatus","online"); // will add later ( e.g. edit profile)
                            hashMap.put("typingTo","noOne"); // will add later ( e.g. edit profile)
                            hashMap.put("phone",""); // will add later ( e.g. edit profile)
                            hashMap.put("image",""); // will add later ( e.g. edit profile)
                            hashMap.put("cover",""); // will add later ( e.g. edit profile)
                            hashMap.put("latitude","");
                            hashMap.put("longitude","");
                            // Firebase DataBase instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            // Path to store user data named "Users"
                            DatabaseReference reference = database.getReference("Users");
                            // Put data within hashMap in database
                            reference.child(uid).setValue(hashMap);


                            Toast.makeText(RegisterActivity.this, "Registered...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error, dismiss progress dialog and get and show the error message
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go to the previous activity
        return super.onSupportNavigateUp();
    }
}