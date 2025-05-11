package com.amadiyawa.feature_requests.domain.usecase

import com.amadiyawa.feature_requests.domain.model.RequestStatus
import com.amadiyawa.feature_requests.domain.model.RequestUpdate
import com.amadiyawa.feature_requests.domain.repository.ServiceRequestRepository

class ResolveServiceRequestUseCase(
    private val repository: ServiceRequestRepository
) {
    suspend operator fun invoke(
        requestId: String,
        agentId: String,
        resolution: String
    ): RequestUpdate {
        val request = repository.getRequestById(requestId)
            ?: throw IllegalArgumentException("Service request not found")

        check(request.status != RequestStatus.IN_PROGRESS) {
            "Can only resolve requests that are in progress"
        }

        return repository.updateRequestStatus(
            requestId = requestId,
            newStatus = RequestStatus.RESOLVED,
            agentId = agentId,
            comment = "Resolved: $resolution"
        )
    }
}