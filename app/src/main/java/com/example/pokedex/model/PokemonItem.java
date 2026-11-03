package com.example.pokedex.model;

public class PokemonItem {

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

        String cleanUrl = url.endsWith("/")
                ? url.substring(0, url.length() - 1)
                : url;

        String[] parts = cleanUrl.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}

