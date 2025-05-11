package com.amadiyawa.feature_requests.presentation.state

sealed class CreateServiceRequestEvent {
    data class NavigateToDetail(val requestId: String) : CreateServiceRequestEvent()
    data object NavigateBack : CreateServiceRequestEvent()
    data class ShowError(val message: String) : CreateServiceRequestEvent()
}