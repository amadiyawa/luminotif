package com.amadiyawa.feature_users.presentation.screen.dashboard

import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_users.domain.repository.UserStatistics

/**
 * Contract class for UserDashboardScreen that defines the UI state, user actions, and side effects
 */
class UserDashboardContract {

    /**
     * Represents the UI state of the User Dashboard screen
     */
    data class State(
        val isLoading: Boolean = true,
        val error: String? = null,
        val userRole: UserRole? = null,
        val userName: String? = null,
        val userStatistics: UserStatistics? = null,
        val canViewAgents: Boolean = false,
        val canViewClients: Boolean = true
    )

    /**
     * Actions that can be dispatched to the ViewModel
     */
    sealed class Action {
        data object NavigateToClients : Action()
        data object NavigateToAgents : Action()
        data object RefreshData : Action()
    }

    /**
     * Side effects that can be emitted by the ViewModel (one-time events)
     */
    sealed class Effect {
        data object NavigateToClientList : Effect()
        data object NavigateToAgentList : Effect()
        data class ShowError(val message: String) : Effect()
    }
}