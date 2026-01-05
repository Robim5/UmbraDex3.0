package com.umbra.umbradex.data.repository

import com.umbra.umbradex.data.model.Mission
import com.umbra.umbradex.data.model.MissionProgress
import com.umbra.umbradex.data.supabase.UmbraSupabase
import com.umbra.umbradex.utils.Resource
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class MissionRepository {

    // Buscar todas as missões disponíveis
    suspend fun getAllMissions(): Flow<Resource<List<Mission>>> = flow {
        emit(Resource.Loading)
        try {
            val missions = UmbraSupabase.client.from("missions")
                .select()
                .decodeList<Mission>()
                .sortedBy { it.sortOrder }

            emit(Resource.Success(missions))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load missions: ${e.message}", e))
        }
    }

    // Buscar progresso do usuário em todas as missões
    suspend fun getUserMissionProgress(userId: String): Flow<Resource<List<MissionProgress>>> = flow {
        emit(Resource.Loading)
        try {
            val progress = UmbraSupabase.client.from("missions_progress")
                .select() {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<MissionProgress>()

            emit(Resource.Success(progress))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load progress: ${e.message}", e))
        }
    }

    // Reclamar recompensa de uma missão
    suspend fun claimMissionReward(userId: String, missionId: Int): Resource<String> {
        return try {
            UmbraSupabase.client.postgrest.rpc(
                function = "claim_mission_reward",
                parameters = mapOf(
                    "p_user_id" to userId,
                    "p_mission_id" to missionId
                )
            )

            Resource.Success("Reward claimed successfully!")
        } catch (e: Exception) {
            Resource.Error("Failed to claim reward: ${e.message}", e)
        }
    }

    // Atualizar progresso de uma missão manualmente (se necessário)
    suspend fun updateMissionProgress(
        userId: String,
        missionId: Int,
        currentValue: Int
    ): Resource<String> {
        return try {
            UmbraSupabase.client.from("missions_progress")
                .upsert(
                    mapOf(
                        "user_id" to userId,
                        "mission_id" to missionId,
                        "current_value" to currentValue,
                        "status" to "active"
                    )
                )

            Resource.Success("Progress updated!")
        } catch (e: Exception) {
            Resource.Error("Failed to update progress: ${e.message}", e)
        }
    }

    // Inicializar missões para um novo usuário
    suspend fun initializeMissionsForUser(userId: String): Resource<String> {
        return try {
            // Buscar todas as missões
            val allMissions = UmbraSupabase.client.from("missions")
                .select()
                .decodeList<Mission>()

            // Filtrar apenas as que não têm pré-requisito (primeiras da cadeia)
            val initialMissions = allMissions.filter { it.prerequisiteMissionId == null }

            // Criar progresso inicial para cada missão
            initialMissions.forEach { mission ->
                UmbraSupabase.client.from("missions_progress")
                    .insert(
                        mapOf(
                            "user_id" to userId,
                            "mission_id" to mission.id,
                            "current_value" to 0,
                            "status" to "active"
                        )
                    )
            }

            Resource.Success("Missions initialized!")
        } catch (e: Exception) {
            Resource.Error("Failed to initialize missions: ${e.message}", e)
        }
    }
}