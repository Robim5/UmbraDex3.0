package com.umbra.umbradex.data.supabase

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

object UmbraSupabase {

    private const val SUPABASE_URL = "https://fgwcqwrohktipjtccclc.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZnd2Nxd3JvaGt0aXBqdGNjY2xjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjYzMjkyMTEsImV4cCI6MjA4MTkwNTIxMX0.ByVz0SU3LRmmlbuT9XQTgCWZjz0HlRtdADUVrYWayPs"

    private val _client by lazy {
        try {
            createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_KEY
            ) {
                install(Auth)
                install(Postgrest)
                install(Storage)
            }
        } catch (e: Exception) {
            Log.e("UmbraSupabase", "Error initializing Supabase client", e)
            throw e
        }
    }

    // Accessors
    val client get() = _client
    val auth get() = _client.auth
    val db get() = _client.postgrest
    val storage get() = _client.storage
}