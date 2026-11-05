package com.example.pokedex.model;

import java.util.List;

public class EvolutionChainResponse {

    private ChainLink chain;

    public ChainLink getChain() {
        return chain;
    }

    public static class ChainLink {

        private Species species;
        private List<ChainLink> evolves_to;
        private List<EvolutionDetail> evolution_details;

        public Species getSpecies() {
            return species;
        }

        public List<ChainLink> getEvolves_to() {
            return evolves_to;
        }

        public List<EvolutionDetail> getEvolution_details() {
            return evolution_details;
        }
    }

    public static class Species {

        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public int getId() {

            if (url == null || url.isEmpty()) {
                return 0;
            }

            String[] partes = url.split("/");

            for (int i = partes.length - 1; i >= 0; i--) {

                if (!partes[i].isEmpty()) {

                    try {
                        return Integer.parseInt(partes[i]);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            }

            return 0;
        }
    }

    public static class EvolutionDetail {

        private int min_level;
        private Item item;
        private Trigger trigger;

        private int min_happiness;
        private int min_beauty;
        private int min_affection;

        private String time_of_day;

        private NamedResource known_move_type;
        private NamedResource location;

        private Integer gender;

        public int getMin_level() {
            return min_level;
        }

        public Item getItem() {
            return item;
        }

        public Trigger getTrigger() {
            return trigger;
        }

        public int getMin_happiness() {
            return min_happiness;
        }

        public int getMin_beauty() {
            return min_beauty;
        }

        public int getMin_affection() {
            return min_affection;
        }

        public String getTime_of_day() {
            return time_of_day;
        }

        public NamedResource getKnown_move_type() {
            return known_move_type;
        }

        public NamedResource getLocation() {
            return location;
        }

        public Integer getGender() {
            return gender;
        }
    }

    public static class NamedResource {

        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }

    public static class Item {

        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }

    public static class Trigger {

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