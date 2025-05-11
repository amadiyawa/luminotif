package com.amadiyawa.feature_requests.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amadiyawa.feature_base.domain.model.UserSessionManager
import com.amadiyawa.feature_requests.domain.model.RequestCategory
import com.amadiyawa.feature_requests.domain.model.RequestPriority
import com.amadiyawa.feature_requests.domain.usecase.CreateServiceRequestUseCase
import com.amadiyawa.feature_requests.presentation.state.CreateServiceRequestAction
import com.amadiyawa.feature_requests.presentation.state.CreateServiceRequestEvent
import com.amadiyawa.feature_requests.presentation.state.CreateServiceRequestUiState
import com.amadiyawa.feature_requests.presentation.state.ServiceRequestFormState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateServiceRequestViewModel(
    private val createServiceRequest: CreateServiceRequestUseCase,
    userSessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateServiceRequestUiState>(CreateServiceRequestUiState.Initial)
    val uiState: StateFlow<CreateServiceRequestUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(ServiceRequestFormState())
    val formState: StateFlow<ServiceRequestFormState> = _formState.asStateFlow()

    private val _events = Channel<CreateServiceRequestEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var currentUserId = userSessionManager.currentUserId.value
    private var currentRole = userSessionManager.currentRole.value

    fun onAction(action: CreateServiceRequestAction) {
        when (action) {
            is CreateServiceRequestAction.UpdateTitle -> updateTitle(action.title)
            is CreateServiceRequestAction.UpdateDescription -> updateDescription(action.description)
            is CreateServiceRequestAction.UpdateCategory -> updateCategory(action.category)
            is CreateServiceRequestAction.UpdatePriority -> updatePriority(action.priority)
            is CreateServiceRequestAction.SubmitRequest -> submitRequest()
        }
    }

    private fun updateTitle(title: String) {
        _formState.update { it.copy(title = title) }
        validateForm()
    }

    private fun updateDescription(description: String) {
        _formState.update { it.copy(description = description) }
        validateForm()
    }

    private fun updateCategory(category: RequestCategory) {
        _formState.update { it.copy(category = category) }
        validateForm()
    }

    private fun updatePriority(priority: RequestPriority) {
        _formState.update { it.copy(priority = priority) }
        validateForm()
    }

    private fun validateForm() {
        _formState.update { currentState ->
            currentState.copy(
                isValid = currentState.title.isNotBlank() &&
                        currentState.description.isNotBlank() &&
                        currentState.category != null
            )
        }
    }

    private fun submitRequest() {
        viewModelScope.launch {
            val form = _formState.value

            if (!form.isValid || currentUserId == null) {
                _events.send(CreateServiceRequestEvent.ShowError("Please fill all required fields"))
                return@launch
            }

            _uiState.value = CreateServiceRequestUiState.Submitting

            try {
                val newRequest = createServiceRequest(
                    clientId = currentUserId!!,
                    title = form.title,
                    description = form.description,
                    category = form.category!!,
                    priority = form.priority
                )

                _uiState.value = CreateServiceRequestUiState.Success(newRequest.id)
                _events.send(CreateServiceRequestEvent.NavigateToDetail(newRequest.id))
            } catch (e: Exception) {
                _uiState.value = CreateServiceRequestUiState.Error(
                    e.message ?: "Failed to create request"
                )
                _events.send(CreateServiceRequestEvent.ShowError(
                    e.message ?: "Failed to create request"
                ))
            }
        }
    }
}