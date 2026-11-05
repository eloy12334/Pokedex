package com.example.pokedex;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.ViewGroup;
import android.graphics.Color;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pokedex.api.ApiClient;
import com.example.pokedex.model.PokemonTypeResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TypeMatchupActivity extends AppCompatActivity {

    private Spinner spnAtaque;
    private Spinner spnDefensa;
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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, tiposEspanol) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.WHITE);
                view.setTextSize(16);
                view.setPadding(12, 0, 12, 0);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(Color.WHITE);
                view.setTextSize(15);
                view.setPadding(18, 14, 18, 14);
                view.setBackgroundColor(0xFF202330);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnAtaque.setAdapter(adapter);
        spnDefensa.setAdapter(adapter);

        btnCalcular.setOnClickListener(v -> calcularEfectividad());
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

                        PokemonTypeResponse.DamageRelations dr =
                                response.body().getDamageRelations();

                        double multiplicador = 1.0;
                        if (contiene(dr.getNoDamageTo(), tipoDefensa)) {
                            multiplicador = 0.0;
                        } else if (contiene(dr.getDoubleDamageTo(), tipoDefensa)) {
                            multiplicador = 2.0;
                        } else if (contiene(dr.getHalfDamageTo(), tipoDefensa)) {
                            multiplicador = 0.5;
                        }

                        String valor;
                        if (multiplicador == 0.0) valor = "0×";
                        else if (multiplicador == 0.5) valor = "0,5×";
                        else if (multiplicador == 2.0) valor = "2×";
                        else valor = "1×";

                        String conclusion;
                        if (multiplicador == 0.0) conclusion = "No causa daño.";
                        else if (multiplicador > 1.0) conclusion = "Es un ataque supereficaz.";
                        else if (multiplicador < 1.0) conclusion = "Es un ataque poco eficaz.";
                        else conclusion = "Daño normal.";

                        txtResultado.setText(
                                tiposEspanol[posicionAtaque] + " contra "
                                + tiposEspanol[posicionDefensa] + "\n\n"
                                + valor + "\n" + conclusion
                        );
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
