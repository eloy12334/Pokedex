package com.example.pokedex;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnEntrarPokedex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btnEntrarPokedex = findViewById(R.id.btnEntrarPokedex);

        btnEntrarPokedex.setOnClickListener(v -> {
            Intent intent = new Intent(
                    WelcomeActivity.this,
                    MainActivity.class
            );
            startActivity(intent);
        });
    }
}
