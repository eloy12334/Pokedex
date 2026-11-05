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

public class PokemonSelectAdapter extends RecyclerView.Adapter<PokemonSelectAdapter.Holder> {
    public interface OnSelectListener { void onSelect(PokemonItem pokemon); }
    private final List<PokemonItem> original;
    private final List<PokemonItem> filtered;
    private final OnSelectListener listener;

    public PokemonSelectAdapter(List<PokemonItem> items, OnSelectListener listener) {
        this.original = new ArrayList<>(items);
        this.filtered = new ArrayList<>(items);
        this.listener = listener;
    }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pokemon_selector, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        PokemonItem p = filtered.get(position);
        int id = p.getId();
        h.number.setText(String.format(Locale.ROOT, "#%03d", id));
        h.name.setText(capitalizar(p.getName()));
        String url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + id + ".png";
        Glide.with(h.image.getContext()).load(url).placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.ic_menu_report_image).into(h.image);
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onSelect(p); });
    }
    public void filtrar(String query) {
        filtered.clear();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        for (PokemonItem p : original) {
            String name = p.getName() == null ? "" : p.getName().toLowerCase(Locale.ROOT);
            String number = String.valueOf(p.getId());
            if (q.isEmpty() || name.contains(q) || number.contains(q) || String.format(Locale.ROOT, "%03d", p.getId()).contains(q)) filtered.add(p);
        }
        notifyDataSetChanged();
    }
    @Override public int getItemCount() { return filtered.size(); }
    private String capitalizar(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0,1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
    static class Holder extends RecyclerView.ViewHolder {
        ImageView image; TextView number, name;
        Holder(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.imgPokemonSelector);
            number = v.findViewById(R.id.txtNumeroSelector);
            name = v.findViewById(R.id.txtNombreSelector);
        }
    }
}
