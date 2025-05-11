package com.amadiyawa.feature_requests.presentation.state

import com.amadiyawa.feature_requests.domain.model.RequestCategory
import com.amadiyawa.feature_requests.domain.model.RequestPriority

sealed class CreateServiceRequestAction {
    data class UpdateTitle(val title: String) : CreateServiceRequestAction()
    data class UpdateDescription(val description: String) : CreateServiceRequestAction()
    data class UpdateCategory(val category: RequestCategory) : CreateServiceRequestAction()
    data class UpdatePriority(val priority: RequestPriority) : CreateServiceRequestAction()
    data object SubmitRequest : CreateServiceRequestAction()
}