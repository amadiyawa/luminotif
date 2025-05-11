package com.amadiyawa.feature_requests.presentation.state

sealed class ServiceRequestListEvent {
    data class NavigateToDetail(val requestId: String) : ServiceRequestListEvent()
    data object NavigateToCreate : ServiceRequestListEvent()
    data class ShowError(val message: String) : ServiceRequestListEvent()
}