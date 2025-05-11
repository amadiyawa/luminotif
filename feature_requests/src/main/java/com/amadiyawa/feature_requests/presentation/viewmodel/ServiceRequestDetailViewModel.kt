package com.amadiyawa.feature_requests.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amadiyawa.feature_base.domain.model.UserSessionManager
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_requests.domain.model.RequestStatus
import com.amadiyawa.feature_requests.domain.usecase.CancelServiceRequestUseCase
import com.amadiyawa.feature_requests.domain.usecase.GetServiceRequestByIdUseCase
import com.amadiyawa.feature_requests.domain.usecase.GetServiceRequestUpdatesUseCase
import com.amadiyawa.feature_requests.domain.usecase.UpdateServiceRequestStatusUseCase
import com.amadiyawa.feature_requests.presentation.state.ServiceRequestDetailAction
import com.amadiyawa.feature_requests.presentation.state.ServiceRequestDetailEvent
import com.amadiyawa.feature_requests.presentation.state.ServiceRequestDetailUiState
import com.amadiyawa.feature_requests.presentation.state.StatusUpdateDialogState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ServiceRequestDetailViewModel(
    private val getServiceRequestById: GetServiceRequestByIdUseCase,
    private val getServiceRequestUpdates: GetServiceRequestUpdatesUseCase,
    private val updateServiceRequestStatus: UpdateServiceRequestStatusUseCase,
    private val cancelServiceRequest: CancelServiceRequestUseCase,
    userSessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceRequestDetailUiState>(ServiceRequestDetailUiState.Loading)
    val uiState: StateFlow<ServiceRequestDetailUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow(StatusUpdateDialogState())
    val dialogState: StateFlow<StatusUpdateDialogState> = _dialogState.asStateFlow()

    private val _events = Channel<ServiceRequestDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var currentRequestId: String? = null

    private var currentUserId = userSessionManager.currentUserId.value
    private var currentRole = userSessionManager.currentRole.value

    fun onAction(action: ServiceRequestDetailAction) {
        when (action) {
            is ServiceRequestDetailAction.LoadRequestDetail -> loadRequestDetail(action.requestId)
            is ServiceRequestDetailAction.ShowStatusUpdateDialog -> showStatusUpdateDialog()
            is ServiceRequestDetailAction.HideStatusUpdateDialog -> hideStatusUpdateDialog()
            is ServiceRequestDetailAction.SelectStatus -> selectStatus(action.status)
            is ServiceRequestDetailAction.UpdateComment -> updateComment(action.comment)
            is ServiceRequestDetailAction.SubmitStatusUpdate -> submitStatusUpdate()
            is ServiceRequestDetailAction.CancelRequest -> cancelRequest()
        }
    }

    private fun loadRequestDetail(requestId: String) {
        currentRequestId = requestId
        viewModelScope.launch {
            _uiState.value = ServiceRequestDetailUiState.Loading
            try {
                val request = getServiceRequestById(requestId)
                if (request != null) {
                    getServiceRequestUpdates(requestId).collect { updates ->
                        _uiState.value = ServiceRequestDetailUiState.Success(
                            request = request,
                            updates = updates
                        )
                    }
                } else {
                    _uiState.value = ServiceRequestDetailUiState.Error("Request not found")
                }
            } catch (e: Exception) {
                _uiState.value = ServiceRequestDetailUiState.Error(
                    e.message ?: "Failed to load request details"
                )
            }
        }
    }

    private fun showStatusUpdateDialog() {
        val currentState = _uiState.value
        if (currentState is ServiceRequestDetailUiState.Success) {
            val availableStatuses = getAvailableStatuses(currentState.request.status)
            if (availableStatuses.isEmpty()) {
                viewModelScope.launch {
                    _events.send(ServiceRequestDetailEvent.ShowError("No status transitions available"))
                }
            } else {
                _dialogState.update {
                    it.copy(
                        isShowing = true,
                        availableStatuses = availableStatuses,
                        selectedStatus = null,
                        comment = ""
                    )
                }
            }
        }
    }

    private fun hideStatusUpdateDialog() {
        _dialogState.update { it.copy(isShowing = false) }
    }

    private fun selectStatus(status: RequestStatus) {
        _dialogState.update { it.copy(selectedStatus = status) }
    }

    private fun updateComment(comment: String) {
        _dialogState.update { it.copy(comment = comment) }
    }

    private fun submitStatusUpdate() {
        viewModelScope.launch {
            val dialogState = _dialogState.value
            val requestId = currentRequestId
            val selectedStatus = dialogState.selectedStatus

            if (requestId == null || selectedStatus == null) {
                _events.send(ServiceRequestDetailEvent.ShowError("Please select a status"))
                return@launch
            }

            val currentState = _uiState.value
            if (currentState is ServiceRequestDetailUiState.Success) {
                _uiState.value = currentState.copy(isUpdating = true)

                try {
                    updateServiceRequestStatus(
                        requestId = requestId,
                        newStatus = selectedStatus,
                        agentId = currentUserId ?: "unknown",
                        comment = dialogState.comment
                    )

                    hideStatusUpdateDialog()
                    _events.send(ServiceRequestDetailEvent.StatusUpdateSuccess)
                    loadRequestDetail(requestId) // Reload to get updated data
                } catch (e: Exception) {
                    _uiState.value = currentState.copy(isUpdating = false)
                    _events.send(ServiceRequestDetailEvent.ShowError(
                        e.message ?: "Failed to update status"
                    ))
                }
            }
        }
    }

    private fun cancelRequest() {
        viewModelScope.launch {
            val requestId = currentRequestId ?: return@launch

            val currentState = _uiState.value
            if (currentState is ServiceRequestDetailUiState.Success) {
                _uiState.value = currentState.copy(isUpdating = true)

                try {
                    cancelServiceRequest(
                        requestId = requestId,
                        agentId = currentUserId ?: "unknown",
                        reason = "Cancelled by user"
                    )

                    _events.send(ServiceRequestDetailEvent.StatusUpdateSuccess)
                    loadRequestDetail(requestId) // Reload to get updated data
                } catch (e: Exception) {
                    _uiState.value = currentState.copy(isUpdating = false)
                    _events.send(ServiceRequestDetailEvent.ShowError(
                        e.message ?: "Failed to cancel request"
                    ))
                }
            }
        }
    }

    private fun getAvailableStatuses(currentStatus: RequestStatus): List<RequestStatus> {
        return when (currentStatus) {
            RequestStatus.PENDING -> listOf(RequestStatus.ASSIGNED, RequestStatus.CANCELLED)
            RequestStatus.ASSIGNED -> listOf(RequestStatus.IN_PROGRESS, RequestStatus.CANCELLED)
            RequestStatus.IN_PROGRESS -> listOf(RequestStatus.RESOLVED, RequestStatus.CANCELLED)
            RequestStatus.RESOLVED -> listOf(RequestStatus.CLOSED)
            RequestStatus.CLOSED -> emptyList()
            RequestStatus.CANCELLED -> emptyList()
        }
    }

    fun canUpdateStatus(): Boolean {
        return currentRole == UserRole.AGENT || currentRole == UserRole.ADMIN
    }
}