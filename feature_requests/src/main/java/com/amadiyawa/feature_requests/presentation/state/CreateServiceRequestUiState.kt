package com.amadiyawa.feature_requests.presentation.state

import com.amadiyawa.feature_requests.domain.model.RequestCategory
import com.amadiyawa.feature_requests.domain.model.RequestPriority

sealed class CreateServiceRequestUiState {
    data object Initial : CreateServiceRequestUiState()
    data object Submitting : CreateServiceRequestUiState()
    data class Success(val requestId: String) : CreateServiceRequestUiState()
    data class Error(val message: String) : CreateServiceRequestUiState()
}

data class ServiceRequestFormState(
    val title: String = "",
    val description: String = "",
    val category: RequestCategory? = null,
    val priority: RequestPriority = RequestPriority.MEDIUM,
    val isValid: Boolean = false
)