
package com.example.pokedex;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnEntrarPokedex;

    private ImageView imgBulbasaur;
    private ImageView imgCharmander;
    private ImageView imgSquirtle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        btnEntrarPokedex = findViewById(R.id.btnEntrarPokedex);

        imgBulbasaur = findViewById(R.id.imgBulbasaur);
        imgCharmander = findViewById(R.id.imgCharmander);
        imgSquirtle = findViewById(R.id.imgSquirtle);

        cargarImagenes();

        btnEntrarPokedex.setOnClickListener(v -> {

            Intent intent = new Intent(
                    WelcomeActivity.this,
                    MainActivity.class
            );

            startActivity(intent);
        });
    }

    private void cargarImagenes() {

        String baseUrl =
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/";

        Glide.with(this)
                .load(baseUrl + "1.png")
                .into(imgBulbasaur);

        Glide.with(this)
                .load(baseUrl + "4.png")
                .into(imgCharmander);

        Glide.with(this)
                .load(baseUrl + "7.png")
                .into(imgSquirtle);
    }
}
