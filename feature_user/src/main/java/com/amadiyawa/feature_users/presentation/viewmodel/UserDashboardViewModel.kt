package com.amadiyawa.feature_users.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amadiyawa.feature_base.domain.model.UserSessionManager
import com.amadiyawa.feature_base.domain.result.OperationResult
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_users.domain.usecase.GetUserStatisticsUseCase
import com.amadiyawa.feature_users.presentation.screen.dashboard.UserDashboardContract
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the User Dashboard screen
 */
class UserDashboardViewModel(
    private val getUserStatisticsUseCase: GetUserStatisticsUseCase,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(UserDashboardContract.State())
    val state: StateFlow<UserDashboardContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<UserDashboardContract.Effect>()
    val effect: SharedFlow<UserDashboardContract.Effect> = _effect.asSharedFlow()

    init {
        // Load initial data
        loadUserData()

        // Only load statistics if user is an admin
        if (userSessionManager.currentRole.value == UserRole.ADMIN) {
            loadStatistics()
        }
    }

    /**
     * Handles actions dispatched from the UI
     */
    fun handleAction(action: UserDashboardContract.Action) {
        when (action) {
            is UserDashboardContract.Action.NavigateToClients -> {
                viewModelScope.launch {
                    _effect.emit(UserDashboardContract.Effect.NavigateToClientList)
                }
            }
            is UserDashboardContract.Action.NavigateToAgents -> {
                if (state.value.canViewAgents) {
                    viewModelScope.launch {
                        _effect.emit(UserDashboardContract.Effect.NavigateToAgentList)
                    }
                }
            }
            is UserDashboardContract.Action.RefreshData -> {
                refreshData()
            }
        }
    }

    /**
     * Loads the current user's information
     */
    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val currentRole = userSessionManager.currentRole.value

                // Get user name
                val userName = getCurrentUserName()

                // Set permissions based on role
                val canViewAgents = currentRole == UserRole.ADMIN

                _state.value = _state.value.copy(
                    userRole = currentRole,
                    userName = userName,
                    canViewAgents = canViewAgents,
                    isLoading = currentRole == UserRole.ADMIN, // Keep loading for ADMIN until stats load
                    error = null
                )

                // If not admin, we're done loading
                if (currentRole != UserRole.ADMIN) {
                    _state.value = _state.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading user data")
                _state.value = _state.value.copy(
                    error = "Failed to load user data: ${e.message}",
                    isLoading = false
                )

                viewModelScope.launch {
                    _effect.emit(UserDashboardContract.Effect.ShowError("Failed to load user profile"))
                }
            }
        }
    }

    /**
     * Get user name based on current user role
     * In a real app, you would use the userId to fetch the actual user name
     */
    private fun getCurrentUserName(): String {
        // In a real app, fetch this from repository using the userSessionManager.currentUserId.value
        return when (userSessionManager.currentRole.value) {
            UserRole.ADMIN -> "Admin User"
            UserRole.AGENT -> "Field Agent"
            UserRole.CLIENT -> "Client User"
            else -> "Unknown User"
        }
    }

    /**
     * Loads statistics for admin dashboard
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            when (val result = getUserStatisticsUseCase()) {
                is OperationResult.Success -> {
                    _state.value = _state.value.copy(
                        userStatistics = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is OperationResult.Failure -> {
                    Timber.e("Business failure loading user statistics: ${result.message}")
                    _state.value = _state.value.copy(
                        error = result.message ?: "Failed to load statistics due to a business rule",
                        isLoading = false
                    )

                    _effect.emit(UserDashboardContract.Effect.ShowError(
                        result.message ?: "Failed to load statistics"
                    ))
                }
                is OperationResult.Error -> {
                    Timber.e(result.throwable, "Error loading user statistics: ${result.message}")
                    _state.value = _state.value.copy(
                        error = result.message ?: "An unexpected error occurred while loading statistics",
                        isLoading = false
                    )

                    _effect.emit(UserDashboardContract.Effect.ShowError(
                        result.message ?: "An unexpected error occurred"
                    ))
                }
            }
        }
    }

    /**
     * Refreshes the dashboard data
     */
    private fun refreshData() {
        _state.value = _state.value.copy(isLoading = true, error = null)

        loadUserData()

        if (userSessionManager.currentRole.value == UserRole.ADMIN) {
            loadStatistics()
        }
    }
}