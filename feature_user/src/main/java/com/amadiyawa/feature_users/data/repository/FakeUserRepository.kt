package com.amadiyawa.feature_users.data.repository

import com.amadiyawa.feature_base.domain.model.UserSessionManager
import com.amadiyawa.feature_base.domain.result.OperationResult
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_base.domain.util.UserStatus
import com.amadiyawa.feature_users.domain.model.Admin
import com.amadiyawa.feature_users.domain.model.AdminRole
import com.amadiyawa.feature_users.domain.model.Agent
import com.amadiyawa.feature_users.domain.model.Client
import com.amadiyawa.feature_users.domain.model.User
import com.amadiyawa.feature_users.domain.repository.UserRepository
import com.amadiyawa.feature_users.domain.repository.UserStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

/**
 * Fake implementation of UserRepository for academic purposes
 * Generates and manages fake user data
 */
class FakeUserRepository(
    private val userSessionManager: UserSessionManager
) : UserRepository {

    private val fakeClients = MutableStateFlow<List<Client>>(emptyList())
    private val fakeAgents = MutableStateFlow<List<Agent>>(emptyList())
    private val fakeAdmins = MutableStateFlow<List<Admin>>(emptyList())

    // Data matching AuthResponse model
    private val cameroonNames = listOf(
        Pair("Erwin", "Smith"),
        Pair("Levi", "Ackerman"),
        Pair("Itachi", "Uchiwa"),
        Pair("Naruto", "Uzumaki"),
        Pair("Amadou", "Iyawa"),
        Pair("Obito", "Uchiha"),
        Pair("Shikamaru", "Nara"),
        Pair("Florence", "Biloa"),
        Pair("Michel", "Tchinda"),
        Pair("Bernadette", "Mbida"),
        Pair("Sasuke", "Uchiha"),
        Pair("Kakashi", "Hatake"),
        Pair("Hinata", "Hyuga"),
        Pair("Sakura", "Haruno"),
        Pair("Gaara", "Sabaku"),
        Pair("Rock", "Lee"),
        Pair("Neji", "Hyuga"),
        Pair("Tenten", "Mitsashi"),
        Pair("Jiraiya", "Sannin"),
        Pair("Tsunade", "Senju")
    )

    private val cameroonCities = listOf(
        "Yaoundé", "Douala", "Garoua", "Bamenda", "Maroua",
        "Bafoussam", "Ngaoundéré", "Bertoua", "Edéa", "Kribi"
    )

    private val cameroonAreas = listOf(
        "Bastos", "Essos", "Kpokolota", "Akwa", "Bonapriso",
        "Mokolo", "Madagascar", "Omnisport", "Yademe", "Mvan",
        "Nkoldongo", "Mokolo safari", "Biyem-Assi", "Nsimeyong", "Cité Verte"
    )

    private val avatarUrl = "https://avatars.githubusercontent.com/u/31802381?v=4"

    init {
        // Initialize fake data
        fakeClients.value = generateFakeClients()
        fakeAgents.value = generateFakeAgents()
        fakeAdmins.value = generateFakeAdmins()
    }

    // Client operations
    override suspend fun getAllClients(): OperationResult<List<Client>> {
        return when (userSessionManager.currentRole.value) {
            UserRole.AGENT -> {
                // For AGENT users, just use the first agent's territories
                // or assign some territories if needed
                val fakeAgent = fakeAgents.value.firstOrNull()

                if (fakeAgent != null) {
                    // Use the agent's territories to filter clients
                    val clientsInTerritory = fakeClients.value.filter { client ->
                        fakeAgent.territories.contains(client.area)
                    }

                    Timber.d("Agent has access to territories: ${fakeAgent.territories}")
                    Timber.d("Found ${clientsInTerritory.size} clients in agent's territories")

                    OperationResult.Success(clientsInTerritory)
                } else {
                    // Fallback if no agents exist - just return some clients
                    Timber.w("No agents found in fake data. Returning a subset of clients.")
                    OperationResult.Success(fakeClients.value.take(10))
                }
            }
            UserRole.ADMIN -> OperationResult.Success(fakeClients.value)
            else -> OperationResult.Error(Exception("Unauthorized"))
        }
    }

    override suspend fun getClientById(clientId: String): OperationResult<Client> {
        val client = fakeClients.value.find { it.id == clientId }
        return if (client != null) {
            when (userSessionManager.currentRole.value) {
                UserRole.AGENT -> {
                    val agentId = userSessionManager.currentUserId.value ?: return OperationResult.Error(Exception("No user ID"))
                    val agent = fakeAgents.value.find { it.id == agentId }
                    if (agent?.territories?.contains(client.area) == true) {
                        OperationResult.Success(client)
                    } else {
                        OperationResult.Error(Exception("Client not in agent's territory"))
                    }
                }
                UserRole.ADMIN -> OperationResult.Success(client)
                else -> OperationResult.Error(Exception("Unauthorized"))
            }
        } else {
            OperationResult.Error(Exception("Client not found"))
        }
    }

    override suspend fun searchClients(query: String): OperationResult<List<Client>> {
        val lowerQuery = query.lowercase()
        val results = fakeClients.value.filter { client ->
            client.fullName.lowercase().contains(lowerQuery) ||
                    client.email.lowercase().contains(lowerQuery) ||
                    client.phoneNumber.contains(lowerQuery) ||
                    client.accountNumber.contains(lowerQuery) ||
                    client.meterNumber.contains(lowerQuery)
        }

        return when (userSessionManager.currentRole.value) {
            UserRole.AGENT -> {
                val agentId = userSessionManager.currentUserId.value ?: return OperationResult.Error(Exception("No user ID"))
                val agent = fakeAgents.value.find { it.id == agentId }
                val filteredResults = results.filter { client ->
                    agent?.territories?.contains(client.area) == true
                }
                OperationResult.Success(filteredResults)
            }
            UserRole.ADMIN -> OperationResult.Success(results)
            else -> OperationResult.Error(Exception("Unauthorized"))
        }
    }

    override suspend fun getClientsByArea(area: String): OperationResult<List<Client>> {
        val clients = fakeClients.value.filter { it.area == area }
        return when (userSessionManager.currentRole.value) {
            UserRole.AGENT -> {
                val agentId = userSessionManager.currentUserId.value ?: return OperationResult.Error(Exception("No user ID"))
                val agent = fakeAgents.value.find { it.id == agentId }
                if (agent?.territories?.contains(area) == true) {
                    OperationResult.Success(clients)
                } else {
                    OperationResult.Error(Exception("Area not in agent's territory"))
                }
            }
            UserRole.ADMIN -> OperationResult.Success(clients)
            else -> OperationResult.Error(Exception("Unauthorized"))
        }
    }

    override suspend fun createClient(client: Client): OperationResult<Unit> {
        if (userSessionManager.currentRole.value != UserRole.ADMIN) {
            return OperationResult.Error(Exception("Only admins can create clients"))
        }

        fakeClients.update { currentList ->
            currentList + client
        }
        return OperationResult.Success(Unit)
    }

    override suspend fun updateClient(client: Client): OperationResult<Unit> {
        if (userSessionManager.currentRole.value != UserRole.ADMIN) {
            return OperationResult.Error(Exception("Only admins can update clients"))
        }

        fakeClients.update { currentList ->
            currentList.map { if (it.id == client.id) client else it }
        }
        return OperationResult.Success(Unit)
    }

    override suspend fun changeClientStatus(clientId: String, status: UserStatus): OperationResult<Unit> {
        if (userSessionManager.currentRole.value != UserRole.ADMIN) {
            return OperationResult.Error(Exception("Only admins can change client status"))
        }

        fakeClients.update { currentList ->
            currentList.map { client ->
                if (client.id == clientId) client.copy(status = status) else client
            }
        }
        return OperationResult.Success(Unit)
    }

    override fun observeClientUpdates(): Flow<List<Client>> = fakeClients.asStateFlow()

    // Agent operations
    override suspend fun getAllAgents(): OperationResult<List<Agent>> {
        return if (userSessionManager.currentRole.value == UserRole.ADMIN) {
            OperationResult.Success(fakeAgents.value)
        } else {
            OperationResult.Error(Exception("Only admins can view agents"))
        }
    }

    override suspend fun getAgentById(agentId: String): OperationResult<Agent> {
        return if (userSessionManager.currentRole.value == UserRole.ADMIN) {
            val agent = fakeAgents.value.find { it.id == agentId }
            if (agent != null) {
                OperationResult.Success(agent)
            } else {
                OperationResult.Error(Exception("Agent not found"))
            }
        } else {
            OperationResult.Error(Exception("Only admins can view agent details"))
        }
    }

    override suspend fun createAgent(agent: Agent): OperationResult<Unit> {
        if (userSessionManager.currentRole.value != UserRole.ADMIN) {
            return OperationResult.Error(Exception("Only admins can create agents"))
        }

        fakeAgents.update { currentList ->
            currentList + agent
        }
        return OperationResult.Success(Unit)
    }

    override suspend fun updateAgent(agent: Agent): OperationResult<Unit> {
        if (userSessionManager.currentRole.value != UserRole.ADMIN) {
            return OperationResult.Error(Exception("Only admins can update agents"))
        }

        fakeAgents.update { currentList ->
            currentList.map { if (it.id == agent.id) agent else it }
        }
        return OperationResult.Success(Unit)
    }

    override suspend fun changeAgentStatus(agentId: String, status: UserStatus): OperationResult<Unit> {
        if (userSessionManager.currentRole.value != UserRole.ADMIN) {
            return OperationResult.Error(Exception("Only admins can change agent status"))
        }

        fakeAgents.update { currentList ->
            currentList.map { agent ->
                if (agent.id == agentId) agent.copy(status = status) else agent
            }
        }
        return OperationResult.Success(Unit)
    }

    override suspend fun getAgentsByTerritory(territory: String): OperationResult<List<Agent>> {
        return if (userSessionManager.currentRole.value == UserRole.ADMIN) {
            val agents = fakeAgents.value.filter { it.territories.contains(territory) }
            OperationResult.Success(agents)
        } else {
            OperationResult.Error(Exception("Only admins can view agents by territory"))
        }
    }

    override fun observeAgentUpdates(): Flow<List<Agent>> = fakeAgents.asStateFlow()

    // Admin operations
    override suspend fun getAllAdmins(): OperationResult<List<Admin>> {
        return if (userSessionManager.currentRole.value == UserRole.ADMIN) {
            val currentAdmin = fakeAdmins.value.find { it.id == userSessionManager.currentUserId.value }
            if (currentAdmin?.accessLevel == AdminRole.SUPER_ADMIN) {
                OperationResult.Success(fakeAdmins.value)
            } else {
                OperationResult.Error(Exception("Only super admins can view all admins"))
            }
        } else {
            OperationResult.Error(Exception("Unauthorized"))
        }
    }

    override suspend fun getAdminById(adminId: String): OperationResult<Admin> {
        return if (userSessionManager.currentRole.value == UserRole.ADMIN) {
            val currentAdmin = fakeAdmins.value.find { it.id == userSessionManager.currentUserId.value }
            if (currentAdmin?.accessLevel == AdminRole.SUPER_ADMIN) {
                val admin = fakeAdmins.value.find { it.id == adminId }
                if (admin != null) {
                    OperationResult.Success(admin)
                } else {
                    OperationResult.Error(Exception("Admin not found"))
                }
            } else {
                OperationResult.Error(Exception("Only super admins can view admin details"))
            }
        } else {
            OperationResult.Error(Exception("Unauthorized"))
        }
    }

    override suspend fun createAdmin(admin: Admin): OperationResult<Unit> {
        return if (userSessionManager.currentRole.value == UserRole.ADMIN) {
            val currentAdmin = fakeAdmins.value.find { it.id == userSessionManager.currentUserId.value }
            if (currentAdmin?.accessLevel == AdminRole.SUPER_ADMIN) {
                fakeAdmins.update { currentList ->
                    currentList + admin
                }
                OperationResult.Success(Unit)
            } else {
                OperationResult.Error(Exception("Only super admins can create admins"))
            }
        } else {
            OperationResult.Error(Exception("Unauthorized"))
        }
    }

    override suspend fun updateAdmin(admin: Admin): OperationResult<Unit> {
        return if (userSessionManager.currentRole.value == UserRole.ADMIN) {
            val currentAdmin = fakeAdmins.value.find { it.id == userSessionManager.currentUserId.value }
            if (currentAdmin?.accessLevel == AdminRole.SUPER_ADMIN) {
                fakeAdmins.update { currentList ->
                    currentList.map { if (it.id == admin.id) admin else it }
                }
                OperationResult.Success(Unit)
            } else {
                OperationResult.Error(Exception("Only super admins can update admins"))
            }
        } else {
            OperationResult.Error(Exception("Unauthorized"))
        }
    }

    override suspend fun changeAdminStatus(adminId: String, status: UserStatus): OperationResult<Unit> {
        return if (userSessionManager.currentRole.value == UserRole.ADMIN) {
            val currentAdmin = fakeAdmins.value.find { it.id == userSessionManager.currentUserId.value }
            if (currentAdmin?.accessLevel == AdminRole.SUPER_ADMIN) {
                fakeAdmins.update { currentList ->
                    currentList.map { admin ->
                        if (admin.id == adminId) admin.copy(status = status) else admin
                    }
                }
                OperationResult.Success(Unit)
            } else {
                OperationResult.Error(Exception("Only super admins can change admin status"))
            }
        } else {
            OperationResult.Error(Exception("Unauthorized"))
        }
    }

    override fun observeAdminUpdates(): Flow<List<Admin>> = fakeAdmins.asStateFlow()

    // General operations
    override suspend fun getUserByEmail(email: String): OperationResult<User> {
        val client = fakeClients.value.find { it.email == email }
        if (client != null) return OperationResult.Success(client)

        val agent = fakeAgents.value.find { it.email == email }
        if (agent != null) return OperationResult.Success(agent)

        val admin = fakeAdmins.value.find { it.email == email }
        if (admin != null) return OperationResult.Success(admin)

        return OperationResult.Error(Exception("User not found"))
    }

    override suspend fun isEmailTaken(email: String): OperationResult<Boolean> {
        val isTaken = fakeClients.value.any { it.email == email } ||
                fakeAgents.value.any { it.email == email } ||
                fakeAdmins.value.any { it.email == email }
        return OperationResult.Success(isTaken)
    }

    override suspend fun isPhoneTaken(phoneNumber: String): OperationResult<Boolean> {
        val isTaken = fakeClients.value.any { it.phoneNumber == phoneNumber } ||
                fakeAgents.value.any { it.phoneNumber == phoneNumber } ||
                fakeAdmins.value.any { it.phoneNumber == phoneNumber }
        return OperationResult.Success(isTaken)
    }

    // Pagination
    override suspend fun getClientsPaged(page: Int, pageSize: Int): OperationResult<List<Client>> {
        val allClients = when (userSessionManager.currentRole.value) {
            UserRole.AGENT -> {
                val agentId = userSessionManager.currentUserId.value ?: return OperationResult.Error(Exception("No user ID"))
                val agent = fakeAgents.value.find { it.id == agentId }
                fakeClients.value.filter { client ->
                    agent?.territories?.contains(client.area) == true
                }
            }
            UserRole.ADMIN -> fakeClients.value
            else -> return OperationResult.Error(Exception("Unauthorized"))
        }

        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, allClients.size)

        return if (startIndex < allClients.size) {
            OperationResult.Success(allClients.subList(startIndex, endIndex))
        } else {
            OperationResult.Success(emptyList())
        }
    }

    override suspend fun getAgentsPaged(page: Int, pageSize: Int): OperationResult<List<Agent>> {
        return if (userSessionManager.currentRole.value == UserRole.ADMIN) {
            val allAgents = fakeAgents.value
            val startIndex = page * pageSize
            val endIndex = minOf(startIndex + pageSize, allAgents.size)

            if (startIndex < allAgents.size) {
                OperationResult.Success(allAgents.subList(startIndex, endIndex))
            } else {
                OperationResult.Success(emptyList())
            }
        } else {
            OperationResult.Error(Exception("Only admins can view agents"))
        }
    }

    // Statistics
    override suspend fun getUserStatistics(): OperationResult<UserStatistics> {
        if (userSessionManager.currentRole.value != UserRole.ADMIN) {
            return OperationResult.Error(Exception("Only admins can view statistics"))
        }

        val thirtyDaysAgo = LocalDateTime.now().minusDays(30)

        val statistics = UserStatistics(
            totalClients = fakeClients.value.size,
            activeClients = fakeClients.value.count { it.status == UserStatus.ACTIVE },
            totalAgents = fakeAgents.value.size,
            activeAgents = fakeAgents.value.count { it.status == UserStatus.ACTIVE },
            totalAdmins = fakeAdmins.value.size,
            clientsByArea = fakeClients.value.groupBy { it.area }.mapValues { it.value.size },
            recentRegistrations = fakeClients.value.count { it.createdAt.isAfter(thirtyDaysAgo) }
        )

        return OperationResult.Success(statistics)
    }

    // Data generators
    private fun generateFakeClients(): List<Client> {
        return (1..50).map { i ->
            val name = cameroonNames.random()
            val randomArea = cameroonAreas.random()
            val randomCity = cameroonCities.random()
            Client(
                id = UUID.randomUUID().toString(),
                fullName = "${name.first} ${name.second}",
                email = "${name.first.lowercase()}.${name.second.lowercase()}@gmail.com",
                phoneNumber = "+237${(600000000..699999999).random()}",
                createdAt = LocalDateTime.now().minusDays(Random.nextLong(30, 365)),
                status = listOf(UserStatus.ACTIVE, UserStatus.PENDING_VERIFICATION).random(),
                avatarUrl = avatarUrl,
                address = "${Random.nextInt(1, 200)} $randomArea, $randomCity",
                meterNumber = "MT${(100000..999999).random()}",
                accountNumber = "AC${(1000000..9999999).random()}",
                area = randomArea
            )
        }
    }

    private fun generateFakeAgents(): List<Agent> {
        return (1..10).map { i ->
            val name = cameroonNames.random()
            val territoryCount = Random.nextInt(2, 5)
            Agent(
                id = UUID.randomUUID().toString(),
                fullName = "${name.first} ${name.second}",
                email = "${name.first.lowercase()}.${name.second.lowercase()}@eneo.cm",
                phoneNumber = "+237${(670000000..699999999).random()}",
                createdAt = LocalDateTime.now().minusDays(Random.nextLong(180, 730)),
                status = UserStatus.ACTIVE,
                avatarUrl = avatarUrl,
                employeeId = "EMP${(1000..9999).random()}",
                territories = cameroonAreas.shuffled().take(territoryCount)
            )
        }
    }

    private fun generateFakeAdmins(): List<Admin> {
        return (1..5).map { i ->
            val name = cameroonNames.random()
            Admin(
                id = UUID.randomUUID().toString(),
                fullName = "${name.first} ${name.second}",
                email = "${name.first.lowercase()}.${name.second.lowercase()}@admin.eneo.cm",
                phoneNumber = "+237${(690000000..699999999).random()}",
                createdAt = LocalDateTime.now().minusDays(Random.nextLong(365, 1095)),
                status = UserStatus.ACTIVE,
                avatarUrl = avatarUrl,
                accessLevel = when (i) {
                    1 -> AdminRole.SUPER_ADMIN
                    2, 3 -> AdminRole.BILLING_ADMIN
                    else -> AdminRole.SERVICE_ADMIN
                }
            )
        }
    }
}