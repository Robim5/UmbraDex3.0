package com.umbra.umbradex.util

object Constants {
    // Supabase
    const val SUPABASE_URL = "https://fczxiqvpfdiualhshfum.supabase.co" // Get from Supabase Dashboard
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZjenhpcXZwZmRpdWFsaHNoZnVtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQwODExODQsImV4cCI6MjA3OTY1NzE4NH0.m_ncuciOqbDu8-7qKVABaMc_gphgilcZceEP8GTQu48" // Get from Supabase Dashboard

    // PokéAPI
    const val POKEAPI_BASE_URL = "https://pokeapi.co/api/v2/"
    const val POKEAPI_SPRITE_BASE = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"
    const val POKEAPI_OFFICIAL_ARTWORK = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"

    // App Constants
    const val MAX_TEAM_SIZE = 6
    const val TOTAL_POKEMON_COUNT = 1025 // Gen 1-9
    const val STARTING_GOLD = 100
    const val STARTING_LEVEL = 1

    // Pokemon Types
    val POKEMON_TYPES = listOf(
        "normal", "fire", "water", "electric", "grass", "ice",
        "fighting", "poison", "ground", "flying", "psychic", "bug",
        "rock", "ghost", "dragon", "dark", "steel", "fairy"
    )

    // Generations
    val GENERATIONS = mapOf(
        1 to 1..151,
        2 to 152..251,
        3 to 252..386,
        4 to 387..493,
        5 to 494..649,
        6 to 650..721,
        7 to 722..809,
        8 to 810..905,
        9 to 906..1025
    )

    // Rarity Colors
    val RARITY_COLORS = mapOf(
        "common" to 0xFF9E9E9E,
        "rare" to 0xFF2196F3,
        "epic" to 0xFF9C27B0,
        "legendary" to 0xFFFFD700
    )

    // Audio files
    const val AUDIO_BACKGROUND = "background.wav"
    const val AUDIO_OPEN_DASHBOARD = "opendashboard.wav"
    const val AUDIO_COMPLETE_QUEST = "completequest.wav"
    const val AUDIO_EQUIP_BADGE = "equipbadget.wav"
    const val AUDIO_GET_SOMETHING = "getsomething.wav"
    const val AUDIO_GOOD_ANIMAL = "goodanimal.wav"

    // Avatar paths
    const val AVATAR_MALE_START_PATH = "characters/male_start/"
    const val AVATAR_FEMALE_START_PATH = "characters/female_start/"
    const val AVATAR_SHOP_PATH = "characters/shop/"

    // Badge paths
    const val BADGE_START_PATH = "badgets/start/"
    const val BADGE_COMMON_PATH = "badgets/common/"
    const val BADGE_RARE_PATH = "badgets/rare/"
    const val BADGE_EPIC_PATH = "badgets/epic/"
    const val BADGE_LEGENDARY_PATH = "badgets/legendary/"
}