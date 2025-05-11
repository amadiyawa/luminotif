package com.amadiyawa.feature_requests.presentation.state

sealed class ServiceRequestDetailEvent {
    data object NavigateBack : ServiceRequestDetailEvent()
    data class ShowError(val message: String) : ServiceRequestDetailEvent()
    data object StatusUpdateSuccess : ServiceRequestDetailEvent()
}