package com.amadiyawa.feature_requests.domain.usecase

import com.amadiyawa.feature_requests.domain.model.RequestCategory
import com.amadiyawa.feature_requests.domain.model.RequestPriority
import com.amadiyawa.feature_requests.domain.model.RequestStatus
import com.amadiyawa.feature_requests.domain.model.ServiceRequest
import com.amadiyawa.feature_requests.domain.repository.ServiceRequestRepository
import java.time.LocalDateTime
import java.util.UUID

class CreateServiceRequestUseCase(
    private val repository: ServiceRequestRepository
) {
    suspend operator fun invoke(
        clientId: String,
        title: String,
        description: String,
        category: RequestCategory,
        priority: RequestPriority = RequestPriority.MEDIUM
    ): ServiceRequest {
        val request = ServiceRequest(
            id = UUID.randomUUID().toString(),
            clientId = clientId,
            title = title,
            description = description,
            category = category,
            priority = priority,
            status = RequestStatus.PENDING,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            assignedAgentId = null
        )

        return repository.createRequest(request)
    }
}