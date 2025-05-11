package com.amadiyawa.feature_requests.domain.repository

import com.amadiyawa.feature_requests.domain.model.RequestStatus
import com.amadiyawa.feature_requests.domain.model.RequestUpdate
import com.amadiyawa.feature_requests.domain.model.ServiceRequest
import kotlinx.coroutines.flow.Flow

interface ServiceRequestRepository {
    suspend fun createRequest(request: ServiceRequest): ServiceRequest
    suspend fun getRequestById(id: String): ServiceRequest?
    suspend fun updateRequest(request: ServiceRequest): ServiceRequest
    suspend fun deleteRequest(id: String)

    fun getRequestsByClient(clientId: String): Flow<List<ServiceRequest>>
    fun getRequestsByAgent(agentId: String): Flow<List<ServiceRequest>>
    fun getRequestsByStatus(status: RequestStatus): Flow<List<ServiceRequest>>

    suspend fun updateRequestStatus(
        requestId: String,
        newStatus: RequestStatus,
        agentId: String,
        comment: String
    ): RequestUpdate

    fun getRequestUpdates(requestId: String): Flow<List<RequestUpdate>>

    fun getAllRequests(): Flow<List<ServiceRequest>>
}