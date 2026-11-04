
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

    private final String[] ordenTipos = {
            "normal", "fire", "water", "grass",
            "electric", "ice", "fighting", "poison",
            "ground", "flying", "psychic", "bug",
            "rock", "ghost", "dragon", "fairy"
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

        String[] tipos = {
                "fire", "water", "grass", "electric",
                "psychic", "ice", "dragon", "ghost",
                "bug", "poison", "ground", "flying",
                "fighting", "rock", "normal", "fairy"
        };

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

        PopupMenu menu = new PopupMenu(this, vista);

        String[] nombres = {
                "Normal", "Fuego", "Agua", "Planta",
                "Eléctrico", "Hielo", "Lucha", "Veneno",
                "Tierra", "Volador", "Psíquico", "Bicho",
                "Roca", "Fantasma", "Dragón", "Hada"
        };

        String[] tiposApi = {
                "normal", "fire", "water", "grass",
                "electric", "ice", "fighting", "poison",
                "ground", "flying", "psychic", "bug",
                "rock", "ghost", "dragon", "fairy"
        };

        for (int i = 0; i < nombres.length; i++) {

            menu.getMenu().add(
                    i,
                    i,
                    i,
                    nombres[i]
            );
        }

        menu.getMenu().add(
                100,
                100,
                100,
                "Quitar filtro"
        );

        menu.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == 100) {

                idsTipoSeleccionado = null;

                actualizarColorFiltros();

                filtrarPokemon(
                        edtBuscarPokemon.getText().toString()
                );

                return true;
            }

            cargarFiltroTipo(
                    tiposApi[item.getItemId()]
            );

            return true;
        });

        menu.show();
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
