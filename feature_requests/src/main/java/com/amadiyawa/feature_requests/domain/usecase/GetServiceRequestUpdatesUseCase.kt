package com.amadiyawa.feature_requests.domain.usecase

import com.amadiyawa.feature_requests.domain.model.RequestUpdate
import com.amadiyawa.feature_requests.domain.repository.ServiceRequestRepository
import kotlinx.coroutines.flow.Flow

class GetServiceRequestUpdatesUseCase(
    private val repository: ServiceRequestRepository
) {
    operator fun invoke(requestId: String): Flow<List<RequestUpdate>> {
        return repository.getRequestUpdates(requestId)
    }
}