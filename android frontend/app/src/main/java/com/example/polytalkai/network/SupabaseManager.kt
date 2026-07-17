package com.example.polytalkai.network

import com.example.polytalkai.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.ktor.client.request.delete
import io.ktor.client.request.header

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

object SupabaseManager {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(ComposeAuth) {
                googleNativeLogin(serverClientId = "862113197757-actjl89abejm912laivdsihcvbi4tp2a.apps.googleusercontent.com") // Configured Google Client ID
            }
            install(Postgrest)
        }
    }

    suspend fun deleteCurrentUser(): Boolean {
        val session = client.auth.currentSessionOrNull() ?: return false
        return try {
            // Call the secure Postgres function to delete the user
            client.postgrest.rpc("delete_user")
            client.auth.signOut()
            true
        } catch (e: Exception) {
            android.util.Log.e("SupabaseManager", "Delete Exception: ${e.localizedMessage}")
            false
        }
    }
}
