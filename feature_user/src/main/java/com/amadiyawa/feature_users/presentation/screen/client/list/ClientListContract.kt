package com.amadiyawa.feature_users.presentation.screen.client.list

import com.amadiyawa.feature_users.domain.model.Client

/**
 * Contract class for ClientListScreen that defines the UI state, user actions, and side effects
 */
class ClientListContract {

    /**
     * Represents the UI state of the Client List screen
     */
    data class State(
        val isLoading: Boolean = true,
        val clients: List<Client> = emptyList(),
        val filteredClients: List<Client> = emptyList(),
        val error: String? = null,
        val searchQuery: String = "",
        val selectedArea: String? = null,
        val availableAreas: List<String> = emptyList(),
        val currentPage: Int = 0,
        val isLastPage: Boolean = false,
        val canCreateClient: Boolean = false,
        val noResultsFound: Boolean = false
    )

    /**
     * Actions that can be dispatched to the ViewModel
     */
    sealed class Action {
        data class Search(val query: String) : Action()
        data class SetAreaFilter(val area: String?) : Action()
        data object LoadNextPage : Action()
        data object RefreshData : Action()
        data class SelectClient(val clientId: String) : Action()
        data object CreateNewClient : Action()
        data object NavigateBack : Action()
    }

    /**
     * Side effects that can be emitted by the ViewModel (one-time events)
     */
    sealed class Effect {
        data class NavigateToClientDetail(val clientId: String) : Effect()
        data object NavigateToCreateClient : Effect()
        data object NavigateBack : Effect()
        data class ShowError(val message: String) : Effect()
    }
}