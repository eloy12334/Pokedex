package com.example.pokedex.api;

import com.example.pokedex.model.PokemonListResponse;
import com.example.pokedex.model.PokemonDetail;
import com.example.pokedex.model.PokemonTypeResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PokeApi {

    @GET("pokemon")
    Call<PokemonListResponse> getPokemonList(
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("pokemon/{nameOrId}")
    Call<PokemonDetail> getPokemonDetail(
            @Path("nameOrId") String nameOrId
    );

    @GET("type/{type}")
    Call<PokemonTypeResponse> getPokemonByType(
            @Path("type") String type
    );
}
