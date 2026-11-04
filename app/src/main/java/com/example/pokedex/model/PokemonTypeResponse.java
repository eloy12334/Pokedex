package com.example.pokedex.model;

import java.util.List;

public class PokemonTypeResponse {

    private List<PokemonEntry> pokemon;

    public List<PokemonEntry> getPokemon() {
        return pokemon;
    }

    public static class PokemonEntry {

        private PokemonItem pokemon;

        public PokemonItem getPokemon() {
            return pokemon;
        }
    }
}
