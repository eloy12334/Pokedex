
package com.example.pokedex;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.pokedex.api.ApiClient;
import com.example.pokedex.api.PokeApi;
import com.example.pokedex.model.EvolutionChainResponse;
import com.example.pokedex.model.PokemonDetail;
import com.example.pokedex.model.PokemonSpecies;
import com.example.pokedex.model.PokemonTypeResponse;
import com.example.pokedex.model.MoveDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "EVOLUCIONES";
    private static final int MAX_MOVIMIENTOS = 30;

    private ImageView imgDetalle;
    private TextView tvDetalleNumero;
    private TextView tvDetalleNombre;
    private TextView tvDetalleTipo;
    private TextView tvAltura;
    private TextView tvPeso;

    private LinearLayout layoutStats;
    private LinearLayout layoutHabilidades;
    private LinearLayout layoutMovimientos;
    private LinearLayout layoutEvoluciones;
    private LinearLayout layoutEfectividad;
    private LinearLayout layoutDatosEspecie;

    private Button btnVerTodosMovimientos;

    private PokeApi api;
    private PokemonDetail pokemonActual;

    private int pokemonId;
    private boolean mostrarTodosMovimientos = false;

    private final List<Integer> idsKanto = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        inicializarVistas();

        api = ApiClient.getApi();

        pokemonId = getIntent().getIntExtra("pokemon_id", 0);

        Button btnVolver = findViewById(R.id.btnVolverPokedex);
        btnVolver.setOnClickListener(v -> finish());

        btnVerTodosMovimientos.setOnClickListener(v -> {
            mostrarTodosMovimientos = !mostrarTodosMovimientos;
            cargarMovimientosActuales();
        });

        if (pokemonId <= 0) {
            Toast.makeText(this, "Pokémon no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        for (int i = 1; i <= 151; i++) {
            idsKanto.add(i);
        }

        cargarPokemon();
    }

    private void inicializarVistas() {

        imgDetalle = findViewById(R.id.imgDetalle);
        tvDetalleNumero = findViewById(R.id.tvDetalleNumero);
        tvDetalleNombre = findViewById(R.id.tvDetalleNombre);
        tvDetalleTipo = findViewById(R.id.tvDetalleTipo);
        tvAltura = findViewById(R.id.tvAltura);
        tvPeso = findViewById(R.id.tvPeso);

        layoutStats = findViewById(R.id.layoutStats);
        layoutHabilidades = findViewById(R.id.layoutHabilidades);
        layoutMovimientos = findViewById(R.id.layoutMovimientos);
        layoutEvoluciones = findViewById(R.id.layoutEvoluciones);
        layoutEfectividad = findViewById(R.id.layoutEfectividad);
        layoutDatosEspecie = findViewById(R.id.layoutDatosEspecie);

        btnVerTodosMovimientos =
                findViewById(R.id.btnVerTodosMovimientos);

        layoutEvoluciones.setOrientation(LinearLayout.VERTICAL);
    }

    // ---------------------------------------------------------
    // CARGAR INFORMACION DEL POKEMON
    // ---------------------------------------------------------

    private void cargarPokemon() {

        api.getPokemonDetail(String.valueOf(pokemonId))
                .enqueue(new Callback<PokemonDetail>() {

                    @Override
                    public void onResponse(
                            Call<PokemonDetail> call,
                            Response<PokemonDetail> response) {

                        if (!response.isSuccessful() || response.body() == null) {

                            Toast.makeText(
                                    DetailActivity.this,
                                    "No se pudo cargar el Pokémon",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        PokemonDetail pokemon = response.body();
                        pokemonActual = pokemon;

                        mostrarInformacion(pokemon);
                        cargarEfectividadTipos(pokemon);
                        mostrarEstadisticas(pokemon);
                        mostrarHabilidades(pokemon);
                        mostrarMovimientos(pokemon);

                        cargarCadenaEvolutiva(pokemon.getId());
                    }

                    @Override
                    public void onFailure(
                            Call<PokemonDetail> call,
                            Throwable t) {

                        Log.e(TAG, "Error cargando Pokémon", t);

                        Toast.makeText(
                                DetailActivity.this,
                                "Error de conexión",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void mostrarInformacion(PokemonDetail pokemon) {

        tvDetalleNumero.setText(
                String.format(Locale.getDefault(), "#%03d", pokemon.getId())
        );

        tvDetalleNombre.setText(capitalizar(pokemon.getName()));

        tvAltura.setText(
                String.format(
                        Locale.getDefault(),
                        "ALTURA\n%.1f m",
                        pokemon.getHeight() / 10.0
                )
        );

        tvPeso.setText(
                String.format(
                        Locale.getDefault(),
                        "PESO\n%.1f kg",
                        pokemon.getWeight() / 10.0
                )
        );

        StringBuilder tipos = new StringBuilder();

        if (pokemon.getTypes() != null) {

            for (PokemonDetail.TypeSlot slot : pokemon.getTypes()) {

                if (slot.getType() != null) {

                    if (tipos.length() > 0) {
                        tipos.append("  •  ");
                    }

                    tipos.append(
                            traducirTipo(slot.getType().getName())
                    );
                }
            }
        }

        tvDetalleTipo.setText(tipos.toString());

        String imagen =
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
                        + pokemon.getId() + ".png";

        Glide.with(this)
                .load(imagen)
                .centerInside()
                .into(imgDetalle);
    }

    // ---------------------------------------------------------
    // EFECTIVIDAD DE TIPOS
    // ---------------------------------------------------------

    private void cargarEfectividadTipos(PokemonDetail pokemon) {

        layoutEfectividad.removeAllViews();
        Log.d(TAG, "INICIANDO CARGA DE EFECTIVIDAD");
        agregarTextoSimple(layoutEfectividad, "Calculando efectividades...");

        if (pokemon.getTypes() == null || pokemon.getTypes().isEmpty()) {
            mostrarErrorEfectividad("No hay información de tipos disponible.");
            return;
        }

        List<String> tiposPokemon = new ArrayList<>();

        for (PokemonDetail.TypeSlot slot : pokemon.getTypes()) {
            if (slot != null && slot.getType() != null
                    && slot.getType().getName() != null) {
                tiposPokemon.add(slot.getType().getName());
            }
        }

        if (tiposPokemon.isEmpty()) {
            mostrarErrorEfectividad("No se pudieron identificar los tipos.");
            return;
        }

        Map<String, PokemonTypeResponse> relaciones = new HashMap<>();
        final int[] respuestasRecibidas = {0};
        final boolean[] huboError = {false};

        for (String tipo : tiposPokemon) {
            api.getPokemonByType(tipo).enqueue(new Callback<PokemonTypeResponse>() {

                @Override
                public void onResponse(
                        Call<PokemonTypeResponse> call,
                        Response<PokemonTypeResponse> response) {

                    Log.d(TAG, "RESPUESTA TIPO: " + tipo
                            + " | HTTP: " + response.code());

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (response.isSuccessful() && response.body() != null
                            && response.body().getDamageRelations() != null) {
                        relaciones.put(tipo, response.body());
                    } else {
                        huboError[0] = true;
                        Log.e(TAG, "No se pudieron cargar relaciones del tipo "
                                + tipo + ". HTTP " + response.code());
                    }

                    respuestasRecibidas[0]++;

                    if (respuestasRecibidas[0] == tiposPokemon.size()) {
                        if (relaciones.size() == tiposPokemon.size()) {
                            mostrarEfectividadCalculada(tiposPokemon, relaciones);
                        } else {
                            mostrarErrorEfectividad(
                                    "No se pudo completar el cálculo. Revisa tu conexión."
                            );
                        }
                    }
                }

                @Override
                public void onFailure(
                        Call<PokemonTypeResponse> call,
                        Throwable t) {

                    Log.e(TAG, "FALLO CONSULTANDO TIPO: " + tipo, t);

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    huboError[0] = true;
                    respuestasRecibidas[0]++;
                    Log.e(TAG, "Error consultando tipo " + tipo, t);

                    if (respuestasRecibidas[0] == tiposPokemon.size()) {
                        mostrarErrorEfectividad(
                                "No se pudo cargar la efectividad. Revisa tu conexión."
                        );
                    }
                }
            });
        }
    }

    private void mostrarEfectividadCalculada(
            List<String> tiposPokemon,
            Map<String, PokemonTypeResponse> relaciones) {

        layoutEfectividad.removeAllViews();

        String[] todosLosTipos = {
                "normal", "fire", "water", "electric", "grass", "ice",
                "fighting", "poison", "ground", "flying", "psychic",
                "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy"
        };

        List<String> fortalezas = new ArrayList<>();
        List<String> debilidades = new ArrayList<>();
        List<String> resistencias = new ArrayList<>();
        List<String> inmunidades = new ArrayList<>();

        for (String tipoObjetivo : todosLosTipos) {

            double multiplicadorAtaque = 1.0;
            double multiplicadorDefensa = 1.0;

            for (String tipoPropio : tiposPokemon) {

                PokemonTypeResponse datos = relaciones.get(tipoPropio);
                if (datos == null || datos.getDamageRelations() == null) {
                    continue;
                }

                PokemonTypeResponse.DamageRelations dr =
                        datos.getDamageRelations();

                // Efectividad ofensiva: cómo ataca este Pokémon a otros tipos.
                multiplicadorAtaque *= obtenerMultiplicador(
                        tipoObjetivo,
                        dr.getDoubleDamageTo(),
                        dr.getHalfDamageTo(),
                        dr.getNoDamageTo()
                );

                // Efectividad defensiva: cuánto daño recibe de cada tipo.
                multiplicadorDefensa *= obtenerMultiplicador(
                        tipoObjetivo,
                        dr.getDoubleDamageFrom(),
                        dr.getHalfDamageFrom(),
                        dr.getNoDamageFrom()
                );
            }

            if (multiplicadorAtaque > 1.0) {
                fortalezas.add(traducirTipo(tipoObjetivo)
                        + " (x" + formatearMultiplicador(multiplicadorAtaque) + ")");
            }

            if (multiplicadorDefensa == 0.0) {
                inmunidades.add(traducirTipo(tipoObjetivo));
            } else if (multiplicadorDefensa > 1.0) {
                debilidades.add(traducirTipo(tipoObjetivo)
                        + " (x" + formatearMultiplicador(multiplicadorDefensa) + ")");
            } else if (multiplicadorDefensa < 1.0) {
                resistencias.add(traducirTipo(tipoObjetivo)
                        + " (x" + formatearMultiplicador(multiplicadorDefensa) + ")");
            }
        }

        agregarSeccionEfectividad(
                "⚔ FORTALEZAS · ATAQUE",
                fortalezas,
                Color.rgb(67, 224, 160),
                "No tiene ventajas ofensivas de tipo."
        );

        agregarSeccionEfectividad(
                "▼ DEBILIDADES",
                debilidades,
                Color.rgb(235, 105, 105),
                "No presenta debilidades de tipo."
        );

        agregarSeccionEfectividad(
                "◆ RESISTENCIAS",
                resistencias,
                Color.rgb(100, 165, 235),
                "No presenta resistencias de tipo."
        );

        agregarSeccionEfectividad(
                "◉ INMUNIDADES",
                inmunidades,
                Color.rgb(190, 150, 235),
                "No presenta inmunidades de tipo."
        );
    }

    private double obtenerMultiplicador(
            String tipoObjetivo,
            List<PokemonTypeResponse.TypeRelation> doble,
            List<PokemonTypeResponse.TypeRelation> mitad,
            List<PokemonTypeResponse.TypeRelation> nulo) {

        if (contieneTipoRelacion(nulo, tipoObjetivo)) {
            return 0.0;
        }

        if (contieneTipoRelacion(doble, tipoObjetivo)) {
            return 2.0;
        }

        if (contieneTipoRelacion(mitad, tipoObjetivo)) {
            return 0.5;
        }

        return 1.0;
    }

    private boolean contieneTipoRelacion(
            List<PokemonTypeResponse.TypeRelation> lista,
            String nombreTipo) {

        if (lista == null) {
            return false;
        }

        for (PokemonTypeResponse.TypeRelation relacion : lista) {
            if (relacion != null && nombreTipo.equals(relacion.getName())) {
                return true;
            }
        }

        return false;
    }

    private String formatearMultiplicador(double valor) {
        if (valor == Math.floor(valor)) {
            return String.valueOf((int) valor);
        }
        return String.valueOf(valor).replace(".", ",");
    }

    private void agregarSeccionEfectividad(
            String titulo,
            List<String> tipos,
            int color,
            String mensajeVacio) {

        TextView encabezado = new TextView(this);
        encabezado.setText(titulo);
        encabezado.setTextColor(color);
        encabezado.setTextSize(13);
        encabezado.setTypeface(null, Typeface.BOLD);
        encabezado.setPadding(0, 14, 0, 8);
        layoutEfectividad.addView(encabezado);

        if (tipos.isEmpty()) {
            TextView vacio = new TextView(this);
            vacio.setText(mensajeVacio);
            vacio.setTextColor(Color.rgb(190, 196, 208));
            vacio.setTextSize(12);
            vacio.setPadding(0, 2, 0, 8);
            layoutEfectividad.addView(vacio);
            return;
        }

        GridLayout cuadrícula = new GridLayout(this);
        cuadrícula.setColumnCount(2);
        cuadrícula.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        cuadrícula.setUseDefaultMargins(false);

        for (String tipoConMultiplicador : tipos) {

            String textoTipo = tipoConMultiplicador;
            String multiplicador = "";

            int indice = tipoConMultiplicador.indexOf(" (x");
            if (indice >= 0) {
                textoTipo = tipoConMultiplicador.substring(0, indice).trim();
                multiplicador = tipoConMultiplicador
                        .substring(indice + 2)
                        .replace(")", "")
                        .toUpperCase(Locale.ROOT);
            }

            TextView etiqueta = new TextView(this);
            etiqueta.setText(multiplicador.isEmpty()
                    ? textoTipo
                    : textoTipo + "  " + multiplicador);
            etiqueta.setTextColor(Color.WHITE);
            etiqueta.setTextSize(12);
            etiqueta.setTypeface(null, Typeface.BOLD);
            etiqueta.setGravity(Gravity.CENTER);
            etiqueta.setPadding(12, 9, 12, 9);
            etiqueta.setBackground(crearFondoRedondeado(
                    obtenerColorTipoMovimiento(obtenerNombreTipoApi(textoTipo)),
                    24,
                    Color.rgb(75, 82, 100)
            ));

            GridLayout.LayoutParams parametros =
                    new GridLayout.LayoutParams();
            parametros.width = 0;
            parametros.height = GridLayout.LayoutParams.WRAP_CONTENT;
            parametros.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            parametros.setMargins(0, 3, 8, 7);

            cuadrícula.addView(etiqueta, parametros);
        }

        layoutEfectividad.addView(cuadrícula);
    }

    private String obtenerNombreTipoApi(String tipoTraducido) {

        switch (tipoTraducido.toLowerCase(Locale.ROOT)) {
            case "normal": return "normal";
            case "fuego": return "fire";
            case "agua": return "water";
            case "eléctrico": return "electric";
            case "planta": return "grass";
            case "hielo": return "ice";
            case "lucha": return "fighting";
            case "veneno": return "poison";
            case "tierra": return "ground";
            case "volador": return "flying";
            case "psíquico": return "psychic";
            case "bicho": return "bug";
            case "roca": return "rock";
            case "fantasma": return "ghost";
            case "dragón": return "dragon";
            case "siniestro": return "dark";
            case "acero": return "steel";
            case "hada": return "fairy";
            default: return "normal";
        }
    }

    private void mostrarErrorEfectividad(String mensaje) {
        layoutEfectividad.removeAllViews();
        agregarTextoSimple(layoutEfectividad, mensaje);
    }

    // ---------------------------------------------------------
    // ESTADISTICAS
    // ---------------------------------------------------------

    private void mostrarEstadisticas(PokemonDetail pokemon) {

        layoutStats.removeAllViews();

        if (pokemon.getStats() == null) {
            return;
        }

        for (PokemonDetail.StatSlot stat : pokemon.getStats()) {

            if (stat.getStat() == null) {
                continue;
            }

            String nombre = traducirEstadistica(
                    stat.getStat().getName()
            );

            int valor = stat.getBaseStat();

            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(Gravity.CENTER_VERTICAL);
            fila.setPadding(0, 8, 0, 8);

            TextView txtNombre = new TextView(this);
            txtNombre.setText(nombre);
            txtNombre.setTextColor(Color.WHITE);
            txtNombre.setTextSize(13);
            txtNombre.setTypeface(null, Typeface.BOLD);

            LinearLayout.LayoutParams nombreParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    );

            fila.addView(txtNombre, nombreParams);

            TextView txtValor = new TextView(this);
            txtValor.setText(String.valueOf(valor));
            txtValor.setTextColor(Color.rgb(67, 224, 160));
            txtValor.setTextSize(14);
            txtValor.setTypeface(null, Typeface.BOLD);

            fila.addView(txtValor);

            layoutStats.addView(fila);

            View separador = new View(this);
            separador.setBackgroundColor(Color.rgb(45, 48, 60));

            LinearLayout.LayoutParams separadorParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    );

            layoutStats.addView(separador, separadorParams);
        }
    }

    // ---------------------------------------------------------
    // HABILIDADES
    // ---------------------------------------------------------

    private void mostrarHabilidades(PokemonDetail pokemon) {

        layoutHabilidades.removeAllViews();

        if (pokemon.getAbilities() == null
                || pokemon.getAbilities().isEmpty()) {

            agregarTextoSimple(
                    layoutHabilidades,
                    "No hay habilidades disponibles."
            );

            return;
        }

        for (PokemonDetail.AbilitySlot slot : pokemon.getAbilities()) {

            if (slot.getAbility() == null) {
                continue;
            }

            String nombre = capitalizar(
                    slot.getAbility().getName().replace("-", " ")
            );

            String texto = nombre;

            if (slot.isHidden()) {
                texto += " (Oculta)";
            }

            agregarTextoSimple(layoutHabilidades, "• " + texto);
        }
    }

    // ---------------------------------------------------------
    // MOVIMIENTOS
    // ---------------------------------------------------------

    private void mostrarMovimientos(PokemonDetail pokemon) {

        layoutMovimientos.removeAllViews();

        if (pokemon.getMoves() == null || pokemon.getMoves().isEmpty()) {

            agregarTextoSimple(
                    layoutMovimientos,
                    "No hay movimientos disponibles."
            );

            btnVerTodosMovimientos.setVisibility(View.GONE);
            return;
        }

        int total = pokemon.getMoves().size();

        int cantidad = mostrarTodosMovimientos
                ? total
                : Math.min(total, MAX_MOVIMIENTOS);

        TextView contador = new TextView(this);

        contador.setText(
                "Movimientos registrados: " + total
                        + " · Mostrando " + cantidad
        );

        contador.setTextColor(Color.rgb(67, 224, 160));
        contador.setTextSize(12);
        contador.setTypeface(null, Typeface.BOLD);
        contador.setPadding(0, 0, 0, 12);

        layoutMovimientos.addView(contador);

        for (int i = 0; i < cantidad; i++) {

            PokemonDetail.MoveSlot movimiento =
                    pokemon.getMoves().get(i);

            if (movimiento == null || movimiento.getMove() == null) {
                continue;
            }

            String nombre = movimiento.getMove().getName();

            if (nombre == null) {
                continue;
            }

            TextView txtMovimiento = new TextView(this);

            txtMovimiento.setText(
                    "• " + capitalizar(nombre.replace("-", " "))
            );

            txtMovimiento.setTextColor(Color.WHITE);
            txtMovimiento.setTextSize(13);
            txtMovimiento.setPadding(4, 12, 4, 12);
            txtMovimiento.setClickable(true);
            txtMovimiento.setFocusable(true);
            txtMovimiento.setBackgroundResource(
                    android.R.drawable.list_selector_background
            );

            txtMovimiento.setOnClickListener(v ->
                    consultarDetalleMovimiento(nombre)
            );

            layoutMovimientos.addView(txtMovimiento);

            View separador = new View(this);
            separador.setBackgroundColor(Color.rgb(45, 48, 60));

            LinearLayout.LayoutParams parametros =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    );

            layoutMovimientos.addView(separador, parametros);
        }

        if (total > MAX_MOVIMIENTOS) {

            btnVerTodosMovimientos.setVisibility(View.VISIBLE);

            btnVerTodosMovimientos.setText(
                    mostrarTodosMovimientos
                            ? "MOSTRAR MENOS"
                            : "VER TODOS LOS MOVIMIENTOS"
            );

        } else {

            btnVerTodosMovimientos.setVisibility(View.GONE);
        }
    }

    // ---------------------------------------------------------
    // DETALLES DE CADA MOVIMIENTO
    // ---------------------------------------------------------

    private void consultarDetalleMovimiento(String nombreMovimiento) {

        AlertDialog dialogoCarga = new AlertDialog.Builder(this)
                .setTitle("Pokédex")
                .setMessage("Consultando información del movimiento...")
                .setCancelable(false)
                .create();

        dialogoCarga.show();

        api.getMoveDetail(nombreMovimiento).enqueue(
                new Callback<MoveDetail>() {

                    @Override
                    public void onResponse(
                            Call<MoveDetail> call,
                            Response<MoveDetail> response) {

                        dialogoCarga.dismiss();

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(
                                    DetailActivity.this,
                                    "No se pudieron cargar los detalles del movimiento",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        mostrarVentanaDetalleMovimiento(
                                nombreMovimiento,
                                response.body()
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<MoveDetail> call,
                            Throwable t) {

                        dialogoCarga.dismiss();

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Log.e(TAG, "Error consultando movimiento: "
                                + nombreMovimiento, t);

                        Toast.makeText(
                                DetailActivity.this,
                                "Error de conexión al consultar el movimiento",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // VENTANA VISUAL DE DETALLES DEL MOVIMIENTO
    // ---------------------------------------------------------

    private void mostrarVentanaDetalleMovimiento(
            String nombreMovimiento,
            MoveDetail movimiento) {

        String nombre = movimiento.getName() == null
                ? capitalizar(nombreMovimiento)
                : capitalizar(movimiento.getName());

        String tipoOriginal = "normal";
        if (movimiento.getType() != null
                && movimiento.getType().getName() != null) {
            tipoOriginal = movimiento.getType().getName();
        }

        String tipo = traducirTipo(tipoOriginal);

        String categoriaOriginal = "status";
        if (movimiento.getDamage_class() != null
                && movimiento.getDamage_class().getName() != null) {
            categoriaOriginal = movimiento.getDamage_class().getName();
        }

        String categoria;
        int colorCategoria;

        switch (categoriaOriginal) {
            case "physical":
                categoria = "FÍSICO";
                colorCategoria = Color.rgb(225, 91, 91);
                break;
            case "special":
                categoria = "ESPECIAL";
                colorCategoria = Color.rgb(91, 151, 235);
                break;
            default:
                categoria = "ESTADO";
                colorCategoria = Color.rgb(155, 165, 180);
                break;
        }

        String potencia = movimiento.getPower() > 0
                ? String.valueOf(movimiento.getPower())
                : "No aplica";

        String precision = movimiento.getAccuracy() > 0
                ? movimiento.getAccuracy() + "%"
                : "No aplica";

        int colorTipo = obtenerColorTipoMovimiento(tipoOriginal);

        LinearLayout contenido = new LinearLayout(this);
        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(24, 22, 24, 18);
        contenido.setBackground(crearFondoRedondeado(
                Color.rgb(17, 20, 30),
                24,
                Color.rgb(55, 61, 77)
        ));

        TextView etiqueta = new TextView(this);
        etiqueta.setText("INFORMACIÓN DEL MOVIMIENTO");
        etiqueta.setTextColor(Color.rgb(67, 224, 160));
        etiqueta.setTextSize(10);
        etiqueta.setTypeface(null, Typeface.BOLD);
        etiqueta.setLetterSpacing(0.08f);
        contenido.addView(etiqueta);

        TextView titulo = new TextView(this);
        titulo.setText(nombre);
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(25);
        titulo.setTypeface(null, Typeface.BOLD);
        titulo.setPadding(0, 5, 0, 15);
        contenido.addView(titulo);

        LinearLayout etiquetas = new LinearLayout(this);
        etiquetas.setOrientation(LinearLayout.HORIZONTAL);
        etiquetas.setGravity(Gravity.CENTER_VERTICAL);

        TextView chipTipo = crearEtiquetaMovimiento(
                tipo.toUpperCase(Locale.ROOT),
                colorTipo
        );

        TextView chipCategoria = crearEtiquetaMovimiento(
                categoria,
                colorCategoria
        );

        LinearLayout.LayoutParams chipParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        chipParams.setMargins(0, 0, 8, 0);

        etiquetas.addView(chipTipo, chipParams);

        LinearLayout.LayoutParams categoriaParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        etiquetas.addView(chipCategoria, categoriaParams);

        contenido.addView(etiquetas);

        View separador = new View(this);
        separador.setBackgroundColor(Color.rgb(55, 61, 77));

        LinearLayout.LayoutParams separadorParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                );
        separadorParams.setMargins(0, 20, 0, 12);
        contenido.addView(separador, separadorParams);

        agregarDatoMovimiento(contenido, "POTENCIA", potencia);
        agregarDatoMovimiento(contenido, "PRECISIÓN", precision);
        agregarDatoMovimiento(contenido, "PP", String.valueOf(movimiento.getPp()));
        agregarDatoMovimiento(contenido, "PRIORIDAD",
                String.valueOf(movimiento.getPriority()));

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setView(contenido)
                .setPositiveButton("CERRAR", (d, which) -> d.dismiss())
                .create();

        dialogo.setOnShowListener(d -> {
            if (dialogo.getWindow() != null) {
                dialogo.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );
            }

            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.rgb(67, 224, 160));
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTypeface(null, Typeface.BOLD);
        });

        dialogo.show();
    }

    private TextView crearEtiquetaMovimiento(String texto, int color) {

        TextView etiqueta = new TextView(this);
        etiqueta.setText(texto);
        etiqueta.setTextColor(Color.WHITE);
        etiqueta.setTextSize(10);
        etiqueta.setTypeface(null, Typeface.BOLD);
        etiqueta.setPadding(12, 8, 12, 8);
        etiqueta.setBackground(crearFondoRedondeado(color, 30, color));

        return etiqueta;
    }

    private void agregarDatoMovimiento(
            LinearLayout contenedor,
            String etiqueta,
            String valor) {

        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setPadding(0, 10, 0, 10);

        TextView nombre = new TextView(this);
        nombre.setText(etiqueta);
        nombre.setTextColor(Color.rgb(165, 174, 192));
        nombre.setTextSize(11);
        nombre.setTypeface(null, Typeface.BOLD);
        nombre.setLetterSpacing(0.05f);

        LinearLayout.LayoutParams nombreParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        fila.addView(nombre, nombreParams);

        TextView dato = new TextView(this);
        dato.setText(valor);
        dato.setTextColor(Color.WHITE);
        dato.setTextSize(16);
        dato.setTypeface(null, Typeface.BOLD);
        fila.addView(dato);

        contenedor.addView(fila);
    }

    private GradientDrawable crearFondoRedondeado(
            int color,
            int radio,
            int colorBorde) {

        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(color);
        fondo.setCornerRadius(radio);
        fondo.setStroke(1, colorBorde);
        return fondo;
    }

    private int obtenerColorTipoMovimiento(String tipo) {

        switch (tipo.toLowerCase(Locale.ROOT)) {
            case "fire": return Color.rgb(220, 91, 65);
            case "water": return Color.rgb(65, 139, 220);
            case "grass": return Color.rgb(83, 170, 105);
            case "electric": return Color.rgb(213, 174, 55);
            case "ice": return Color.rgb(91, 183, 195);
            case "fighting": return Color.rgb(184, 75, 65);
            case "poison": return Color.rgb(155, 91, 180);
            case "ground": return Color.rgb(185, 145, 83);
            case "flying": return Color.rgb(125, 145, 205);
            case "psychic": return Color.rgb(218, 91, 135);
            case "bug": return Color.rgb(145, 165, 65);
            case "rock": return Color.rgb(160, 145, 95);
            case "ghost": return Color.rgb(105, 91, 155);
            case "dragon": return Color.rgb(105, 85, 205);
            case "dark": return Color.rgb(95, 85, 85);
            case "steel": return Color.rgb(130, 145, 160);
            case "fairy": return Color.rgb(205, 135, 175);
            default: return Color.rgb(115, 125, 145);
        }
    }

    private void cargarMovimientosActuales() {

        if (pokemonActual != null) {
            mostrarMovimientos(pokemonActual);
        }
    }

    private void agregarTextoSimple(
            LinearLayout contenedor,
            String texto) {

        TextView elemento = new TextView(this);
        elemento.setText(texto);
        elemento.setTextColor(Color.WHITE);
        elemento.setTextSize(14);
        elemento.setPadding(0, 8, 0, 8);

        contenedor.addView(elemento);
    }

    // ---------------------------------------------------------
    // CADENA EVOLUTIVA
    // ---------------------------------------------------------

    private void cargarCadenaEvolutiva(int id) {

        Log.d(TAG, "Consultando especie del Pokémon: " + id);

        api.getPokemonSpecies(id).enqueue(
                new Callback<PokemonSpecies>() {

                    @Override
                    public void onResponse(
                            Call<PokemonSpecies> call,
                            Response<PokemonSpecies> response) {

                        if (!response.isSuccessful() || response.body() == null) {

                            Log.e(TAG, "Error HTTP consultando especie: "
                                    + response.code());

                            mostrarMensajeEvolucion(
                                    "No se pudo cargar la información evolutiva"
                            );

                            return;
                        }

                        PokemonSpecies especie = response.body();
                        mostrarDatosEspecie(especie);

                        if (especie.getEvolution_chain() == null
                                || especie.getEvolution_chain().getUrl() == null) {

                            mostrarMensajeEvolucion(
                                    "Este Pokémon no tiene evoluciones registradas"
                            );

                            return;
                        }

                        String url = especie.getEvolution_chain().getUrl();

                        int idCadena = obtenerIdDeUrl(url);

                        Log.d(TAG, "URL cadena evolutiva: " + url);
                        Log.d(TAG, "ID cadena evolutiva: " + idCadena);

                        if (idCadena <= 0) {

                            mostrarMensajeEvolucion(
                                    "No se encontró la cadena evolutiva"
                            );

                            return;
                        }

                        consultarEvoluciones(idCadena);
                    }

                    @Override
                    public void onFailure(
                            Call<PokemonSpecies> call,
                            Throwable t) {

                        Log.e(TAG, "Error consultando especie", t);

                        mostrarMensajeEvolucion(
                                "Error de conexión al consultar evoluciones"
                        );
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // DATOS DE LA ESPECIE
    // ---------------------------------------------------------

    private void mostrarDatosEspecie(PokemonSpecies especie) {

        layoutDatosEspecie.removeAllViews();

        if (especie == null) {
            agregarTextoSimple(layoutDatosEspecie,
                    "No hay información de especie disponible.");
            return;
        }

        String habitat = "No registrado";
        if (especie.getHabitat() != null
                && especie.getHabitat().getName() != null) {
            habitat = capitalizar(especie.getHabitat().getName());
        }

        String generacion = "No registrada";
        if (especie.getGeneration() != null
                && especie.getGeneration().getName() != null) {
            generacion = traducirGeneracion(especie.getGeneration().getName());
        }

        String captura = especie.getCapture_rate() >= 0
                ? especie.getCapture_rate() + " / 255"
                : "No registrada";

        String crecimiento = "No registrado";
        if (especie.getGrowth_rate() != null
                && especie.getGrowth_rate().getName() != null) {
            crecimiento = traducirCrecimiento(
                    especie.getGrowth_rate().getName()
            );
        }

        String descripcion = obtenerDescripcionEspanol(especie);
        if (descripcion.isEmpty()) {
            descripcion = "No hay una descripción en español disponible.";
        }

        agregarDatoEspecie("HÁBITAT", habitat);
        agregarDatoEspecie("REGIÓN / GENERACIÓN", generacion);
        agregarDatoEspecie("TASA DE CAPTURA", captura);
        agregarDatoEspecie("CRECIMIENTO", crecimiento);
        agregarDatoEspecie("DESCRIPCIÓN", descripcion);
    }

    private void agregarDatoEspecie(String titulo, String valor) {

        LinearLayout bloque = new LinearLayout(this);
        bloque.setOrientation(LinearLayout.VERTICAL);
        bloque.setPadding(0, 9, 0, 9);

        TextView etiqueta = new TextView(this);
        etiqueta.setText(titulo);
        etiqueta.setTextColor(Color.rgb(67, 224, 160));
        etiqueta.setTextSize(11);
        etiqueta.setTypeface(null, Typeface.BOLD);
        etiqueta.setLetterSpacing(0.05f);
        bloque.addView(etiqueta);

        TextView contenido = new TextView(this);
        contenido.setText(valor);
        contenido.setTextColor(Color.WHITE);
        contenido.setTextSize(13);
        contenido.setLineSpacing(3, 1.0f);
        contenido.setPadding(0, 4, 0, 0);
        bloque.addView(contenido);

        layoutDatosEspecie.addView(bloque);

        View separador = new View(this);
        separador.setBackgroundColor(Color.rgb(45, 48, 60));

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                );
        layoutDatosEspecie.addView(separador, parametros);
    }

    private String obtenerDescripcionEspanol(PokemonSpecies especie) {

        if (especie.getFlavor_text_entries() == null) {
            return "";
        }

        for (PokemonSpecies.FlavorTextEntry entrada :
                especie.getFlavor_text_entries()) {

            if (entrada == null
                    || entrada.getLanguage() == null
                    || entrada.getLanguage().getName() == null
                    || entrada.getFlavor_text() == null) {
                continue;
            }

            if ("es".equals(entrada.getLanguage().getName())) {
                return entrada.getFlavor_text()
                        .replace("\\f", " ")
                        .replace("\\n", " ")
                        .replace("\\r", " ")
                        .replaceAll("\\s+", " ")
                        .trim();
            }
        }

        return "";
    }

    private String traducirGeneracion(String generacion) {

        switch (generacion) {
            case "generation-i": return "Kanto · Primera generación";
            case "generation-ii": return "Johto · Segunda generación";
            case "generation-iii": return "Hoenn · Tercera generación";
            case "generation-iv": return "Sinnoh · Cuarta generación";
            case "generation-v": return "Teselia · Quinta generación";
            case "generation-vi": return "Kalos · Sexta generación";
            case "generation-vii": return "Alola · Séptima generación";
            case "generation-viii": return "Galar · Octava generación";
            case "generation-ix": return "Paldea · Novena generación";
            default: return capitalizar(generacion.replace("-", " "));
        }
    }

    private String traducirCrecimiento(String crecimiento) {

        switch (crecimiento) {
            case "slow": return "Lento";
            case "medium": return "Medio";
            case "fast": return "Rápido";
            case "medium-slow": return "Medio-lento";
            case "slow-then-very-fast": return "Lento y luego muy rápido";
            case "fast-then-very-slow": return "Rápido y luego muy lento";
            default: return capitalizar(crecimiento.replace("-", " "));
        }
    }

    private void consultarEvoluciones(int idCadena) {

        Log.d(TAG, "Consultando cadena evolutiva: " + idCadena);

        api.getEvolutionChain(idCadena).enqueue(
                new Callback<EvolutionChainResponse>() {

                    @Override
                    public void onResponse(
                            Call<EvolutionChainResponse> call,
                            Response<EvolutionChainResponse> response) {

                        if (!response.isSuccessful() || response.body() == null) {

                            Log.e(TAG, "Error HTTP cadena: " + response.code());

                            mostrarMensajeEvolucion(
                                    "No se pudo obtener la cadena evolutiva"
                            );

                            return;
                        }

                        EvolutionChainResponse datos = response.body();

                        if (datos.getChain() == null) {

                            mostrarMensajeEvolucion(
                                    "No se encontraron evoluciones"
                            );

                            return;
                        }

                        Log.d(TAG, "Cadena evolutiva recibida correctamente");

                        layoutEvoluciones.removeAllViews();

                        crearArbolEvolutivo(datos.getChain());
                    }

                    @Override
                    public void onFailure(
                            Call<EvolutionChainResponse> call,
                            Throwable t) {

                        Log.e(TAG, "Error de conexión cadena", t);

                        mostrarMensajeEvolucion(
                                "Error al cargar las evoluciones"
                        );
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // CONSTRUIR ARBOL EVOLUTIVO
    // ---------------------------------------------------------

    private void crearArbolEvolutivo(
            EvolutionChainResponse.ChainLink raiz) {

        if (raiz == null) {
            mostrarMensajeEvolucion("No hay información evolutiva");
            return;
        }

        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(8, 8, 8, 8);

        layoutEvoluciones.addView(contenedor);

        List<EvolutionChainResponse.ChainLink> nivel =
                new ArrayList<>();

        nivel.add(raiz);

        crearNivelEvolutivo(contenedor, nivel);
    }

    private void crearNivelEvolutivo(
            LinearLayout contenedor,
            List<EvolutionChainResponse.ChainLink> nivel) {

        if (nivel == null || nivel.isEmpty()) {
            return;
        }

        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setPadding(0, 8, 0, 8);

        contenedor.addView(fila);

        List<EvolutionChainResponse.ChainLink> siguienteNivel =
                new ArrayList<>();

        boolean primero = true;

        for (EvolutionChainResponse.ChainLink nodo : nivel) {

            if (nodo == null || nodo.getSpecies() == null) {
                continue;
            }

            int id = nodo.getSpecies().getId();

            if (!contienePokemonKanto(id)) {
                continue;
            }

            if (!primero) {
                TextView flecha = new TextView(this);
                flecha.setText("  →  ");
                flecha.setTextColor(Color.rgb(67, 224, 160));
                flecha.setTextSize(20);
                fila.addView(flecha);
            }

            crearTarjetaEvolucion(fila, nodo);

            primero = false;

            if (nodo.getEvolves_to() != null) {

                for (EvolutionChainResponse.ChainLink hijo :
                        nodo.getEvolves_to()) {

                    if (hijo != null
                            && hijo.getSpecies() != null
                            && contienePokemonKanto(
                            hijo.getSpecies().getId())) {

                        siguienteNivel.add(hijo);
                    }
                }
            }
        }

        if (!siguienteNivel.isEmpty()) {

            TextView separador = new TextView(this);
            separador.setText("↓");
            separador.setTextColor(Color.rgb(67, 224, 160));
            separador.setTextSize(22);
            separador.setGravity(Gravity.CENTER);
            separador.setPadding(0, 4, 0, 4);

            contenedor.addView(separador);

            crearNivelEvolutivo(contenedor, siguienteNivel);
        }
    }

    // ---------------------------------------------------------
    // TARJETA DE CADA EVOLUCION
    // ---------------------------------------------------------

    private void crearTarjetaEvolucion(
            LinearLayout contenedor,
            EvolutionChainResponse.ChainLink nodo) {

        int id = nodo.getSpecies().getId();

        String nombre = capitalizar(
                nodo.getSpecies().getName()
        );

        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setGravity(Gravity.CENTER);
        tarjeta.setPadding(12, 12, 12, 12);
        tarjeta.setBackgroundColor(Color.rgb(25, 28, 39));

        LinearLayout.LayoutParams tarjetaParams =
                new LinearLayout.LayoutParams(
                        180,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        tarjetaParams.setMargins(5, 5, 5, 5);
        tarjeta.setLayoutParams(tarjetaParams);

        ImageView imagen = new ImageView(this);

        LinearLayout.LayoutParams imagenParams =
                new LinearLayout.LayoutParams(110, 110);

        imagen.setLayoutParams(imagenParams);

        String urlImagen =
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
                        + id + ".png";

        Glide.with(this)
                .load(urlImagen)
                .centerInside()
                .into(imagen);

        tarjeta.addView(imagen);

        TextView numero = new TextView(this);
        numero.setText(String.format(Locale.getDefault(), "#%03d", id));
        numero.setTextColor(Color.rgb(67, 224, 160));
        numero.setTextSize(12);
        numero.setGravity(Gravity.CENTER);

        tarjeta.addView(numero);

        TextView nombrePokemon = new TextView(this);
        nombrePokemon.setText(nombre);
        nombrePokemon.setTextColor(Color.WHITE);
        nombrePokemon.setTextSize(15);
        nombrePokemon.setTypeface(null, Typeface.BOLD);
        nombrePokemon.setGravity(Gravity.CENTER);

        tarjeta.addView(nombrePokemon);

        String condicion = obtenerCondicion(nodo);

        if (!condicion.isEmpty()) {

            TextView textoCondicion = new TextView(this);
            textoCondicion.setText(condicion);
            textoCondicion.setTextColor(Color.LTGRAY);
            textoCondicion.setTextSize(11);
            textoCondicion.setGravity(Gravity.CENTER);
            textoCondicion.setPadding(0, 6, 0, 0);

            tarjeta.addView(textoCondicion);
        }

        tarjeta.setOnClickListener(v -> {

            if (id == pokemonId) {
                return;
            }

            android.content.Intent intent =
                    new android.content.Intent(
                            DetailActivity.this,
                            DetailActivity.class
                    );

            intent.putExtra("pokemon_id", id);
            startActivity(intent);
        });

        contenedor.addView(tarjeta);
    }

    // ---------------------------------------------------------
    // CONDICIONES DE EVOLUCION
    // ---------------------------------------------------------

    private String obtenerCondicion(
            EvolutionChainResponse.ChainLink nodo) {

        StringBuilder resultado = new StringBuilder();

        if (nodo.getEvolution_details() == null
                || nodo.getEvolution_details().isEmpty()) {

            return "";
        }

        for (EvolutionChainResponse.EvolutionDetail detalle :
                nodo.getEvolution_details()) {

            if (detalle == null) {
                continue;
            }

            if (detalle.getMin_level() > 0) {
                agregarCondicion(
                        resultado,
                        "Nivel " + detalle.getMin_level()
                );
            }

            if (detalle.getItem() != null
                    && detalle.getItem().getName() != null) {

                agregarCondicion(
                        resultado,
                        "Objeto: " +
                                capitalizar(
                                        detalle.getItem().getName()
                                                .replace("-", " ")
                                )
                );
            }

            if (detalle.getTrigger() != null) {

                String trigger = detalle.getTrigger().getName();

                if (trigger != null) {

                    switch (trigger) {

                        case "level-up":
                            if (detalle.getMin_level() <= 0) {
                                agregarCondicion(resultado, "Subir de nivel");
                            }
                            break;

                        case "trade":
                            agregarCondicion(resultado, "Intercambio");
                            break;

                        case "use-item":
                            if (detalle.getItem() == null) {
                                agregarCondicion(resultado, "Usar objeto");
                            }
                            break;

                        case "shed":
                            agregarCondicion(resultado, "Evolución especial");
                            break;

                        default:
                            agregarCondicion(
                                    resultado,
                                    capitalizar(trigger.replace("-", " "))
                            );
                            break;
                    }
                }
            }

            if (detalle.getMin_happiness() > 0) {
                agregarCondicion(resultado, "Felicidad alta");
            }

            if (detalle.getMin_beauty() > 0) {
                agregarCondicion(resultado, "Belleza alta");
            }

            if (detalle.getMin_affection() > 0) {
                agregarCondicion(resultado, "Afecto alto");
            }

            if (detalle.getTime_of_day() != null
                    && !detalle.getTime_of_day().isEmpty()) {

                agregarCondicion(
                        resultado,
                        "Durante " + traducirMomento(detalle.getTime_of_day())
                );
            }

            if (detalle.getKnown_move_type() != null
                    && detalle.getKnown_move_type().getName() != null) {

                agregarCondicion(
                        resultado,
                        "Movimiento tipo " +
                                traducirTipo(
                                        detalle.getKnown_move_type().getName()
                                )
                );
            }

            if (detalle.getLocation() != null
                    && detalle.getLocation().getName() != null) {

                agregarCondicion(
                        resultado,
                        "Lugar: " +
                                capitalizar(
                                        detalle.getLocation()
                                                .getName()
                                                .replace("-", " ")
                                )
                );
            }

            if (detalle.getGender() != null) {

                if (detalle.getGender() == 1) {
                    agregarCondicion(resultado, "Hembra");
                } else if (detalle.getGender() == 2) {
                    agregarCondicion(resultado, "Macho");
                }
            }
        }

        return resultado.toString();
    }

    private void agregarCondicion(
            StringBuilder resultado,
            String texto) {

        if (texto == null || texto.trim().isEmpty()) {
            return;
        }

        if (resultado.length() > 0) {
            resultado.append(" / ");
        }

        resultado.append(texto);
    }

    // ---------------------------------------------------------
    // UTILIDADES
    // ---------------------------------------------------------

    private boolean contienePokemonKanto(int id) {
        return id >= 1 && id <= 151;
    }

    private int obtenerIdDeUrl(String url) {

        if (url == null || url.isEmpty()) {
            return 0;
        }

        String[] partes = url.split("/");

        for (int i = partes.length - 1; i >= 0; i--) {

            if (!partes[i].isEmpty()) {

                try {
                    return Integer.parseInt(partes[i]);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }

        return 0;
    }

    private void mostrarMensajeEvolucion(String mensaje) {

        layoutEvoluciones.removeAllViews();

        TextView texto = new TextView(this);
        texto.setText(mensaje);
        texto.setTextColor(Color.LTGRAY);
        texto.setTextSize(13);
        texto.setGravity(Gravity.CENTER);
        texto.setPadding(12, 16, 12, 16);

        layoutEvoluciones.addView(texto);
    }

    private String capitalizar(String texto) {

        if (texto == null || texto.isEmpty()) {
            return "";
        }

        String limpio = texto.replace("-", " ");

        return limpio.substring(0, 1).toUpperCase(Locale.ROOT)
                + limpio.substring(1);
    }

    private String traducirTipo(String tipo) {

        switch (tipo.toLowerCase(Locale.ROOT)) {
            case "normal": return "Normal";
            case "fire": return "Fuego";
            case "water": return "Agua";
            case "electric": return "Eléctrico";
            case "grass": return "Planta";
            case "ice": return "Hielo";
            case "fighting": return "Lucha";
            case "poison": return "Veneno";
            case "ground": return "Tierra";
            case "flying": return "Volador";
            case "psychic": return "Psíquico";
            case "bug": return "Bicho";
            case "rock": return "Roca";
            case "ghost": return "Fantasma";
            case "dragon": return "Dragón";
            case "dark": return "Siniestro";
            case "steel": return "Acero";
            case "fairy": return "Hada";
            default: return capitalizar(tipo);
        }
    }

    private String traducirEstadistica(String estadistica) {

        switch (estadistica) {
            case "hp": return "PS";
            case "attack": return "Ataque";
            case "defense": return "Defensa";
            case "special-attack": return "At. Especial";
            case "special-defense": return "Def. Especial";
            case "speed": return "Velocidad";
            default: return capitalizar(estadistica);
        }
    }

    private String traducirMomento(String momento) {

        switch (momento) {
            case "day": return "el día";
            case "night": return "la noche";
            default: return momento;
        }
    }
}