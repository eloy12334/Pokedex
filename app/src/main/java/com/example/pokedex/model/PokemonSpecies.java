package com.example.pokedex.model;

import java.util.List;

public class PokemonSpecies {

    private EvolutionChain evolution_chain;
    private NamedResource habitat;
    private NamedResource generation;
    private NamedResource growth_rate;
    private int capture_rate;
    private List<FlavorTextEntry> flavor_text_entries;

    public EvolutionChain getEvolution_chain() {
        return evolution_chain;
    }

    public NamedResource getHabitat() {
        return habitat;
    }

    public NamedResource getGeneration() {
        return generation;
    }

    public NamedResource getGrowth_rate() {
        return growth_rate;
    }

    public int getCapture_rate() {
        return capture_rate;
    }

    public List<FlavorTextEntry> getFlavor_text_entries() {
        return flavor_text_entries;
    }

    public static class EvolutionChain {

        private String url;

        public String getUrl() {
            return url;
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

    public static class FlavorTextEntry {

        private String flavor_text;
        private NamedResource language;
        private NamedResource version;

        public String getFlavor_text() {
            return flavor_text;
        }

        public NamedResource getLanguage() {
            return language;
        }

        public NamedResource getVersion() {
            return version;
        }
    }
}
