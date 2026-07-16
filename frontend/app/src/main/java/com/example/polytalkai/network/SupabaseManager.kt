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
        }
    }

    suspend fun deleteCurrentUser(): Boolean {
        val session = client.auth.currentSessionOrNull() ?: return false
        val token = session.accessToken
        val httpClient = io.ktor.client.HttpClient(io.ktor.client.engine.android.Android)
        return try {
            val response: io.ktor.client.statement.HttpResponse = httpClient.delete("${BuildConfig.SUPABASE_URL}/auth/v1/user") {
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
            }
            client.auth.signOut()
            response.status.value in 200..299
        } catch (e: Exception) {
            false
        } finally {
            httpClient.close()
        }
    }
}
