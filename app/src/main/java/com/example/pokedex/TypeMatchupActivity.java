package com.example.pokedex;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pokedex.api.ApiClient;
import com.example.pokedex.model.PokemonTypeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TypeMatchupActivity extends AppCompatActivity {

    private Spinner spnAtaque, spnDefensa;
    private TextView txtResultado;
    private Button btnCalcular;

    private final String[] tiposApi = {
            "normal", "fire", "water", "electric", "grass", "ice",
            "fighting", "poison", "ground", "flying", "psychic",
            "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy"
    };

    private final String[] tiposEspanol = {
            "Normal", "Fuego", "Agua", "Eléctrico", "Planta", "Hielo",
            "Lucha", "Veneno", "Tierra", "Volador", "Psíquico",
            "Bicho", "Roca", "Fantasma", "Dragón", "Siniestro", "Acero", "Hada"
    };

    private final String[] simbolos = {
            "◯", "🔥", "💧", "⚡", "🍃", "❄", "✊", "☠",
            "⛰", "🪽", "🔮", "🐛", "🪨", "👻", "🐉", "🌑", "⚙", "✨"
    };

    private final int[] colores = {
            0xFFA8A878, 0xFFF08030, 0xFF6890F0, 0xFFF8D030,
            0xFF78C850, 0xFF98D8D8, 0xFFC03028, 0xFFA040A0,
            0xFFE0C068, 0xFFA890F0, 0xFFF85888, 0xFFA8B820,
            0xFFB8A038, 0xFF705898, 0xFF7038F8, 0xFF705848,
            0xFFB8B8D0, 0xFFEE99AC
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_matchup);

        spnAtaque = findViewById(R.id.spnAtaque);
        spnDefensa = findViewById(R.id.spnDefensa);
        txtResultado = findViewById(R.id.txtResultado);
        btnCalcular = findViewById(R.id.btnCalcular);

        Button btnVolver = findViewById(R.id.btnVolverTipos);
        btnVolver.setOnClickListener(v -> finish());

        TypeSpinnerAdapter adapter = new TypeSpinnerAdapter();
        spnAtaque.setAdapter(adapter);
        spnDefensa.setAdapter(new TypeSpinnerAdapter());

        btnCalcular.setOnClickListener(v -> calcularEfectividad());
    }

    private class TypeSpinnerAdapter extends ArrayAdapter<String> {
        TypeSpinnerAdapter() {
            super(TypeMatchupActivity.this, android.R.layout.simple_spinner_item, tiposEspanol);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return crearFila(position, false);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return crearFila(position, true);
        }

        private View crearFila(int position, boolean desplegable) {
            LinearLayout fila = new LinearLayout(TypeMatchupActivity.this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(Gravity.CENTER_VERTICAL);
            fila.setPadding(12, desplegable ? 12 : 5, 12, desplegable ? 12 : 5);
            fila.setBackgroundColor(desplegable ? 0xFF20222E : Color.TRANSPARENT);

            TextView icono = new TextView(TypeMatchupActivity.this);
            icono.setText(simbolos[position]);
            icono.setTextSize(17);
            icono.setGravity(Gravity.CENTER);
            GradientDrawable circulo = new GradientDrawable();
            circulo.setShape(GradientDrawable.OVAL);
            circulo.setColor(colores[position]);
            icono.setBackground(circulo);
            icono.setWidth(dp(34));
            icono.setHeight(dp(34));

            TextView nombre = new TextView(TypeMatchupActivity.this);
            nombre.setText(tiposEspanol[position]);
            nombre.setTextColor(Color.WHITE);
            nombre.setTextSize(14);
            nombre.setTypeface(null, Typeface.BOLD);
            nombre.setPadding(dp(12), 0, 0, 0);

            fila.addView(icono);
            fila.addView(nombre);
            return fila;
        }
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void calcularEfectividad() {
        int posicionAtaque = spnAtaque.getSelectedItemPosition();
        int posicionDefensa = spnDefensa.getSelectedItemPosition();
        String tipoAtaque = tiposApi[posicionAtaque];
        String tipoDefensa = tiposApi[posicionDefensa];

        btnCalcular.setEnabled(false);
        txtResultado.setText("Consultando tabla oficial de tipos...");

        ApiClient.getApi().getPokemonByType(tipoAtaque)
                .enqueue(new Callback<PokemonTypeResponse>() {
                    @Override
                    public void onResponse(Call<PokemonTypeResponse> call,
                                           Response<PokemonTypeResponse> response) {
                        btnCalcular.setEnabled(true);

                        if (!response.isSuccessful() || response.body() == null
                                || response.body().getDamageRelations() == null) {
                            txtResultado.setText("No se pudo obtener la efectividad. Intenta nuevamente.");
                            return;
                        }

                        PokemonTypeResponse.DamageRelations dr = response.body().getDamageRelations();
                        double multiplicador = 1.0;
                        if (contiene(dr.getNoDamageTo(), tipoDefensa)) multiplicador = 0.0;
                        else if (contiene(dr.getDoubleDamageTo(), tipoDefensa)) multiplicador = 2.0;
                        else if (contiene(dr.getHalfDamageTo(), tipoDefensa)) multiplicador = 0.5;

                        String valor = multiplicador == 0.0 ? "0×"
                                : multiplicador == 0.5 ? "0,5×"
                                : multiplicador == 2.0 ? "2×" : "1×";

                        String conclusion = multiplicador == 0.0 ? "No causa daño."
                                : multiplicador > 1.0 ? "Es un ataque supereficaz."
                                : multiplicador < 1.0 ? "Es un ataque poco eficaz."
                                : "Daño normal.";

                        txtResultado.setText(simbolos[posicionAtaque] + " "
                                + tiposEspanol[posicionAtaque] + " contra "
                                + simbolos[posicionDefensa] + " "
                                + tiposEspanol[posicionDefensa] + "\n\n"
                                + valor + "\n" + conclusion);
                    }

                    @Override
                    public void onFailure(Call<PokemonTypeResponse> call, Throwable t) {
                        btnCalcular.setEnabled(true);
                        txtResultado.setText("Error de conexión. Revisa internet e inténtalo otra vez.");
                    }
                });
    }

    private boolean contiene(List<PokemonTypeResponse.TypeRelation> lista, String tipo) {
        if (lista == null) return false;
        for (PokemonTypeResponse.TypeRelation relacion : lista) {
            if (relacion != null && tipo.equals(relacion.getName())) return true;
        }
        return false;
    }
}
