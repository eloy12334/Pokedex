package com.example.pokedex.api;

import com.example.pokedex.model.PokemonListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Path;
import com.example.pokedex.model.PokemonDetail;

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
}
