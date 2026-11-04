package com.example.pokedex.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pokedex.R;
import com.example.pokedex.model.PokemonItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder> {

    private final List<PokemonItem> pokemonList;
    private final OnPokemonClickListener listener;

    private final Map<Integer, List<String>> tiposPokemon =
            new HashMap<>();

    public interface OnPokemonClickListener {
        void onPokemonClick(PokemonItem pokemon);
    }

    public PokemonAdapter(
            List<PokemonItem> pokemonList,
            OnPokemonClickListener listener) {

        this.pokemonList = new ArrayList<>(pokemonList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public PokemonViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pokemon, parent, false);

        return new PokemonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PokemonViewHolder holder, int position) {

        PokemonItem pokemon = pokemonList.get(position);

        int id = pokemon.getId();
        String nombre = pokemon.getName();

        holder.txtNombre.setText(
                nombre.substring(0, 1).toUpperCase(Locale.ROOT)
                        + nombre.substring(1)
        );

        holder.txtNumero.setText(
                String.format(Locale.ROOT, "#%03d", id)
        );

        holder.txtNombre.setTextColor(Color.WHITE);
        holder.txtNumero.setTextColor(0xFFE0E2EB);

        // COLOR PRINCIPAL SEGUN EL PRIMER TIPO
        List<String> tipos = tiposPokemon.get(id);

        String tipoPrincipal =
                (tipos != null && !tipos.isEmpty())
                        ? tipos.get(0) : null;

        int color = obtenerColor(tipoPrincipal);

        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(color);
        fondo.setCornerRadius(dp(holder.itemView, 18));
        fondo.setStroke(
                dp(holder.itemView, 1),
                aclararColor(color)
        );

        holder.itemView.setBackground(fondo);

        // MOSTRAR LOS TIPOS
        mostrarTipos(holder, tipos);

        // IMAGEN OFICIAL
        String imageUrl =
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
                        + id + ".png";

        Glide.with(holder.imgPokemon.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.imgPokemon);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPokemonClick(pokemon);
            }
        });
    }

    private void mostrarTipos(
            PokemonViewHolder holder,
            List<String> tipos) {

        holder.txtTipo1.setVisibility(View.GONE);
        holder.txtTipo2.setVisibility(View.GONE);

        if (tipos == null || tipos.isEmpty()) {
            return;
        }

        // PRIMER TIPO
        String tipo1 = tipos.get(0);

        holder.txtTipo1.setText(traducirTipo(tipo1));
        holder.txtTipo1.setBackground(crearFondoTipo(tipo1));
        holder.txtTipo1.setVisibility(View.VISIBLE);

        // SEGUNDO TIPO
        if (tipos.size() >= 2) {

            String tipo2 = tipos.get(1);

            holder.txtTipo2.setText(traducirTipo(tipo2));
            holder.txtTipo2.setBackground(crearFondoTipo(tipo2));
            holder.txtTipo2.setVisibility(View.VISIBLE);
        }
    }

    public void actualizarLista(List<PokemonItem> nuevaLista) {
        pokemonList.clear();
        pokemonList.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    public void actualizarTipos(
            Map<Integer, List<String>> nuevosTipos) {

        tiposPokemon.clear();

        for (Map.Entry<Integer, List<String>> entry
                : nuevosTipos.entrySet()) {

            tiposPokemon.put(
                    entry.getKey(),
                    new ArrayList<>(entry.getValue())
            );
        }

        notifyDataSetChanged();
    }

    private GradientDrawable crearFondoTipo(String tipo) {

        GradientDrawable fondo = new GradientDrawable();

        fondo.setColor(obtenerColor(tipo));
        fondo.setCornerRadius(dp(null, 15));

        return fondo;
    }

    private int obtenerColor(String tipo) {

        if (tipo == null) {
            return Color.rgb(38, 40, 55);
        }

        switch (tipo) {
            case "grass": return Color.rgb(25, 91, 55);
            case "fire": return Color.rgb(126, 48, 34);
            case "water": return Color.rgb(28, 78, 119);
            case "electric": return Color.rgb(111, 91, 25);
            case "bug": return Color.rgb(83, 101, 28);
            case "poison": return Color.rgb(94, 48, 112);
            case "normal": return Color.rgb(91, 84, 78);
            case "flying": return Color.rgb(67, 72, 125);
            case "ground": return Color.rgb(112, 78, 42);
            case "fairy": return Color.rgb(130, 65, 94);
            case "fighting": return Color.rgb(126, 47, 44);
            case "psychic": return Color.rgb(125, 43, 83);
            case "rock": return Color.rgb(105, 91, 51);
            case "ghost": return Color.rgb(66, 58, 112);
            case "ice": return Color.rgb(35, 102, 112);
            case "dragon": return Color.rgb(67, 49, 130);
            default: return Color.rgb(38, 40, 55);
        }
    }

    private int aclararColor(int color) {

        int rojo = Math.min(255, Color.red(color) + 65);
        int verde = Math.min(255, Color.green(color) + 65);
        int azul = Math.min(255, Color.blue(color) + 65);

        return Color.rgb(rojo, verde, azul);
    }

    private int dp(View view, int valor) {

        float densidad = view != null
                ? view.getResources().getDisplayMetrics().density
                : 1f;

        return (int) (valor * densidad);
    }

    private String traducirTipo(String tipo) {

        switch (tipo) {
            case "grass": return "🌿 Planta";
            case "fire": return "🔥 Fuego";
            case "water": return "💧 Agua";
            case "electric": return "⚡ Eléctrico";
            case "bug": return "🐛 Bicho";
            case "poison": return "☠ Veneno";
            case "normal": return "Normal";
            case "flying": return "🪽 Volador";
            case "ground": return "Tierra";
            case "fairy": return "Hada";
            case "fighting": return "Lucha";
            case "psychic": return "🔮 Psíquico";
            case "rock": return "Roca";
            case "ghost": return "👻 Fantasma";
            case "ice": return "❄ Hielo";
            case "dragon": return "🐉 Dragón";
            default: return tipo;
        }
    }

    @Override
    public int getItemCount() {
        return pokemonList.size();
    }

    public static class PokemonViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgPokemon;

        TextView txtNumero;
        TextView txtNombre;
        TextView txtTipo1;
        TextView txtTipo2;

        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPokemon = itemView.findViewById(R.id.imgPokemon);

            txtNumero = itemView.findViewById(R.id.txtNumero);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtTipo1 = itemView.findViewById(R.id.txtTipo1);
            txtTipo2 = itemView.findViewById(R.id.txtTipo2);
        }
    }
}
