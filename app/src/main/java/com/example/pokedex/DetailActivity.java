
package com.example.pokedex;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pokedex.api.ApiClient;
import com.example.pokedex.model.PokemonDetail;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    private LinearLayout layoutStats;

    private final Map<String, String> traduccionesTipos =
            new HashMap<>();

    private final Map<String, String> traduccionesStats =
            new HashMap<>();

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
        layoutStats = findViewById(R.id.layoutStats);

        configurarTraducciones();

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

    private void configurarTraducciones() {

        traduccionesTipos.put("grass", "Planta");
        traduccionesTipos.put("poison", "Veneno");
        traduccionesTipos.put("fire", "Fuego");
        traduccionesTipos.put("water", "Agua");
        traduccionesTipos.put("bug", "Bicho");
        traduccionesTipos.put("normal", "Normal");
        traduccionesTipos.put("flying", "Volador");
        traduccionesTipos.put("electric", "Eléctrico");
        traduccionesTipos.put("ground", "Tierra");
        traduccionesTipos.put("fairy", "Hada");
        traduccionesTipos.put("fighting", "Lucha");
        traduccionesTipos.put("psychic", "Psíquico");
        traduccionesTipos.put("rock", "Roca");
        traduccionesTipos.put("ghost", "Fantasma");
        traduccionesTipos.put("ice", "Hielo");
        traduccionesTipos.put("dragon", "Dragón");

        traduccionesStats.put("hp", "PS");
        traduccionesStats.put("attack", "Ataque");
        traduccionesStats.put("defense", "Defensa");
        traduccionesStats.put("special-attack", "Ataque especial");
        traduccionesStats.put("special-defense", "Defensa especial");
        traduccionesStats.put("speed", "Velocidad");
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
                                    String.format(Locale.ROOT,
                                            "#%03d", pokemon.getId())
                            );

                            String nombre = pokemon.getName();

                            tvDetalleNombre.setText(
                                    nombre.substring(0, 1).toUpperCase(Locale.ROOT)
                                            + nombre.substring(1)
                            );

                            // Tipos en español
                            StringBuilder tipos = new StringBuilder();

                            if (pokemon.getTypes() != null) {
                                for (int i = 0;
                                     i < pokemon.getTypes().size(); i++) {

                                    String tipo = pokemon.getTypes()
                                            .get(i).getType().getName();

                                    String tipoTraducido =
                                            traduccionesTipos.getOrDefault(
                                                    tipo, tipo);

                                    if (i > 0) {
                                        tipos.append(" / ");
                                    }

                                    tipos.append(tipoTraducido);
                                }
                            }

                            tvDetalleTipo.setText(tipos.toString());

                            // Altura y peso
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

                            // Estadísticas
                            mostrarEstadisticas(pokemon);

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

                        Log.e("POKEDEX",
                                "Error detalle: " + t.getMessage());

                        Toast.makeText(
                                DetailActivity.this,
                                "Error al cargar los detalles",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void mostrarEstadisticas(PokemonDetail pokemon) {

        layoutStats.removeAllViews();

        if (pokemon.getStats() == null) {
            return;
        }

        for (PokemonDetail.StatSlot stat : pokemon.getStats()) {

            String nombreOriginal = stat.getStat().getName();

            String nombre = traduccionesStats.getOrDefault(
                    nombreOriginal, nombreOriginal);

            int valor = stat.getBaseStat();

            TextView texto = new TextView(this);

            texto.setText(nombre + ": " + valor);
            texto.setTextSize(16);
            texto.setTextColor(0xFF252A34);
            texto.setPadding(12, 12, 12, 12);

            layoutStats.addView(texto);
        }
    }
}
