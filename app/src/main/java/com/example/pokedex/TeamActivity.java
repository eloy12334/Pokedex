package com.example.pokedex;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pokedex.adapter.PokemonSelectAdapter;
import com.example.pokedex.api.ApiClient;
import com.example.pokedex.database.DatabaseHelper;
import com.example.pokedex.model.PokemonItem;
import com.example.pokedex.model.PokemonListResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamActivity extends AppCompatActivity {
    private LinearLayout layoutEquipo;
    private DatabaseHelper databaseHelper;
    private final Map<Integer, PokemonItem> pokemonDisponibles = new HashMap<>();
    private final int[] idsSlots = new int[6];
    private final String[] nombresSlots = new String[6];

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);
        databaseHelper = new DatabaseHelper(this);
        layoutEquipo = findViewById(R.id.layoutEquipo);
        findViewById(R.id.btnVolverEquipo).setOnClickListener(v -> finish());
        cargarEquipoGuardado();
        dibujarEquipo();
        cargarPokemonParaSeleccion();
    }

    private void cargarPokemonParaSeleccion() {
        ApiClient.getApi().getPokemonList(151, 0).enqueue(new Callback<PokemonListResponse>() {
            @Override public void onResponse(Call<PokemonListResponse> call, Response<PokemonListResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    for (PokemonItem p : response.body().getResults()) {
                        int id = p.getId();
                        if (id >= 1 && id <= 151) pokemonDisponibles.put(id, p);
                    }
                } else Toast.makeText(TeamActivity.this, "No se pudo cargar la lista de Pokémon.", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<PokemonListResponse> call, Throwable t) {
                if (!isFinishing() && !isDestroyed()) Toast.makeText(TeamActivity.this, "Error de conexión. Revisa internet.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cargarEquipoGuardado() {
        Cursor c = databaseHelper.obtenerEquipo();
        try {
            while (c.moveToNext()) {
                int slot = c.getInt(0) - 1;
                if (slot >= 0 && slot < 6) {
                    idsSlots[slot] = c.getInt(1);
                    nombresSlots[slot] = c.getString(2);
                }
            }
        } finally { c.close(); }
    }

    private void dibujarEquipo() {
        layoutEquipo.removeAllViews();
        int ocupados = 0;
        for (int id : idsSlots) if (id > 0) ocupados++;

        TextView contador = new TextView(this);
        contador.setText("EQUIPO ACTUAL     " + ocupados + " / 6");
        contador.setTextColor(0xFF43E0A0);
        contador.setTextSize(13);
        contador.setTypeface(null, android.graphics.Typeface.BOLD);
        contador.setPadding(4, 4, 4, 16);
        layoutEquipo.addView(contador);

        for (int i = 0; i < 6; i++) {
            final int slot = i + 1;
            final int pokemonId = idsSlots[i];
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(10), dp(8), dp(10), dp(8));
            card.setBackgroundResource(R.drawable.bg_team_slot);

            ImageView image = new ImageView(this);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp(76), dp(76));
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            if (pokemonId > 0) {
                Glide.with(this).load(artwork(pokemonId)).placeholder(android.R.drawable.ic_menu_gallery).into(image);
            } else {
                image.setImageResource(android.R.drawable.ic_input_add);
                image.setAlpha(0.45f);
            }
            card.addView(image, imageParams);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            infoParams.leftMargin = dp(10);
            card.addView(info, infoParams);

            TextView number = new TextView(this);
            number.setText(pokemonId > 0 ? String.format(java.util.Locale.ROOT, "#%03d", pokemonId) : "ESPACIO " + slot);
            number.setTextColor(0xFF9DA3B5);
            number.setTextSize(11);
            info.addView(number);

            TextView name = new TextView(this);
            name.setText(pokemonId > 0 ? capitalizar(nombresSlots[i]) : "Añadir Pokémon");
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(16);
            name.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(-1, -2);
            nameParams.topMargin = dp(3);
            info.addView(name, nameParams);

            TextView hint = new TextView(this);
            hint.setText(pokemonId > 0 ? "Toca para cambiar" : "Elige entre los 151 de Kanto");
            hint.setTextColor(0xFF858B9D);
            hint.setTextSize(10);
            info.addView(hint);

            Button action = new Button(this);
            action.setText(pokemonId > 0 ? "QUITAR" : "＋");
            action.setTextSize(10);
            action.setTextColor(0xFFFFFFFF);
            action.setMinWidth(0);
            action.setPadding(dp(8), 0, dp(8), 0);
            action.setBackgroundTintList(android.content.res.ColorStateList.valueOf(pokemonId > 0 ? 0xFF923847 : 0xFF267B62));
            action.setOnClickListener(v -> {
                if (idsSlots[slot - 1] == 0) mostrarSelector(slot);
                else {
                    databaseHelper.eliminarPokemonEquipo(slot);
                    idsSlots[slot - 1] = 0;
                    nombresSlots[slot - 1] = null;
                    dibujarEquipo();
                }
            });
            card.addView(action);

            card.setOnClickListener(v -> mostrarSelector(slot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(9);
            layoutEquipo.addView(card, params);
        }
    }

    private void mostrarSelector(int slot) {
        if (pokemonDisponibles.isEmpty()) {
            Toast.makeText(this, "Cargando Pokémon. Intenta nuevamente en unos segundos.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<PokemonItem> lista = new ArrayList<>(pokemonDisponibles.values());
        lista.sort(Comparator.comparingInt(PokemonItem::getId));

        View content = getLayoutInflater().inflate(R.layout.dialog_pokemon_selector, null);
        RecyclerView recycler = content.findViewById(R.id.recyclerPokemonSelector);
        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        PokemonSelectAdapter adapter = new PokemonSelectAdapter(lista, elegido -> {
            for (int i = 0; i < 6; i++) {
                if (idsSlots[i] == elegido.getId() && i != slot - 1) {
                    Toast.makeText(this, "Ese Pokémon ya está en tu equipo.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (databaseHelper.guardarPokemonEquipo(slot, elegido.getId(), elegido.getName())) {
                idsSlots[slot - 1] = elegido.getId();
                nombresSlots[slot - 1] = elegido.getName();
                dibujarEquipo();
                selector.dismiss();
            } else Toast.makeText(this, "No se pudo guardar el Pokémon.", Toast.LENGTH_SHORT).show();
        });
        recycler.setAdapter(adapter);
        content.<android.widget.EditText>findViewById(R.id.edtBuscarPokemon).addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { adapter.filtrar(s.toString()); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        content.<TextView>findViewById(R.id.txtTituloSelector).setText("ELIGE TU POKÉMON  ·  ESPACIO " + slot);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(content).create();
        selector = dialog;
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels - dp(28),
                    (int)(getResources().getDisplayMetrics().heightPixels * 0.82));
        }
    }
    private AlertDialog selector;

    private String artwork(int id) {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + id + ".png";
    }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
    private String capitalizar(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase(java.util.Locale.ROOT) + s.substring(1);
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) databaseHelper.close();
    }
}
