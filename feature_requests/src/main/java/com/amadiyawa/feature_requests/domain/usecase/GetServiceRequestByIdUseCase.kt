package com.amadiyawa.feature_requests.domain.usecase

import com.amadiyawa.feature_requests.domain.model.ServiceRequest
import com.amadiyawa.feature_requests.domain.repository.ServiceRequestRepository

class GetServiceRequestByIdUseCase(
    private val repository: ServiceRequestRepository
) {
    suspend operator fun invoke(requestId: String): ServiceRequest? {
        return repository.getRequestById(requestId)
    }
}