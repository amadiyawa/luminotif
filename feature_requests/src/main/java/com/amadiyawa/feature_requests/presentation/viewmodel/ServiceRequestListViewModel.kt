package com.amadiyawa.feature_requests.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amadiyawa.feature_base.domain.model.UserSessionManager
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_requests.domain.model.RequestCategory
import com.amadiyawa.feature_requests.domain.model.RequestPriority
import com.amadiyawa.feature_requests.domain.model.RequestStatus
import com.amadiyawa.feature_requests.domain.usecase.FilterServiceRequestsUseCase
import com.amadiyawa.feature_requests.domain.usecase.GetServiceRequestsUseCase
import com.amadiyawa.feature_requests.presentation.state.FilterOptions
import com.amadiyawa.feature_requests.presentation.state.ServiceRequestListAction
import com.amadiyawa.feature_requests.presentation.state.ServiceRequestListEvent
import com.amadiyawa.feature_requests.presentation.state.ServiceRequestListUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ServiceRequestListViewModel(
    private val getServiceRequests: GetServiceRequestsUseCase,
    private val filterServiceRequests: FilterServiceRequestsUseCase,
    userSessionManager: UserSessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<ServiceRequestListUiState>(ServiceRequestListUiState.Loading)
    val uiState: StateFlow<ServiceRequestListUiState> = _uiState.asStateFlow()

    private val _filterOptions = MutableStateFlow(FilterOptions())
    val filterOptions: StateFlow<FilterOptions> = _filterOptions.asStateFlow()

    private val _events = Channel<ServiceRequestListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    var currentUserId = userSessionManager.currentUserId.value
    var currentRole = userSessionManager.currentRole.value

    init {
        loadRequests()
    }

    fun onAction(action: ServiceRequestListAction) {
        when (action) {
            is ServiceRequestListAction.LoadRequests -> loadRequests()
            is ServiceRequestListAction.RefreshRequests -> refreshRequests()
            is ServiceRequestListAction.FilterByStatus -> filterByStatus(action.status)
            is ServiceRequestListAction.FilterByCategory -> filterByCategory(action.category)
            is ServiceRequestListAction.FilterByPriority -> filterByPriority(action.priority)
            is ServiceRequestListAction.ClearFilters -> clearFilters()
        }
    }

    private fun loadRequests() {
        viewModelScope.launch {
            _uiState.value = ServiceRequestListUiState.Loading
            loadRequestsWithFilters()
        }
    }

    private fun refreshRequests() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                if (currentState is ServiceRequestListUiState.Success) {
                    currentState.copy(isRefreshing = true)
                } else {
                    currentState
                }
            }
            loadRequestsWithFilters()
        }
    }

    private suspend fun loadRequestsWithFilters() {
        try {
            val filters = _filterOptions.value

            val requestsFlow = when {
                filters.hasActiveFilters() -> {
                    filterServiceRequests(
                        status = filters.selectedStatus,
                        category = filters.selectedCategory,
                        priority = filters.selectedPriority
                    )
                }
                else -> {
                    when (currentRole) {
                        UserRole.CLIENT -> getServiceRequests.getByClient(currentUserId!!)
                        UserRole.AGENT -> getServiceRequests.getByAgent(currentUserId!!)
                        UserRole.ADMIN -> getServiceRequests.getAll()
                        else -> flowOf(emptyList())
                    }
                }
            }

            requestsFlow.collect { requests ->
                _uiState.value = ServiceRequestListUiState.Success(
                    requests = requests,
                    isRefreshing = false
                )
            }
        } catch (e: Exception) {
            _uiState.value = ServiceRequestListUiState.Error(
                e.message ?: "An error occurred while loading requests"
            )
            _events.send(ServiceRequestListEvent.ShowError(
                e.message ?: "Failed to load requests"
            ))
        }
    }

    private fun filterByStatus(status: RequestStatus?) {
        viewModelScope.launch {
            _filterOptions.update { it.copy(selectedStatus = status) }
            loadRequestsWithFilters()
        }
    }

    private fun filterByCategory(category: RequestCategory?) {
        viewModelScope.launch {
            _filterOptions.update { it.copy(selectedCategory = category) }
            loadRequestsWithFilters()
        }
    }

    private fun filterByPriority(priority: RequestPriority?) {
        viewModelScope.launch {
            _filterOptions.update { it.copy(selectedPriority = priority) }
            loadRequestsWithFilters()
        }
    }

    private fun clearFilters() {
        viewModelScope.launch {
            _filterOptions.value = FilterOptions()
            loadRequestsWithFilters()
        }
    }

    fun navigateToDetail(requestId: String) {
        viewModelScope.launch {
            _events.send(ServiceRequestListEvent.NavigateToDetail(requestId))
        }
    }

    fun navigateToCreate() {
        viewModelScope.launch {
            if (currentRole == UserRole.CLIENT) {
                _events.send(ServiceRequestListEvent.NavigateToCreate)
            } else {
                _events.send(ServiceRequestListEvent.ShowError("Only clients can create requests"))
            }
        }
    }

    private fun FilterOptions.hasActiveFilters(): Boolean {
        return selectedStatus != null || selectedCategory != null || selectedPriority != null
    }
}