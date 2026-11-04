package com.example.pokedex.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pokedex.R;
import com.example.pokedex.database.DatabaseHelper;
import com.example.pokedex.model.PokemonItem;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder> {

    public interface OnPokemonClickListener {
        void onPokemonClick(PokemonItem pokemon);
    }

    private final List<PokemonItem> pokemonList;
    private final DatabaseHelper databaseHelper;
    private final OnPokemonClickListener listener;
    private final Runnable onFavoritesChanged;

    private final Map<Integer, List<String>> tiposPokemon = new HashMap<>();

    public PokemonAdapter(
            List<PokemonItem> pokemonList,
            DatabaseHelper databaseHelper,
            OnPokemonClickListener listener,
            Runnable onFavoritesChanged
    ) {
        this.pokemonList = pokemonList;
        this.databaseHelper = databaseHelper;
        this.listener = listener;
        this.onFavoritesChanged = onFavoritesChanged;
    }

    @NonNull
    @Override
    public PokemonViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pokemon, parent, false);

        return new PokemonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PokemonViewHolder holder,
            int position
    ) {
        PokemonItem pokemon = pokemonList.get(position);

        int id = pokemon.getId();
        String nombre = pokemon.getName();

        holder.txtNumero.setText(
                String.format(Locale.ROOT, "#%03d", id)
        );

        holder.txtNombre.setText(
                capitalizar(nombre)
        );

        String imagenUrl =
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
                        + id + ".png";

        Glide.with(holder.itemView.getContext())
                .load(imagenUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.imgPokemon);

        // MOSTRAR TIPOS

        List<String> tipos = tiposPokemon.get(id);

        if (tipos != null && !tipos.isEmpty()) {

            holder.txtTipo1.setText(capitalizar(tipos.get(0)));
            holder.txtTipo1.setVisibility(View.VISIBLE);

            holder.txtTipo1.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            colorTipo(tipos.get(0))
                    )
            );

            if (tipos.size() > 1) {

                holder.txtTipo2.setText(capitalizar(tipos.get(1)));
                holder.txtTipo2.setVisibility(View.VISIBLE);

                holder.txtTipo2.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                colorTipo(tipos.get(1))
                        )
                );

            } else {
                holder.txtTipo2.setVisibility(View.GONE);
            }

        } else {
            holder.txtTipo1.setVisibility(View.GONE);
            holder.txtTipo2.setVisibility(View.GONE);
        }

        // ACTUALIZAR ESTRELLA

        actualizarEstrella(holder, id);

        // AGREGAR O QUITAR FAVORITO

        holder.txtFavorito.setOnClickListener(v -> {

            boolean esFavorito = databaseHelper.esFavorito(id);

            if (esFavorito) {

                databaseHelper.eliminarFavorito(id);

                Toast.makeText(
                        holder.itemView.getContext(),
                        nombre + " eliminado de favoritos",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                databaseHelper.agregarFavorito(
                        id,
                        nombre,
                        imagenUrl
                );

                Toast.makeText(
                        holder.itemView.getContext(),
                        nombre + " agregado a favoritos",
                        Toast.LENGTH_SHORT
                ).show();
            }

            actualizarEstrella(holder, id);

            if (onFavoritesChanged != null) {
                onFavoritesChanged.run();
            }
        });

        // ABRIR DETALLE

        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {
                listener.onPokemonClick(pokemon);
            }
        });
    }

    // ACTUALIZAR ESTRELLA

    private void actualizarEstrella(
            PokemonViewHolder holder,
            int id
    ) {

        boolean favorito = databaseHelper.esFavorito(id);

        if (favorito) {

            holder.txtFavorito.setText("★");
            holder.txtFavorito.setTextColor(Color.YELLOW);
            holder.txtFavorito.setContentDescription(
                    "Quitar de favoritos"
            );

        } else {

            holder.txtFavorito.setText("☆");
            holder.txtFavorito.setTextColor(Color.WHITE);
            holder.txtFavorito.setContentDescription(
                    "Agregar a favoritos"
            );
        }
    }

    // ACTUALIZAR LISTA

    public void actualizarLista(List<PokemonItem> nuevaLista) {
        // La lista es compartida con MainActivity.
        notifyDataSetChanged();
    }

    // ACTUALIZAR TIPOS

    public void actualizarTipos(Map<Integer, List<String>> nuevosTipos) {

        tiposPokemon.clear();
        tiposPokemon.putAll(nuevosTipos);

        notifyDataSetChanged();
    }

    // CAPITALIZAR NOMBRE

    private String capitalizar(String texto) {

        if (texto == null || texto.isEmpty()) {
            return "";
        }

        return texto.substring(0, 1).toUpperCase(Locale.ROOT)
                + texto.substring(1);
    }

    // COLORES DE TIPOS

    private int colorTipo(String tipo) {

        switch (tipo.toLowerCase(Locale.ROOT)) {

            case "fire": return Color.rgb(238, 129, 48);
            case "water": return Color.rgb(99, 144, 240);
            case "grass": return Color.rgb(122, 199, 76);
            case "electric": return Color.rgb(247, 208, 44);
            case "ice": return Color.rgb(150, 217, 214);
            case "fighting": return Color.rgb(194, 46, 40);
            case "poison": return Color.rgb(163, 62, 161);
            case "ground": return Color.rgb(226, 191, 101);
            case "flying": return Color.rgb(169, 143, 243);
            case "psychic": return Color.rgb(249, 85, 135);
            case "bug": return Color.rgb(166, 185, 26);
            case "rock": return Color.rgb(182, 161, 54);
            case "ghost": return Color.rgb(115, 87, 151);
            case "dragon": return Color.rgb(111, 53, 252);
            case "dark": return Color.rgb(112, 87, 70);
            case "steel": return Color.rgb(183, 183, 206);
            case "fairy": return Color.rgb(214, 133, 173);

            default: return Color.GRAY;
        }
    }

    @Override
    public int getItemCount() {
        return pokemonList.size();
    }

    // VISTA DE CADA POKEMON

    static class PokemonViewHolder extends RecyclerView.ViewHolder {

        TextView txtNumero;
        TextView txtNombre;
        TextView txtTipo1;
        TextView txtTipo2;
        TextView txtFavorito;

        ImageView imgPokemon;

        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNumero = itemView.findViewById(R.id.txtNumero);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtTipo1 = itemView.findViewById(R.id.txtTipo1);
            txtTipo2 = itemView.findViewById(R.id.txtTipo2);
            txtFavorito = itemView.findViewById(R.id.txtFavorito);

            imgPokemon = itemView.findViewById(R.id.imgPokemon);
        }
    }
}
