package com.amadiyawa.feature_users.presentation.screen.agent.list

import com.amadiyawa.feature_users.domain.model.Agent

/**
 * Contract class for AgentListScreen that defines the UI state, user actions, and side effects
 */
class AgentListContract {

    /**
     * Represents the UI state of the Agent List screen
     */
    data class State(
        val isLoading: Boolean = true,
        val agents: List<Agent> = emptyList(),
        val filteredAgents: List<Agent> = emptyList(),
        val error: String? = null,
        val searchQuery: String = "",
        val selectedTerritory: String? = null,
        val availableTerritories: List<String> = emptyList(),
        val currentPage: Int = 0,
        val isLastPage: Boolean = false,
        val canCreateAgent: Boolean = false,
        val noResultsFound: Boolean = false
    )

    /**
     * Actions that can be dispatched to the ViewModel
     */
    sealed class Action {
        data class Search(val query: String) : Action()
        data class SetTerritoryFilter(val territory: String?) : Action()
        data object LoadNextPage : Action()
        data object RefreshData : Action()
        data class SelectAgent(val agentId: String) : Action()
        data object CreateNewAgent : Action()
        data object NavigateBack : Action()
    }

    /**
     * Side effects that can be emitted by the ViewModel (one-time events)
     */
    sealed class Effect {
        data class NavigateToAgentDetail(val agentId: String) : Effect()
        data object NavigateToCreateAgent : Effect()
        data object NavigateBack : Effect()
        data class ShowError(val message: String) : Effect()
    }
}