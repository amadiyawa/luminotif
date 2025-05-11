package com.amadiyawa.feature_requests.domain.usecase

import com.amadiyawa.feature_requests.domain.model.RequestStatus
import com.amadiyawa.feature_requests.domain.model.RequestUpdate
import com.amadiyawa.feature_requests.domain.repository.ServiceRequestRepository

class UpdateServiceRequestStatusUseCase(
    private val repository: ServiceRequestRepository
) {
    suspend operator fun invoke(
        requestId: String,
        newStatus: RequestStatus,
        agentId: String,
        comment: String
    ): RequestUpdate {
        // Validate status transitions
        val currentRequest = repository.getRequestById(requestId)
            ?: throw IllegalArgumentException("Service request not found")

        validateStatusTransition(currentRequest.status, newStatus)

        return repository.updateRequestStatus(requestId, newStatus, agentId, comment)
    }

    private fun validateStatusTransition(currentStatus: RequestStatus, newStatus: RequestStatus) {
        val validTransitions = when (currentStatus) {
            RequestStatus.PENDING -> listOf(RequestStatus.ASSIGNED, RequestStatus.CANCELLED)
            RequestStatus.ASSIGNED -> listOf(RequestStatus.IN_PROGRESS, RequestStatus.CANCELLED)
            RequestStatus.IN_PROGRESS -> listOf(RequestStatus.RESOLVED, RequestStatus.CANCELLED)
            RequestStatus.RESOLVED -> listOf(RequestStatus.CLOSED)
            RequestStatus.CLOSED -> emptyList()
            RequestStatus.CANCELLED -> emptyList()
        }

        check(newStatus in validTransitions) { "Invalid status transition from $currentStatus to $newStatus" }
    }
}