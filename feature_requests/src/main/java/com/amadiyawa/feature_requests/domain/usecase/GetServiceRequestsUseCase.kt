package com.amadiyawa.feature_requests.domain.usecase

import com.amadiyawa.feature_requests.domain.model.ServiceRequest
import com.amadiyawa.feature_requests.domain.repository.ServiceRequestRepository
import kotlinx.coroutines.flow.Flow

class GetServiceRequestsUseCase(
    private val repository: ServiceRequestRepository
) {
    fun getByClient(clientId: String): Flow<List<ServiceRequest>> {
        return repository.getRequestsByClient(clientId)
    }

    fun getByAgent(agentId: String): Flow<List<ServiceRequest>> {
        return repository.getRequestsByAgent(agentId)
    }

    fun getAll(): Flow<List<ServiceRequest>> {
        return repository.getAllRequests()
    }
}