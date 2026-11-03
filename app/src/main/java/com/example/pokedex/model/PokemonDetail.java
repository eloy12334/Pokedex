package com.example.pokedex.model;

import java.util.List;

public class PokemonDetail {

    private int id;
    private String name;
    private int height;
    private int weight;
    private Sprites sprites;
    private List<TypeSlot> types;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getHeight() {
        return height;
    }

    public int getWeight() {
        return weight;
    }

    public Sprites getSprites() {
        return sprites;
    }

    public List<TypeSlot> getTypes() {
        return types;
    }

    public static class Sprites {
        private String front_default;

        public String getFront_default() {
            return front_default;
        }
    }

    public static class TypeSlot {
        private TypeInfo type;

        public TypeInfo getType() {
            return type;
        }
    }

    public static class TypeInfo {
        private String name;

        public String getName() {
            return name;
        }
    }
}
