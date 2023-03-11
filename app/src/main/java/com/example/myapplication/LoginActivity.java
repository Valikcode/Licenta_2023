package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    TextView textViewNotHaveAccount, textViewRecoverPass;

    // Declare an instance of FirebeaseAuth
    private FirebaseAuth mAuth;

    // Progress dialog
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set FullScreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        // Ascund Action Bar de pe Splah Screen
        //getSupportActionBar().hide();
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
        textViewRecoverPass = findViewById(R.id.textViewForgotPass);

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

        // Recover password textView click Handler
        textViewRecoverPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDialog();
            }
        });

        // Init progress dialog
        progressDialog = new ProgressDialog(this);
    }

    // Recover pass dialog
    private void showRecoverPasswordDialog() {
        // AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        // set layout linear layout
        LinearLayout linearLayout = new LinearLayout(this);

        // Views to set in the dialog
        EditText editTextEmail = new EditText(this);
        editTextEmail.setHint("E-Mail");
        editTextEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // Sets the min witdh of a EditView to fit a text of n 'M' letters regardless
        // of the actual text extensions and text sieze.
        editTextEmail.setMaxEms(16);

        linearLayout.addView(editTextEmail);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        // Buttons recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // input email
                String email = editTextEmail.getText().toString().trim();
                beginRecovery(email);
            }
        });

        // Buttons cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // dismiss dialog
                dialogInterface.dismiss();
            }
        });

        // show dialog
        builder.create().show();
    }

    // Pass recovery method
    private void beginRecovery(String email) {
        // Show progress dialog
        progressDialog.setMessage(("Sending recovery E-Mail..."));
        progressDialog.show();

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Email sent!", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(LoginActivity.this, "Failed, make sure an account exist on this email!", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                // get and show proper error message
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Login method
    private void loginUser(String email, String pass) {
        // Show progress dialog
        progressDialog.setMessage(("Logging In..."));
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

    // Right Up Corner menu
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go to the previous activity
        return super.onSupportNavigateUp();
    }
}