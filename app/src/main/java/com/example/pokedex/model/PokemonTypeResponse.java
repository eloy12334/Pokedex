package com.example.pokedex.model;

import java.util.List;

public class PokemonTypeResponse {

    private List<PokemonEntry> pokemon;
    private DamageRelations damage_relations;

    public List<PokemonEntry> getPokemon() {
        return pokemon;
    }

    public DamageRelations getDamageRelations() {
        return damage_relations;
    }

    public static class PokemonEntry {

        private PokemonItem pokemon;

        public PokemonItem getPokemon() {
            return pokemon;
        }
    }

    public static class DamageRelations {

        private List<TypeRelation> double_damage_to;
        private List<TypeRelation> half_damage_to;
        private List<TypeRelation> no_damage_to;

        private List<TypeRelation> double_damage_from;
        private List<TypeRelation> half_damage_from;
        private List<TypeRelation> no_damage_from;

        public List<TypeRelation> getDoubleDamageTo() {
            return double_damage_to;
        }

        public List<TypeRelation> getHalfDamageTo() {
            return half_damage_to;
        }

        public List<TypeRelation> getNoDamageTo() {
            return no_damage_to;
        }

        public List<TypeRelation> getDoubleDamageFrom() {
            return double_damage_from;
        }

        public List<TypeRelation> getHalfDamageFrom() {
            return half_damage_from;
        }

        public List<TypeRelation> getNoDamageFrom() {
            return no_damage_from;
        }
    }

    public static class TypeRelation {

        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }
}
