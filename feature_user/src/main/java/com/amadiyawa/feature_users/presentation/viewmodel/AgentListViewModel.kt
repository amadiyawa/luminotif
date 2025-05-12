package com.amadiyawa.feature_users.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amadiyawa.feature_base.domain.model.UserSessionManager
import com.amadiyawa.feature_base.domain.result.OperationResult
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_users.domain.model.Agent
import com.amadiyawa.feature_users.domain.usecase.GetAgentsByTerritoryUseCase
import com.amadiyawa.feature_users.domain.usecase.GetAgentsPagedUseCase
import com.amadiyawa.feature_users.domain.usecase.GetAllAgentsUseCase
import com.amadiyawa.feature_users.domain.usecase.ObserveAgentUpdatesUseCase
import com.amadiyawa.feature_users.presentation.screen.agent.list.AgentListContract
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the Agent List screen
 */
class AgentListViewModel(
    private val getAllAgentsUseCase: GetAllAgentsUseCase,
    private val getAgentsByTerritoryUseCase: GetAgentsByTerritoryUseCase,
    private val getAgentsPagedUseCase: GetAgentsPagedUseCase,
    private val observeAgentUpdatesUseCase: ObserveAgentUpdatesUseCase,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(AgentListContract.State())
    val state: StateFlow<AgentListContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AgentListContract.Effect>()
    val effect: SharedFlow<AgentListContract.Effect> = _effect.asSharedFlow()

    // Cache for loaded agents to avoid unnecessary network calls
    private val allLoadedAgents = mutableListOf<Agent>()
    private val pageSize = 20

    init {
        // Check if user can create agents (ADMIN only)
        _state.value = _state.value.copy(
            canCreateAgent = userSessionManager.currentRole.value == UserRole.ADMIN
        )

        // Only ADMIN should be able to access this screen
        if (userSessionManager.currentRole.value != UserRole.ADMIN) {
            viewModelScope.launch {
                _effect.emit(AgentListContract.Effect.ShowError("Only admins can view agents"))
                _effect.emit(AgentListContract.Effect.NavigateBack)
            }
        } else {
            // Start observing agent updates
            observeAgentUpdates()

            // Load initial data
            loadAgents()
        }
    }

    /**
     * Handles actions dispatched from the UI
     */
    fun handleAction(action: AgentListContract.Action) {
        if (userSessionManager.currentRole.value != UserRole.ADMIN) {
            // Only admins can perform actions on this screen
            viewModelScope.launch {
                _effect.emit(AgentListContract.Effect.ShowError("Only admins can manage agents"))
                _effect.emit(AgentListContract.Effect.NavigateBack)
            }
            return
        }

        when (action) {
            is AgentListContract.Action.Search -> {
                updateSearchQuery(action.query)
            }
            is AgentListContract.Action.SetTerritoryFilter -> {
                updateSelectedTerritory(action.territory)
            }
            is AgentListContract.Action.LoadNextPage -> {
                loadNextPage()
            }
            is AgentListContract.Action.RefreshData -> {
                refreshData()
            }
            is AgentListContract.Action.SelectAgent -> {
                viewModelScope.launch {
                    _effect.emit(AgentListContract.Effect.NavigateToAgentDetail(action.agentId))
                }
            }
            is AgentListContract.Action.CreateNewAgent -> {
                viewModelScope.launch {
                    _effect.emit(AgentListContract.Effect.NavigateToCreateAgent)
                }
            }
            is AgentListContract.Action.NavigateBack -> {
                viewModelScope.launch {
                    _effect.emit(AgentListContract.Effect.NavigateBack)
                }
            }
        }
    }

    /**
     * Observes agent updates to refresh UI when data changes
     */
    private fun observeAgentUpdates() {
        viewModelScope.launch {
            try {
                observeAgentUpdatesUseCase().collect { updatedAgents ->
                    // Only update if we have already loaded agents
                    if (allLoadedAgents.isNotEmpty()) {
                        allLoadedAgents.clear()
                        allLoadedAgents.addAll(updatedAgents)

                        // Update the filtered list based on current search and filter
                        applyFilters()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error observing agent updates")
            }
        }
    }

    /**
     * Loads all agents
     */
    private fun loadAgents() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            when (val result = getAllAgentsUseCase()) {
                is OperationResult.Success -> {
                    val agents = result.data

                    // Extract unique territories for filtering
                    val territories = agents.flatMap { it.territories }.distinct().sorted()

                    // Store all agents
                    allLoadedAgents.clear()
                    allLoadedAgents.addAll(agents)

                    // Update state
                    _state.value = _state.value.copy(
                        agents = agents,
                        filteredAgents = agents,
                        availableTerritories = territories,
                        isLoading = false,
                        error = null,
                        isLastPage = true, // Since we loaded all at once
                        noResultsFound = agents.isEmpty()
                    )
                }
                is OperationResult.Failure -> {
                    Timber.e("Business failure loading agents: ${result.message}")
                    _state.value = _state.value.copy(
                        error = result.message ?: "Failed to load agents due to a business rule",
                        isLoading = false,
                        noResultsFound = true
                    )

                    _effect.emit(AgentListContract.Effect.ShowError(
                        result.message ?: "Failed to load agents"
                    ))
                }
                is OperationResult.Error -> {
                    Timber.e(result.throwable, "Error loading agents: ${result.message}")
                    _state.value = _state.value.copy(
                        error = result.message ?: "An unexpected error occurred while loading agents",
                        isLoading = false,
                        noResultsFound = true
                    )

                    _effect.emit(AgentListContract.Effect.ShowError(
                        result.message ?: "An unexpected error occurred"
                    ))
                }
            }
        }
    }

    /**
     * Loads agents with pagination
     */
    private fun loadAgentsPage(page: Int) {
        viewModelScope.launch {
            if (page == 0) {
                _state.value = _state.value.copy(isLoading = true)
            }

            when (val result = getAgentsPagedUseCase(page, pageSize)) {
                is OperationResult.Success -> {
                    val newAgents = result.data

                    // If it's the first page, clear the list
                    if (page == 0) {
                        allLoadedAgents.clear()
                    }

                    // Add new agents to the full list
                    allLoadedAgents.addAll(newAgents)

                    // Extract unique territories for filtering
                    val territories = allLoadedAgents.flatMap { it.territories }.distinct().sorted()

                    // Update state
                    _state.value = _state.value.copy(
                        agents = allLoadedAgents.toList(),
                        filteredAgents = applyFiltersToAgents(allLoadedAgents),
                        availableTerritories = territories,
                        isLoading = false,
                        error = null,
                        currentPage = page,
                        isLastPage = newAgents.size < pageSize,
                        noResultsFound = allLoadedAgents.isEmpty()
                    )
                }
                is OperationResult.Failure -> {
                    Timber.e("Business failure loading agents page $page: ${result.message}")
                    _state.value = _state.value.copy(
                        error = result.message,
                        isLoading = false
                    )

                    _effect.emit(AgentListContract.Effect.ShowError(
                        result.message ?: "Failed to load more agents"
                    ))
                }
                is OperationResult.Error -> {
                    Timber.e(result.throwable, "Error loading agents page $page: ${result.message}")
                    _state.value = _state.value.copy(
                        error = result.message,
                        isLoading = false
                    )

                    _effect.emit(AgentListContract.Effect.ShowError(
                        result.message ?: "An unexpected error occurred while loading more agents"
                    ))
                }
            }
        }
    }

    /**
     * Loads the next page of agents
     */
    private fun loadNextPage() {
        if (!_state.value.isLastPage && !_state.value.isLoading) {
            loadAgentsPage(_state.value.currentPage + 1)
        }
    }

    /**
     * Updates the search query and applies filters
     */
    private fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }

    /**
     * Updates the selected territory and applies filters
     */
    private fun updateSelectedTerritory(territory: String?) {
        _state.value = _state.value.copy(selectedTerritory = territory)
        applyFilters()
    }

    /**
     * Applies current search query and territory filter to the loaded agents
     */
    private fun applyFilters() {
        _state.value = _state.value.copy(
            filteredAgents = applyFiltersToAgents(allLoadedAgents),
            noResultsFound = false
        )

        // Check if we have any results after filtering
        if (_state.value.filteredAgents.isEmpty() && allLoadedAgents.isNotEmpty()) {
            _state.value = _state.value.copy(noResultsFound = true)
        }
    }

    /**
     * Applies filters to the given list of agents
     */
    private fun applyFiltersToAgents(agents: List<Agent>): List<Agent> {
        var filtered = agents

        // Apply search query filter
        val query = _state.value.searchQuery
        if (query.isNotEmpty()) {
            filtered = filtered.filter { agent ->
                agent.fullName.contains(query, ignoreCase = true) ||
                        agent.email.contains(query, ignoreCase = true) ||
                        agent.phoneNumber.contains(query, ignoreCase = true) ||
                        agent.employeeId.contains(query, ignoreCase = true)
            }
        }

        // Apply territory filter
        _state.value.selectedTerritory?.let { territory ->
            filtered = filtered.filter { agent ->
                agent.territories.contains(territory)
            }
        }

        return filtered
    }

    /**
     * Refreshes the agent data
     */
    fun refreshData() {
        _state.value = _state.value.copy(
            searchQuery = "",
            selectedTerritory = null,
            currentPage = 0
        )
        loadAgents()
    }
}