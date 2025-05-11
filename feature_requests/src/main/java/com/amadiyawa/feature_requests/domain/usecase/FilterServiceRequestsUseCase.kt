package com.amadiyawa.feature_requests.domain.usecase

import com.amadiyawa.feature_requests.domain.model.RequestCategory
import com.amadiyawa.feature_requests.domain.model.RequestPriority
import com.amadiyawa.feature_requests.domain.model.RequestStatus
import com.amadiyawa.feature_requests.domain.model.ServiceRequest
import com.amadiyawa.feature_requests.domain.repository.ServiceRequestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FilterServiceRequestsUseCase(
    private val repository: ServiceRequestRepository
) {
    operator fun invoke(
        status: RequestStatus? = null,
        category: RequestCategory? = null,
        priority: RequestPriority? = null
    ): Flow<List<ServiceRequest>> {
        return repository.getAllRequests().map { requests ->
            requests.filter { request ->
                (status == null || request.status == status) &&
                        (category == null || request.category == category) &&
                        (priority == null || request.priority == priority)
            }
        }
    }
}