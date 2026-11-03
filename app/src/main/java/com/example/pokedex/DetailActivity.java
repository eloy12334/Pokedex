
package com.example.pokedex;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pokedex.api.ApiClient;
import com.example.pokedex.model.PokemonDetail;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private ImageView imgDetalle;
    private TextView tvDetalleNumero;
    private TextView tvDetalleNombre;
    private TextView tvDetalleTipo;
    private TextView tvAltura;
    private TextView tvPeso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imgDetalle = findViewById(R.id.imgDetalle);
        tvDetalleNumero = findViewById(R.id.tvDetalleNumero);
        tvDetalleNombre = findViewById(R.id.tvDetalleNombre);
        tvDetalleTipo = findViewById(R.id.tvDetalleTipo);
        tvAltura = findViewById(R.id.tvAltura);
        tvPeso = findViewById(R.id.tvPeso);

        int pokemonId = getIntent().getIntExtra("pokemon_id", 0);

        if (pokemonId >= 1 && pokemonId <= 151) {
            cargarDetalle(pokemonId);
        } else {
            Toast.makeText(this,
                    "Pokémon no válido",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void cargarDetalle(int id) {

        ApiClient.getApi().getPokemonDetail(String.valueOf(id))
                .enqueue(new Callback<PokemonDetail>() {

                    @Override
                    public void onResponse(
                            Call<PokemonDetail> call,
                            Response<PokemonDetail> response) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            PokemonDetail pokemon = response.body();

                            tvDetalleNumero.setText(
                                    String.format(Locale.ROOT, "#%03d",
                                            pokemon.getId())
                            );

                            String nombre = pokemon.getName();

                            tvDetalleNombre.setText(
                                    nombre.substring(0, 1).toUpperCase(Locale.ROOT)
                                            + nombre.substring(1)
                            );

                            // Tipos del Pokémon
                            StringBuilder tipos = new StringBuilder();

                            if (pokemon.getTypes() != null) {
                                for (int i = 0;
                                     i < pokemon.getTypes().size(); i++) {

                                    String tipo = pokemon.getTypes()
                                            .get(i).getType().getName();

                                    tipo = tipo.substring(0, 1)
                                            .toUpperCase(Locale.ROOT)
                                            + tipo.substring(1);

                                    if (i > 0) {
                                        tipos.append(" / ");
                                    }

                                    tipos.append(tipo);
                                }
                            }

                            tvDetalleTipo.setText(tipos.toString());

                            // Conversión de unidades de PokéAPI
                            tvAltura.setText(String.format(
                                    Locale.ROOT,
                                    "Altura: %.1f m",
                                    pokemon.getHeight() / 10.0
                            ));

                            tvPeso.setText(String.format(
                                    Locale.ROOT,
                                    "Peso: %.1f kg",
                                    pokemon.getWeight() / 10.0
                            ));

                            // Imagen oficial
                            String imagen =
                                    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
                                            + pokemon.getId() + ".png";

                            Glide.with(DetailActivity.this)
                                    .load(imagen)
                                    .into(imgDetalle);

                        } else {
                            Toast.makeText(
                                    DetailActivity.this,
                                    "No se pudo obtener el Pokémon",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PokemonDetail> call,
                            Throwable t) {

                        Log.e("POKEDEX", "Error detalle: "
                                + t.getMessage());

                        Toast.makeText(
                                DetailActivity.this,
                                "Error al cargar los detalles",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
