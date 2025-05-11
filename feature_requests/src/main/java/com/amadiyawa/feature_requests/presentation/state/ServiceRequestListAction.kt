package com.amadiyawa.feature_requests.presentation.state

import com.amadiyawa.feature_requests.domain.model.RequestCategory
import com.amadiyawa.feature_requests.domain.model.RequestPriority
import com.amadiyawa.feature_requests.domain.model.RequestStatus


sealed class ServiceRequestListAction {
    data object LoadRequests : ServiceRequestListAction()
    data object RefreshRequests : ServiceRequestListAction()
    data class FilterByStatus(val status: RequestStatus?) : ServiceRequestListAction()
    data class FilterByCategory(val category: RequestCategory?) : ServiceRequestListAction()
    data class FilterByPriority(val priority: RequestPriority?) : ServiceRequestListAction()
    data object ClearFilters : ServiceRequestListAction()
}