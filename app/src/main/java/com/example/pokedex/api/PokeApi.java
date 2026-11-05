
package com.example.pokedex.api;

import com.example.pokedex.model.PokemonDetail;
import com.example.pokedex.model.PokemonListResponse;
import com.example.pokedex.model.PokemonTypeResponse;
import com.example.pokedex.model.PokemonSpecies;
import com.example.pokedex.model.EvolutionChainResponse;
import com.example.pokedex.model.MoveDetail;

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

    // DATOS DE LA ESPECIE

    @GET("pokemon-species/{id}")
    Call<PokemonSpecies> getPokemonSpecies(
            @Path("id") int id
    );

    // CADENA EVOLUTIVA

    @GET("evolution-chain/{id}")
    Call<EvolutionChainResponse> getEvolutionChain(
            @Path("id") int id
    );

    // DETALLES DE LOS MOVIMIENTOS

    @GET("move/{nameOrId}")
    Call<MoveDetail> getMoveDetail(
            @Path("nameOrId") String nameOrId
    );
}
