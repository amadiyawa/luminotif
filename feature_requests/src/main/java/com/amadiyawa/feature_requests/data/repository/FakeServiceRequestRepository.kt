package com.amadiyawa.feature_requests.data.repository

import com.amadiyawa.feature_requests.domain.model.RequestCategory
import com.amadiyawa.feature_requests.domain.model.RequestPriority
import com.amadiyawa.feature_requests.domain.model.RequestStatus
import com.amadiyawa.feature_requests.domain.model.RequestUpdate
import com.amadiyawa.feature_requests.domain.model.ServiceRequest
import com.amadiyawa.feature_requests.domain.repository.ServiceRequestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

class FakeServiceRequestRepository : ServiceRequestRepository {

    private val serviceRequests = MutableStateFlow<List<ServiceRequest>>(emptyList())
    private val requestUpdates = MutableStateFlow<List<RequestUpdate>>(emptyList())

    // Store the current user ID when requests are made
    private var currentUserId: String? = null
    private var currentAgentId: String? = null

    init {
        generateFakeData()
    }

    override suspend fun createRequest(request: ServiceRequest): ServiceRequest {
        // Store the client ID for future reference
        currentUserId = request.clientId

        val newRequest = request.copy(
            id = UUID.randomUUID().toString(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        serviceRequests.update { it + newRequest }
        return newRequest
    }

    override suspend fun getRequestById(id: String): ServiceRequest? {
        return serviceRequests.value.find { it.id == id }
    }

    override suspend fun updateRequest(request: ServiceRequest): ServiceRequest {
        val updatedRequest = request.copy(updatedAt = LocalDateTime.now())
        serviceRequests.update { requests ->
            requests.map { if (it.id == request.id) updatedRequest else it }
        }
        return updatedRequest
    }

    override suspend fun deleteRequest(id: String) {
        serviceRequests.update { requests ->
            requests.filterNot { it.id == id }
        }
    }

    override fun getRequestsByClient(clientId: String): Flow<List<ServiceRequest>> {
        // Store the client ID for data generation
        currentUserId = clientId

        return serviceRequests.map { requests ->
            // If no requests exist for this client, generate some
            val clientRequests = requests.filter { it.clientId == clientId }

            if (clientRequests.isEmpty() && requests.isNotEmpty()) {
                // Update some existing requests to use this client ID
                val updatedRequests = requests.take(13).map { request ->
                    request.copy(clientId = clientId)
                }
                serviceRequests.value = requests.map { request ->
                    updatedRequests.find { it.id == request.id } ?: request
                }
                updatedRequests.sortedByDescending { it.createdAt }
            } else {
                clientRequests.sortedByDescending { it.createdAt }
            }
        }
    }

    override fun getRequestsByAgent(agentId: String): Flow<List<ServiceRequest>> {
        // Store the agent ID for data generation
        currentAgentId = agentId

        return serviceRequests.map { requests ->
            // If no requests exist for this agent, generate some
            val agentRequests = requests.filter { it.assignedAgentId == agentId }

            if (agentRequests.isEmpty() && requests.isNotEmpty()) {
                // Update some assigned requests to use this agent ID
                val assignedRequests = requests.filter { it.status != RequestStatus.PENDING }
                val updatedRequests = assignedRequests.take(26).map { request ->
                    request.copy(assignedAgentId = agentId)
                }
                serviceRequests.value = requests.map { request ->
                    updatedRequests.find { it.id == request.id } ?: request
                }
                updatedRequests.sortedByDescending { it.createdAt }
            } else {
                agentRequests.sortedByDescending { it.createdAt }
            }
        }
    }

    override fun getRequestsByStatus(status: RequestStatus): Flow<List<ServiceRequest>> {
        return serviceRequests.map { requests ->
            requests.filter { it.status == status }
                .sortedByDescending { it.createdAt }
        }
    }

    override suspend fun updateRequestStatus(
        requestId: String,
        newStatus: RequestStatus,
        agentId: String,
        comment: String
    ): RequestUpdate {
        // Store the agent ID
        currentAgentId = agentId

        val request = getRequestById(requestId)
            ?: throw IllegalArgumentException("Request not found")

        val updatedRequest = request.copy(
            status = newStatus,
            assignedAgentId = if (newStatus == RequestStatus.ASSIGNED) agentId else request.assignedAgentId,
            updatedAt = LocalDateTime.now()
        )

        updateRequest(updatedRequest)

        val update = RequestUpdate(
            id = UUID.randomUUID().toString(),
            requestId = requestId,
            agentId = agentId,
            comment = comment,
            timestamp = LocalDateTime.now(),
            newStatus = newStatus
        )

        requestUpdates.update { it + update }
        return update
    }

    override fun getRequestUpdates(requestId: String): Flow<List<RequestUpdate>> {
        return requestUpdates.map { updates ->
            updates.filter { it.requestId == requestId }
                .sortedByDescending { it.timestamp }
        }
    }

    override fun getAllRequests(): Flow<List<ServiceRequest>> {
        return serviceRequests.map { requests ->
            // If we have a current user ID and no requests for them, update some requests
            if (currentUserId != null && requests.none { it.clientId == currentUserId }) {
                val updatedRequests = requests.take(13).map { it.copy(clientId = currentUserId!!) }
                val allRequests = requests.map { request ->
                    updatedRequests.find { it.id == request.id } ?: request
                }
                serviceRequests.value = allRequests
                allRequests.sortedByDescending { it.createdAt }
            } else {
                requests.sortedByDescending { it.createdAt }
            }
        }
    }

    private fun generateFakeData() {
        val clientIds = (1..20).map { "client_$it" }
        val agentIds = (1..5).map { "agent_$it" }

        val fakeRequests = (1..50).map { index ->
            val createdAt = LocalDateTime.now().minusDays(Random.nextLong(0, 30))
            val status = RequestStatus.entries.toTypedArray().random()

            ServiceRequest(
                id = "request_$index",
                clientId = clientIds.random(),
                title = generateTitle(),
                description = generateDescription(),
                category = RequestCategory.entries.toTypedArray().random(),
                priority = RequestPriority.entries.toTypedArray().random(),
                status = status,
                createdAt = createdAt,
                updatedAt = createdAt.plusHours(Random.nextLong(1, 48)),
                assignedAgentId = if (status != RequestStatus.PENDING) agentIds.random() else null
            )
        }

        serviceRequests.value = fakeRequests

        // Generate fake updates for assigned/in-progress/resolved requests
        val fakeUpdates = fakeRequests
            .filter { it.status != RequestStatus.PENDING }
            .flatMap { request ->
                generateUpdatesForRequest(request, agentIds)
            }

        requestUpdates.value = fakeUpdates
    }

    private fun generateTitle(): String {
        val titles = listOf(
            "Power outage in my area",
            "High electricity bill inquiry",
            "Meter reading issue",
            "Request for new connection",
            "Voltage fluctuation problem",
            "Payment not reflected",
            "Meter replacement needed",
            "Frequent power cuts",
            "Bill calculation error",
            "Request for meter relocation"
        )
        return titles.random()
    }

    private fun generateDescription(): String {
        val descriptions = listOf(
            "I have been experiencing frequent power outages in my area for the past week.",
            "My electricity bill seems unusually high this month. Please check.",
            "The meter is not showing correct readings. It seems to be malfunctioning.",
            "I need a new electricity connection for my new house.",
            "There are voltage fluctuations that are damaging my appliances.",
            "I made a payment last week but it's not reflected in my account.",
            "My meter is very old and needs to be replaced with a new one.",
            "We are experiencing power cuts every day for 2-3 hours.",
            "There seems to be an error in my bill calculation. Please verify.",
            "I need to relocate my meter to a different position in my house."
        )
        return descriptions.random()
    }

    private fun generateUpdatesForRequest(
        request: ServiceRequest,
        agentIds: List<String>
    ): List<RequestUpdate> {
        val updates = mutableListOf<RequestUpdate>()
        var currentTime = request.createdAt.plusMinutes(30)

        // Generate updates based on status
        when (request.status) {
            RequestStatus.ASSIGNED -> {
                updates.add(createUpdate(request.id, agentIds.random(), currentTime,
                    RequestStatus.ASSIGNED, "Request assigned to agent"))
            }
            RequestStatus.IN_PROGRESS -> {
                updates.add(createUpdate(request.id, request.assignedAgentId!!, currentTime,
                    RequestStatus.ASSIGNED, "Request assigned to agent"))
                currentTime = currentTime.plusHours(1)
                updates.add(createUpdate(request.id, request.assignedAgentId, currentTime,
                    RequestStatus.IN_PROGRESS, "Started working on the issue"))
            }
            RequestStatus.RESOLVED -> {
                updates.add(createUpdate(request.id, request.assignedAgentId!!, currentTime,
                    RequestStatus.ASSIGNED, "Request assigned to agent"))
                currentTime = currentTime.plusHours(1)
                updates.add(createUpdate(request.id, request.assignedAgentId, currentTime,
                    RequestStatus.IN_PROGRESS, "Started working on the issue"))
                currentTime = currentTime.plusHours(2)
                updates.add(createUpdate(request.id, request.assignedAgentId, currentTime,
                    RequestStatus.RESOLVED, "Issue has been resolved"))
            }
            else -> {}
        }

        return updates
    }

    private fun createUpdate(
        requestId: String,
        agentId: String,
        timestamp: LocalDateTime,
        status: RequestStatus,
        comment: String
    ): RequestUpdate {
        return RequestUpdate(
            id = UUID.randomUUID().toString(),
            requestId = requestId,
            agentId = agentId,
            comment = comment,
            timestamp = timestamp,
            newStatus = status
        )
    }
}