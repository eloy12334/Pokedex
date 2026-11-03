
package com.example.pokedex.adapter;

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
import java.util.List;
import java.util.Locale;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder> {

    private final List<PokemonItem> pokemonList;
    private final OnPokemonClickListener listener;

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

        holder.tvNombre.setText(
                nombre.substring(0, 1).toUpperCase(Locale.ROOT)
                        + nombre.substring(1)
        );

        holder.tvNumero.setText(
                String.format(Locale.ROOT, "#%03d", id)
        );

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

    public void actualizarLista(List<PokemonItem> nuevaLista) {
        pokemonList.clear();
        pokemonList.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return pokemonList.size();
    }

    public static class PokemonViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPokemon;
        TextView tvNumero;
        TextView tvNombre;

        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPokemon = itemView.findViewById(R.id.imgPokemon);
            tvNumero = itemView.findViewById(R.id.tvNumero);
            tvNombre = itemView.findViewById(R.id.tvNombre);
        }
    }
}
