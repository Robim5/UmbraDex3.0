package com.umbra.umbradex.data.repository

import com.umbra.umbradex.data.supabase.UmbraSupabase
import com.umbra.umbradex.utils.Resource
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Objects

class DataRepository {

    private val db = UmbraSupabase.db
    private val auth = UmbraSupabase.auth

    // Obter estatísticas da Living Dex
    suspend fun getLivingDexStats(): Flow<Resource<Map<String, Any>>> = flow {
        emit(Resource.Loading)
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            // Total de Pokémon capturados
            val totalCaught = db.from("user_pokemons")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Map<String, String>>()
                .size

            // Total possível (Gen 1-9 = 1025)
            val totalPossible = 1025

            // Percentagem
            val percentage = if (totalPossible > 0) {
                (totalCaught.toFloat() / totalPossible * 100).toInt()
            } else 0

            val stats = mapOf(
                "total_caught" to totalCaught,
                "total_possible" to totalPossible,
                "percentage" to percentage,
                "missing" to (totalPossible - totalCaught)
            )

            emit(Resource.Success(stats))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load stats: ${e.message}"))
        }
    }

    // Obter estatísticas de missões
    suspend fun getMissionStats(): Flow<Resource<Map<String, Int>>> = flow {
        emit(Resource.Loading)
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            val progress = db.from("missions_progress")
                .select(columns = Columns.list("status")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Map<String, String>>()

            val completed = progress.count { it["status"] == "completed" }
            val active = progress.count { it["status"] == "active" }
            val locked = progress.count { it["status"] == "locked" }

            val stats = mapOf(
                "completed" to completed,
                "active" to active,
                "locked" to locked,
                "total" to progress.size
            )

            emit(Resource.Success(stats))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load mission stats: ${e.message}"))
        }
    }

    // Iniciar sessão (tracking de tempo online)
    suspend fun startSession(): Flow<Resource<String>> = flow {
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            // Inserir nova sessão
            db.from("user_sessions").insert(
                buildJsonObject {
                    put("user_id", userId)
                }
            )

            // Como não conseguimos obter o ID diretamente do insert,
            // vamos buscar a sessão mais recente do user
            val result = db.from("user_sessions")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("user_id", userId)
                        Objects.isNull("ended_at")
                    }
                    order("started_at", Order.DESCENDING)
                    limit(1)
                }
                .decodeSingle<Map<String, String>>()

            val sessionId = result["id"] ?: throw Exception("Failed to get session ID")
            emit(Resource.Success(sessionId))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to start session: ${e.message}"))
        }
    }

    // Terminar sessão
    suspend fun endSession(sessionId: String): Flow<Resource<Boolean>> = flow {
        try {
            db.rpc("end_session", buildJsonObject {
                put("p_session_id", sessionId)
            })

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to end session: ${e.message}"))
        }
    }

    // Obter tempo total online
    suspend fun getTotalTimeOnline(): Flow<Resource<Long>> = flow {
        emit(Resource.Loading)
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            val profile = db.from("profiles")
                .select(columns = Columns.list("total_time_seconds")) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<Map<String, Long>>()

            val seconds = profile["total_time_seconds"] ?: 0L
            emit(Resource.Success(seconds))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load time: ${e.message}"))
        }
    }
}