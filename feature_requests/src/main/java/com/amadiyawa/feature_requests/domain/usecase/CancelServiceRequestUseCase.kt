package com.amadiyawa.feature_requests.domain.usecase

import com.amadiyawa.feature_requests.domain.model.RequestStatus
import com.amadiyawa.feature_requests.domain.model.RequestUpdate
import com.amadiyawa.feature_requests.domain.repository.ServiceRequestRepository

class CancelServiceRequestUseCase(
    private val repository: ServiceRequestRepository
) {
    suspend operator fun invoke(
        requestId: String,
        agentId: String,
        reason: String
    ): RequestUpdate {
        val request = repository.getRequestById(requestId)
            ?: throw IllegalArgumentException("Service request not found")

        check (request.status in listOf(RequestStatus.CLOSED, RequestStatus.CANCELLED)) {
            "Cannot cancel ${request.status} request"
        }

        return repository.updateRequestStatus(
            requestId = requestId,
            newStatus = RequestStatus.CANCELLED,
            agentId = agentId,
            comment = "Request cancelled: $reason"
        )
    }
}