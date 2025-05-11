package com.amadiyawa.feature_requests.presentation.state

import com.amadiyawa.feature_requests.domain.model.RequestStatus

sealed class ServiceRequestDetailAction {
    data class LoadRequestDetail(val requestId: String) : ServiceRequestDetailAction()
    data object ShowStatusUpdateDialog : ServiceRequestDetailAction()
    data object HideStatusUpdateDialog : ServiceRequestDetailAction()
    data class SelectStatus(val status: RequestStatus) : ServiceRequestDetailAction()
    data class UpdateComment(val comment: String) : ServiceRequestDetailAction()
    data object SubmitStatusUpdate : ServiceRequestDetailAction()
    data object CancelRequest : ServiceRequestDetailAction()
}