package com.umbra.umbradex.data.remote.pokeapi

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface PokeApiService {

    @GET("pokemon/{id}")
    suspend fun getPokemon(@Path("id") id: Int): Response<PokeApiPokemonResponse>

    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") id: Int): Response<PokeApiSpeciesResponse>

    @GET("evolution-chain/{id}")
    suspend fun getEvolutionChain(@Path("id") id: Int): Response<PokeApiEvolutionChainResponse>
}