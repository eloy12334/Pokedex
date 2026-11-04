
package com.example.pokedex.model;

import java.util.List;

public class PokemonDetail {

    private int id;
    private String name;
    private int height;
    private int weight;
    private Sprites sprites;
    private List<TypeSlot> types;
    private List<StatSlot> stats;

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

    public List<StatSlot> getStats() {
        return stats;
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

    public static class StatSlot {
        private int base_stat;
        private StatInfo stat;

        public int getBaseStat() {
            return base_stat;
        }

        public StatInfo getStat() {
            return stat;
        }
    }

    public static class StatInfo {
        private String name;

        public String getName() {
            return name;
        }
    }
}
