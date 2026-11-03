package com.example.pokedex;

import android.content.Intent;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokedex.adapter.PokemonAdapter;
import com.example.pokedex.api.ApiClient;
import com.example.pokedex.model.PokemonItem;
import com.example.pokedex.model.PokemonListResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvPokemon;
    private EditText etBuscar;
    private ProgressBar progressBar;

    private PokemonAdapter adapter;

    private final List<PokemonItem> listaCompleta = new ArrayList<>();
    private final List<PokemonItem> listaVisible = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        rvPokemon = findViewById(R.id.rvPokemon);
        etBuscar = findViewById(R.id.etBuscar);
        progressBar = findViewById(R.id.progressBar);

        rvPokemon.setLayoutManager(
                new androidx.recyclerview.widget.GridLayoutManager(this, 2)
        );

        adapter = new PokemonAdapter(listaVisible, pokemon -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    DetailActivity.class
            );

            intent.putExtra("pokemon_id", pokemon.getId());

            startActivity(intent);
        });
        rvPokemon.setAdapter(adapter);

        configurarBusqueda();
        cargarPokemon();
    }

    private void cargarPokemon() {

        progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApi().getPokemonList(151, 0)
                .enqueue(new Callback<PokemonListResponse>() {

                    @Override
                    public void onResponse(
                            Call<PokemonListResponse> call,
                            Response<PokemonListResponse> response) {

                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getResults() != null) {

                            listaCompleta.clear();

                            listaCompleta.addAll(
                                    response.body().getResults()
                            );

                            filtrarPokemon(etBuscar.getText().toString());

                            Log.d("POKEDEX",
                                    "Pokémon cargados: " + listaCompleta.size());

                        } else {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Error al obtener los Pokémon",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PokemonListResponse> call,
                            Throwable t) {

                        progressBar.setVisibility(View.GONE);

                        Log.e("POKEDEX", "Error: " + t.getMessage());

                        Toast.makeText(
                                MainActivity.this,
                                "No se pudo conectar con PokéAPI",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void configurarBusqueda() {

        etBuscar.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                filtrarPokemon(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filtrarPokemon(String texto) {

        listaVisible.clear();

        String busqueda = texto.trim().toLowerCase(Locale.ROOT);

        for (PokemonItem pokemon : listaCompleta) {

            String nombre = pokemon.getName().toLowerCase(Locale.ROOT);
            String numero = String.valueOf(pokemon.getId());

            if (nombre.contains(busqueda)
                    || numero.contains(busqueda)) {

                listaVisible.add(pokemon);
            }
        }

        adapter.actualizarLista(listaVisible);
    }
}
