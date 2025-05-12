package com.amadiyawa.feature_users.presentation.screen.common

/**
 * Generic contract for User List screens (clients, agents)
 */
class UserListContract {

    /**
     * Represents the UI state of a User List screen
     */
    data class State<T>(
        val isLoading: Boolean = true,
        val users: List<T> = emptyList(),
        val filteredUsers: List<T> = emptyList(),
        val error: String? = null,
        val searchQuery: String = "",
        val selectedArea: String? = null,
        val availableAreas: List<String> = emptyList(),
        val currentPage: Int = 0,
        val isLastPage: Boolean = false,
        val canCreateUser: Boolean = false,
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
        data class SelectUser(val userId: String) : Action()
        data object CreateNewUser : Action()
        data object NavigateBack : Action()
    }

    /**
     * Side effects that can be emitted by the ViewModel (one-time events)
     */
    sealed class Effect {
        data class NavigateToUserDetail(val userId: String) : Effect()
        data object NavigateToCreateUser : Effect()
        data object NavigateBack : Effect()
        data class ShowError(val message: String) : Effect()
    }
}