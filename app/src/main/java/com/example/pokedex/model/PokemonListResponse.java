    package com.example.pokedex.model;

import java.util.List;

public class PokemonListResponse {

    private int count;
    private String next;
    private String previous;
    private List<PokemonItem> results;

    public int getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<PokemonItem> getResults() {
        return results;
    }
}
