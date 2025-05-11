package com.amadiyawa.feature_requests.presentation.state

import com.amadiyawa.feature_requests.domain.model.RequestCategory
import com.amadiyawa.feature_requests.domain.model.RequestPriority
import com.amadiyawa.feature_requests.domain.model.RequestStatus
import com.amadiyawa.feature_requests.domain.model.ServiceRequest

sealed class ServiceRequestListUiState {
    data object Loading : ServiceRequestListUiState()
    data class Success(
        val requests: List<ServiceRequest>,
        val isRefreshing: Boolean = false
    ) : ServiceRequestListUiState()
    data class Error(val message: String) : ServiceRequestListUiState()
}

data class FilterOptions(
    val selectedStatus: RequestStatus? = null,
    val selectedCategory: RequestCategory? = null,
    val selectedPriority: RequestPriority? = null
)