
package com.example.pokedex;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokedex.adapter.PokemonAdapter;
import com.example.pokedex.api.ApiClient;
import com.example.pokedex.database.DatabaseHelper;
import com.example.pokedex.model.PokemonItem;
import com.example.pokedex.model.PokemonListResponse;
import com.example.pokedex.model.PokemonTypeResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // COMPONENTES DE LA INTERFAZ

    private RecyclerView rvPokemon;
    private EditText edtBuscarPokemon;
    private ProgressBar progressBar;
    private Spinner spnOrden;

    private TextView txtResultados;
    private TextView txtTotalPokemon;

    private Button btnVolverInicio;
    private Button btnTodos;
    private Button btnPorTipo;
    private Button btnFavoritos;
    private Button btnCuadricula;
    private Button btnLista;
    private Button btnEquipo;
    private Button btnEfectividad;

    // ADAPTADOR Y BASE DE DATOS

    private PokemonAdapter adapter;
    private DatabaseHelper databaseHelper;

    // CONTROL DE FAVORITOS

    private boolean mostrarSoloFavoritos = false;

    // LISTAS DE POKEMON

    private final List<PokemonItem> listaCompleta =
            new ArrayList<>();

    private final List<PokemonItem> listaVisible =
            new ArrayList<>();

    // TIPOS DE CADA POKEMON

    private final Map<Integer, List<String>> mapaTiposPokemon =
            new HashMap<>();

    // FILTRO SELECCIONADO

    private Set<Integer> idsTipoSeleccionado = null;

    // CONFIGURACION DE VISTA

    private boolean modoCuadricula = true;

    // ORDEN DE TIPOS

    // Los 18 tipos oficiales, en el mismo orden usado por los filtros.
    private final String[] ordenTipos = {
            "normal", "fire", "water", "grass", "electric", "ice",
            "fighting", "poison", "ground", "flying", "psychic", "bug",
            "rock", "ghost", "dragon", "dark", "steel", "fairy"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        configurarVentana();

        inicializarVistas();

        // INICIALIZAR SQLITE ANTES DEL ADAPTADOR
        databaseHelper = new DatabaseHelper(this);

        configurarRecyclerView();

        configurarOrdenamiento();

        configurarBotones();

        configurarBusqueda();

        // CARGAR INFORMACION DE POKEAPI
        cargarPokemon();

        cargarTiposPokemon();
    }

    // CONFIGURAR VENTANA

    private void configurarVentana() {

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
    }

    // INICIALIZAR COMPONENTES

    private void inicializarVistas() {

        rvPokemon = findViewById(R.id.rvPokemon);

        edtBuscarPokemon =
                findViewById(R.id.edtBuscarPokemon);

        progressBar =
                findViewById(R.id.progressBar);

        spnOrden =
                findViewById(R.id.spnOrden);

        txtResultados =
                findViewById(R.id.txtResultados);

        txtTotalPokemon =
                findViewById(R.id.txtTotalPokemon);

        btnVolverInicio =
                findViewById(R.id.btnVolverInicio);

        btnTodos =
                findViewById(R.id.btnTodos);

        btnPorTipo =
                findViewById(R.id.btnPorTipo);

        btnFavoritos =
                findViewById(R.id.btnFavoritos);

        btnCuadricula =
                findViewById(R.id.btnCuadricula);

        btnLista =
                findViewById(R.id.btnLista);

        btnEquipo = findViewById(R.id.btnEquipo);
        btnEfectividad = findViewById(R.id.btnEfectividad);
    }

    // CONFIGURAR RECYCLERVIEW

    private void configurarRecyclerView() {

        rvPokemon.setLayoutManager(
                new GridLayoutManager(this, 2)
        );

        adapter = new PokemonAdapter(
                listaVisible,
                databaseHelper,
                pokemon -> {

                    Intent intent = new Intent(
                            MainActivity.this,
                            DetailActivity.class
                    );

                    intent.putExtra(
                            "pokemon_id",
                            pokemon.getId()
                    );

                    startActivity(intent);
                },
                () -> {
                    // ACTUALIZAR SI ESTAMOS VIENDO FAVORITOS
                    if (mostrarSoloFavoritos) {
                        filtrarPokemon(
                                edtBuscarPokemon.getText().toString()
                        );
                    }
                }
        );

        rvPokemon.setAdapter(adapter);
    }

    // CONFIGURAR ORDENAMIENTO

    private void configurarOrdenamiento() {

        String[] opciones = {
                "Número: menor a mayor",
                "Número: mayor a menor",
                "Nombre: A - Z",
                "Nombre: Z - A"
        };

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        opciones
                ) {

                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            android.view.ViewGroup parent) {

                        TextView txt = (TextView) super.getView(
                                position,
                                convertView,
                                parent
                        );

                        txt.setTextColor(Color.WHITE);
                        txt.setTextSize(13);
                        txt.setPadding(8, 0, 8, 0);

                        return txt;
                    }

                    @Override
                    public View getDropDownView(
                            int position,
                            View convertView,
                            android.view.ViewGroup parent) {

                        TextView txt = (TextView) super.getDropDownView(
                                position,
                                convertView,
                                parent
                        );

                        txt.setTextColor(Color.WHITE);
                        txt.setBackgroundColor(0xFF252631);
                        txt.setPadding(15, 15, 15, 15);

                        return txt;
                    }
                };

        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spnOrden.setAdapter(spinnerAdapter);

        spnOrden.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        filtrarPokemon(
                                edtBuscarPokemon.getText().toString()
                        );
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                }
        );
    }

    // CONFIGURAR BOTONES

    private void configurarBotones() {

        // REGRESAR A BIENVENIDA

        btnVolverInicio.setOnClickListener(v -> finish());

        // HERRAMIENTAS DE LA POKEDEX

        btnEquipo.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TeamActivity.class)));

        btnEfectividad.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TypeMatchupActivity.class)));

        // MOSTRAR TODOS

        btnTodos.setOnClickListener(v -> {

            idsTipoSeleccionado = null;

            actualizarColorFiltros();

            filtrarPokemon(
                    edtBuscarPokemon.getText().toString()
            );
        });

        // FILTRAR POR TIPO

        btnPorTipo.setOnClickListener(
                this::mostrarMenuTipos
        );

        // MOSTRAR FAVORITOS

        btnFavoritos.setOnClickListener(v -> {

            mostrarSoloFavoritos = !mostrarSoloFavoritos;

            if (mostrarSoloFavoritos) {
                btnFavoritos.setText("★ Ver todos");
            } else {
                btnFavoritos.setText("★ Favoritos");
            }

            actualizarColorFiltros();

            filtrarPokemon(
                    edtBuscarPokemon.getText().toString()
            );
        });

        // VISTA CUADRICULA

        btnCuadricula.setOnClickListener(v -> {

            modoCuadricula = true;

            rvPokemon.setLayoutManager(
                    new GridLayoutManager(this, 2)
            );

            actualizarColorVista();
        });

        // VISTA LISTA

        btnLista.setOnClickListener(v -> {

            modoCuadricula = false;

            rvPokemon.setLayoutManager(
                    new LinearLayoutManager(this)
            );

            actualizarColorVista();
        });

        actualizarColorFiltros();

        actualizarColorVista();
    }

    // CONFIGURAR BUSCADOR

    private void configurarBusqueda() {

        edtBuscarPokemon.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        filtrarPokemon(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
    }

    // CARGAR LOS 151 POKEMON

    private void cargarPokemon() {

        progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApi()
                .getPokemonList(151, 0)
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

                            // SOLO PRIMERA GENERACION

                            if (listaCompleta.size() > 151) {
                                listaCompleta.subList(
                                        151,
                                        listaCompleta.size()
                                ).clear();
                            }

                            txtTotalPokemon.setText(
                                    listaCompleta.size() + " Pokémon"
                            );

                            filtrarPokemon("");

                            Log.d(
                                    "POKEDEX",
                                    "Pokémon cargados: "
                                            + listaCompleta.size()
                            );

                        } else {

                            mostrarError(
                                    "No se pudieron cargar los Pokémon"
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PokemonListResponse> call,
                            Throwable t) {

                        progressBar.setVisibility(View.GONE);

                        Log.e(
                                "POKEDEX",
                                "Error API: " + t.getMessage()
                        );

                        mostrarError(
                                "Error de conexión con PokéAPI"
                        );
                    }
                });
    }

    // CARGAR LOS TIPOS DE LOS POKEMON

    private void cargarTiposPokemon() {

        // Cargar los 18 tipos, incluidos Siniestro y Acero.
        String[] tipos = ordenTipos.clone();

        for (String tipo : tipos) {

            ApiClient.getApi()
                    .getPokemonByType(tipo)
                    .enqueue(new Callback<PokemonTypeResponse>() {

                        @Override
                        public void onResponse(
                                Call<PokemonTypeResponse> call,
                                Response<PokemonTypeResponse> response) {

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().getPokemon() != null) {

                                for (PokemonTypeResponse.PokemonEntry entry
                                        : response.body().getPokemon()) {

                                    PokemonItem pokemon =
                                            entry.getPokemon();

                                    int id = pokemon.getId();

                                    // SOLO POKEMON DE KANTO

                                    if (id >= 1 && id <= 151) {

                                        List<String> listaTipos =
                                                mapaTiposPokemon.get(id);

                                        if (listaTipos == null) {

                                            listaTipos =
                                                    new ArrayList<>();

                                            mapaTiposPokemon.put(
                                                    id,
                                                    listaTipos
                                            );
                                        }

                                        if (!listaTipos.contains(tipo)) {
                                            listaTipos.add(tipo);
                                        }
                                    }
                                }

                                ordenarTiposPokemon();

                                adapter.actualizarTipos(
                                        mapaTiposPokemon
                                );

                                Log.d(
                                        "POKEDEX_TIPOS",
                                        "Tipos actualizados: "
                                                + mapaTiposPokemon.size()
                                );
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<PokemonTypeResponse> call,
                                Throwable t) {

                            Log.e(
                                    "POKEDEX_TIPOS",
                                    "Error cargando tipos: "
                                            + t.getMessage()
                            );
                        }
                    });
        }
    }

    // ORDENAR TIPOS PARA LAS TARJETAS

    private void ordenarTiposPokemon() {

        for (List<String> tipos : mapaTiposPokemon.values()) {

            Collections.sort(
                    tipos,
                    (tipoA, tipoB) -> Integer.compare(
                            obtenerPosicionTipo(tipoA),
                            obtenerPosicionTipo(tipoB)
                    )
            );
        }
    }

    private int obtenerPosicionTipo(String tipo) {

        for (int i = 0; i < ordenTipos.length; i++) {

            if (ordenTipos[i].equals(tipo)) {
                return i;
            }
        }

        return ordenTipos.length;
    }

    // MOSTRAR MENU DE TIPOS

    private void mostrarMenuTipos(View vista) {

        final String[] nombres = {
                "Normal", "Fuego", "Agua", "Planta",
                "Eléctrico", "Hielo", "Lucha", "Veneno",
                "Tierra", "Volador", "Psíquico", "Bicho",
                "Roca", "Fantasma", "Dragón", "Siniestro",
                "Acero", "Hada"
        };

        final String[] tiposApi = {
                "normal", "fire", "water", "grass",
                "electric", "ice", "fighting", "poison",
                "ground", "flying", "psychic", "bug",
                "rock", "ghost", "dragon", "dark",
                "steel", "fairy"
        };

        final String[] simbolos = {
                "◉", "🔥", "💧", "🌿", "⚡", "❄", "✊", "☠",
                "⛰", "🪽", "🔮", "🐛", "🪨", "👻", "🐉", "🌑",
                "⚙", "✨"
        };

        final int[] colores = {
                0xFFA8A878, 0xFFF08030, 0xFF6890F0, 0xFF78C850,
                0xFFF8D030, 0xFF98D8D8, 0xFFC03028, 0xFFA040A0,
                0xFFE0C068, 0xFFA890F0, 0xFFF85888, 0xFFA8B820,
                0xFFB8A038, 0xFF705898, 0xFF7038F8, 0xFF705848,
                0xFFB8B8D0, 0xFFEE99AC
        };

        android.app.Dialog dialog = new android.app.Dialog(this);
        android.widget.LinearLayout contenedor =
                new android.widget.LinearLayout(this);
        contenedor.setOrientation(android.widget.LinearLayout.VERTICAL);
        contenedor.setPadding(18, 16, 18, 12);
        contenedor.setBackgroundColor(0xFF171923);

        TextView titulo = new TextView(this);
        titulo.setText("FILTRAR POR TIPO");
        titulo.setTextColor(0xFF43E0A0);
        titulo.setTextSize(13);
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);
        titulo.setPadding(8, 8, 8, 16);
        contenedor.addView(titulo);

        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        android.widget.LinearLayout lista =
                new android.widget.LinearLayout(this);
        lista.setOrientation(android.widget.LinearLayout.VERTICAL);

        for (int i = 0; i < nombres.length; i++) {
            final int posicion = i;

            android.widget.LinearLayout fila =
                    new android.widget.LinearLayout(this);
            fila.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            fila.setGravity(android.view.Gravity.CENTER_VERTICAL);
            fila.setPadding(12, 11, 12, 11);
            fila.setBackground(makeTypeMenuBackground());

            TextView simbolo = new TextView(this);
            simbolo.setText(simbolos[i]);
            simbolo.setTextSize(19);
            simbolo.setTextColor(colores[i]);
            simbolo.setGravity(android.view.Gravity.CENTER);
            simbolo.setWidth(42);
            fila.addView(simbolo);

            TextView nombre = new TextView(this);
            nombre.setText(nombres[i]);
            nombre.setTextColor(0xFFF5F5F7);
            nombre.setTextSize(15);
            nombre.setTypeface(null, android.graphics.Typeface.BOLD);
            fila.addView(nombre, new android.widget.LinearLayout.LayoutParams(
                    0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            android.view.View indicador = new android.view.View(this);
            android.graphics.drawable.GradientDrawable punto =
                    new android.graphics.drawable.GradientDrawable();
            punto.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            punto.setColor(colores[i]);
            indicador.setBackground(punto);
            android.widget.LinearLayout.LayoutParams puntoParams =
                    new android.widget.LinearLayout.LayoutParams(9, 9);
            fila.addView(indicador, puntoParams);

            fila.setOnClickListener(v -> {
                dialog.dismiss();
                cargarFiltroTipo(tiposApi[posicion]);
                btnPorTipo.setText("Tipo: " + nombres[posicion]);
            });

            android.widget.LinearLayout.LayoutParams filaParams =
                    new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            filaParams.bottomMargin = 5;
            lista.addView(fila, filaParams);
        }

        TextView quitar = new TextView(this);
        quitar.setText("✕   Quitar filtro");
        quitar.setTextColor(0xFFFF6474);
        quitar.setTextSize(14);
        quitar.setTypeface(null, android.graphics.Typeface.BOLD);
        quitar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        quitar.setPadding(16, 16, 16, 16);
        quitar.setOnClickListener(v -> {
            dialog.dismiss();
            idsTipoSeleccionado = null;
            btnPorTipo.setText("Por tipo");
            actualizarColorFiltros();
            filtrarPokemon(edtBuscarPokemon.getText().toString());
        });

        scroll.addView(lista);
        contenedor.addView(scroll, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        contenedor.addView(quitar);

        dialog.setContentView(contenedor);
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.82),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.72));
        }
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.82),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.72));
        }
    }

    private android.graphics.drawable.Drawable makeTypeMenuBackground() {
        android.graphics.drawable.GradientDrawable fondo =
                new android.graphics.drawable.GradientDrawable();
        fondo.setColor(0xFF222531);
        fondo.setCornerRadius(14 * getResources().getDisplayMetrics().density);
        return fondo;
    }

    // FILTRAR POR TIPO

    private void cargarFiltroTipo(String tipo) {

        progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApi()
                .getPokemonByType(tipo)
                .enqueue(new Callback<PokemonTypeResponse>() {

                    @Override
                    public void onResponse(
                            Call<PokemonTypeResponse> call,
                            Response<PokemonTypeResponse> response) {

                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getPokemon() != null) {

                            Set<Integer> ids = new HashSet<>();

                            for (PokemonTypeResponse.PokemonEntry entry
                                    : response.body().getPokemon()) {

                                int id = entry.getPokemon().getId();

                                if (id >= 1 && id <= 151) {
                                    ids.add(id);
                                }
                            }

                            idsTipoSeleccionado = ids;

                            actualizarColorFiltros();

                            filtrarPokemon(
                                    edtBuscarPokemon.getText().toString()
                            );

                        } else {

                            mostrarError(
                                    "No se pudo aplicar el filtro"
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PokemonTypeResponse> call,
                            Throwable t) {

                        progressBar.setVisibility(View.GONE);

                        mostrarError(
                                "Error al filtrar por tipo"
                        );

                        Log.e(
                                "POKEDEX",
                                "Filtro: " + t.getMessage()
                        );
                    }
                });
    }

    // FILTRAR POR NOMBRE, NUMERO, TIPO Y FAVORITOS

    private void filtrarPokemon(String texto) {

        listaVisible.clear();

        String busqueda = texto.trim().toLowerCase(Locale.ROOT);

        // OBTENER FAVORITOS DESDE SQLITE

        List<Integer> favoritos =
                databaseHelper.obtenerIdsFavoritos();

        for (PokemonItem pokemon : listaCompleta) {

            int id = pokemon.getId();

            String nombre = pokemon.getName()
                    .toLowerCase(Locale.ROOT);

            String numero = String.valueOf(id);

            boolean coincideBusqueda =
                    nombre.contains(busqueda)
                            || numero.contains(busqueda);

            boolean coincideTipo =
                    idsTipoSeleccionado == null
                            || idsTipoSeleccionado.contains(id);

            boolean coincideFavorito =
                    !mostrarSoloFavoritos
                            || favoritos.contains(id);

            if (coincideBusqueda
                    && coincideTipo
                    && coincideFavorito) {

                listaVisible.add(pokemon);
            }
        }

        ordenarLista();

        adapter.actualizarLista(listaVisible);

        txtResultados.setText(
                "Mostrando " + listaVisible.size()
                        + " de " + listaCompleta.size()
                        + " Pokémon"
        );
    }

    // ORDENAR LISTA

    private void ordenarLista() {

        int opcion = spnOrden.getSelectedItemPosition();

        switch (opcion) {

            case 1:

                Collections.sort(
                        listaVisible,
                        (a, b) -> Integer.compare(
                                b.getId(),
                                a.getId()
                        )
                );

                break;

            case 2:

                Collections.sort(
                        listaVisible,
                        Comparator.comparing(
                                PokemonItem::getName
                        )
                );

                break;

            case 3:

                Collections.sort(
                        listaVisible,
                        (a, b) -> b.getName().compareTo(
                                a.getName()
                        )
                );

                break;

            default:

                Collections.sort(
                        listaVisible,
                        Comparator.comparingInt(
                                PokemonItem::getId
                        )
                );

                break;
        }
    }

    // ACTUALIZAR COLORES DE FILTROS

    private void actualizarColorFiltros() {

        btnTodos.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        idsTipoSeleccionado == null
                                ? 0xFFE9273D
                                : 0xFF252631
                )
        );

        btnPorTipo.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        idsTipoSeleccionado != null
                                ? 0xFFE9273D
                                : 0xFF252631
                )
        );

        btnFavoritos.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        mostrarSoloFavoritos
                                ? 0xFFE9273D
                                : 0xFF252631
                )
        );
    }

    // ACTUALIZAR COLORES DE VISTA

    private void actualizarColorVista() {

        btnCuadricula.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        modoCuadricula
                                ? 0xFFE9273D
                                : 0xFF252631
                )
        );

        btnLista.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        !modoCuadricula
                                ? 0xFFE9273D
                                : 0xFF252631
                )
        );
    }

    // MOSTRAR MENSAJES

    private void mostrarError(String mensaje) {

        Toast.makeText(
                this,
                mensaje,
                Toast.LENGTH_LONG
        ).show();
    }

    // CERRAR BASE DE DATOS

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
