package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Views
    TextView mRegisterButton;
    Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set FullScreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ascund Action Bar de pe Splah Screen
        getSupportActionBar().hide();

        // Init Views
        mRegisterButton = findViewById(R.id.textViewRegister);
        mLoginButton = findViewById(R.id.buttonLogin);

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


    }
}