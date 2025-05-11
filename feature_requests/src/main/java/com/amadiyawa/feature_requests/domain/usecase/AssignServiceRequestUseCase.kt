package com.amadiyawa.feature_requests.domain.usecase

import com.amadiyawa.feature_requests.domain.model.RequestStatus
import com.amadiyawa.feature_requests.domain.model.RequestUpdate
import com.amadiyawa.feature_requests.domain.repository.ServiceRequestRepository

class AssignServiceRequestUseCase(
    private val repository: ServiceRequestRepository
) {
    suspend operator fun invoke(
        requestId: String,
        agentId: String
    ): RequestUpdate {
        val request = repository.getRequestById(requestId)
            ?: throw IllegalArgumentException("Service request not found")

        check(request.status == RequestStatus.PENDING) {
            "Service request is not in a state that can be assigned"
        }

        return repository.updateRequestStatus(
            requestId = requestId,
            newStatus = RequestStatus.ASSIGNED,
            agentId = agentId,
            comment = "Request assigned to agent"
        )
    }
}