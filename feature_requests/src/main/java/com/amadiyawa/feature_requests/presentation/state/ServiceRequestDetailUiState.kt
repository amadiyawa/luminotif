package com.amadiyawa.feature_requests.presentation.state

import com.amadiyawa.feature_requests.domain.model.RequestStatus
import com.amadiyawa.feature_requests.domain.model.RequestUpdate
import com.amadiyawa.feature_requests.domain.model.ServiceRequest

sealed class ServiceRequestDetailUiState {
    data object Loading : ServiceRequestDetailUiState()
    data class Success(
        val request: ServiceRequest,
        val updates: List<RequestUpdate>,
        val isUpdating: Boolean = false
    ) : ServiceRequestDetailUiState()
    data class Error(val message: String) : ServiceRequestDetailUiState()
}

data class StatusUpdateDialogState(
    val isShowing: Boolean = false,
    val availableStatuses: List<RequestStatus> = emptyList(),
    val selectedStatus: RequestStatus? = null,
    val comment: String = ""
)