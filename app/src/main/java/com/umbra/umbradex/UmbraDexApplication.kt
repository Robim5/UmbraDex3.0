package com.umbra.umbradex

import android.app.Application
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

class UmbraDexApplication : Application() {

    companion object {
        lateinit var supabase: SupabaseClient
            private set
    }

    override fun onCreate() {
        super.onCreate()

        supabase = createSupabaseClient(
            supabaseUrl = "https://fczxiqvpfdiualhshfum.supabase.co", // Add your URL here
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZjenhpcXZwZmRpdWFsaHNoZnVtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQwODExODQsImV4cCI6MjA3OTY1NzE4NH0.m_ncuciOqbDu8-7qKVABaMc_gphgilcZceEP8GTQu48" // Add your key here
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}