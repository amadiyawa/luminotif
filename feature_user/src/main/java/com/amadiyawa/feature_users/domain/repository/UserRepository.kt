package com.amadiyawa.feature_users.domain.repository

import com.amadiyawa.feature_base.domain.result.OperationResult
import com.amadiyawa.feature_base.domain.util.UserStatus
import com.amadiyawa.feature_users.domain.model.Admin
import com.amadiyawa.feature_users.domain.model.Agent
import com.amadiyawa.feature_users.domain.model.Client
import com.amadiyawa.feature_users.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for user management operations
 */
interface UserRepository {

    // Client operations
    suspend fun getAllClients(): OperationResult<List<Client>>
    suspend fun getClientById(clientId: String): OperationResult<Client>
    suspend fun searchClients(query: String): OperationResult<List<Client>>
    suspend fun getClientsByArea(area: String): OperationResult<List<Client>>
    suspend fun createClient(client: Client): OperationResult<Unit>
    suspend fun updateClient(client: Client): OperationResult<Unit>
    suspend fun changeClientStatus(clientId: String, status: UserStatus): OperationResult<Unit>
    fun observeClientUpdates(): Flow<List<Client>>

    // Agent operations (Admin only)
    suspend fun getAllAgents(): OperationResult<List<Agent>>
    suspend fun getAgentById(agentId: String): OperationResult<Agent>
    suspend fun createAgent(agent: Agent): OperationResult<Unit>
    suspend fun updateAgent(agent: Agent): OperationResult<Unit>
    suspend fun changeAgentStatus(agentId: String, status: UserStatus): OperationResult<Unit>
    suspend fun getAgentsByTerritory(territory: String): OperationResult<List<Agent>>
    fun observeAgentUpdates(): Flow<List<Agent>>

    // Admin operations (Super Admin only)
    suspend fun getAllAdmins(): OperationResult<List<Admin>>
    suspend fun getAdminById(adminId: String): OperationResult<Admin>
    suspend fun createAdmin(admin: Admin): OperationResult<Unit>
    suspend fun updateAdmin(admin: Admin): OperationResult<Unit>
    suspend fun changeAdminStatus(adminId: String, status: UserStatus): OperationResult<Unit>
    fun observeAdminUpdates(): Flow<List<Admin>>

    // General user operations
    suspend fun getUserByEmail(email: String): OperationResult<User>
    suspend fun isEmailTaken(email: String): OperationResult<Boolean>
    suspend fun isPhoneTaken(phoneNumber: String): OperationResult<Boolean>

    // Pagination support
    suspend fun getClientsPaged(page: Int, pageSize: Int = 20): OperationResult<List<Client>>
    suspend fun getAgentsPaged(page: Int, pageSize: Int = 20): OperationResult<List<Agent>>

    // Statistics (for admin dashboard)
    suspend fun getUserStatistics(): OperationResult<UserStatistics>
}

/**
 * Data class for user statistics
 */
data class UserStatistics(
    val totalClients: Int,
    val activeClients: Int,
    val totalAgents: Int,
    val activeAgents: Int,
    val totalAdmins: Int,
    val clientsByArea: Map<String, Int>,
    val recentRegistrations: Int // Last 30 days
)