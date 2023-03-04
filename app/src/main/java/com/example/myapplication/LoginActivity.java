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

public class LoginActivity extends AppCompatActivity {

    // Views
    EditText editTextEmail, editTextPassword;
    Button buttonLoginSec;
    TextView textViewNotHaveAccount;

    // Declare an instance of FirebeaseAuth
    private FirebaseAuth mAuth;

    // Progress dialog
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");

        // Enable Back Button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Initialize the FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Init
        editTextEmail = findViewById(R.id.editTextEmailLogin);
        editTextPassword = findViewById(R.id.editTextPasswordLogin);
        buttonLoginSec = findViewById(R.id.buttonLoginSec);
        textViewNotHaveAccount = findViewById(R.id.textViewNotUser);

        // Login button click Handler
        buttonLoginSec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Input data
                String email = editTextEmail.getText().toString().trim();
                String pass = editTextPassword.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    // Invalid email pattern set error
                    editTextEmail.setError("Invalid Email");
                    editTextEmail.setFocusable(true);
                } else{
                    // Valid email pattern
                    loginUser(email, pass);
                }
            }
        });

        // Don`t have an accout textview click Handler
        textViewNotHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });

        // Init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(("Logging In...."));

    }

    private void loginUser(String email, String pass) {
        // Show progress dialog
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Dismiss progress dialog
                            progressDialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            // User is logged in, so start ProfileActivity
                            startActivity(new Intent(LoginActivity.this,ProfileActivity.class));
                            finish();
                        } else {
                            // Dismiss progress dialog
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Dismiss progress dialog
                        progressDialog.dismiss();
                        // Error, get and show error msg
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go to the previous activity
        return super.onSupportNavigateUp();
    }
}