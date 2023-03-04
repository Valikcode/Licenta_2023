package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    // Views
    Button mRegisterButton, mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init Views
        mRegisterButton = findViewById(R.id.buttonRegisterMain);
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