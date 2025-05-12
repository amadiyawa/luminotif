package com.amadiyawa.feature_users.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amadiyawa.feature_base.domain.model.UserSessionManager
import com.amadiyawa.feature_base.domain.result.OperationResult
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_users.domain.model.Client
import com.amadiyawa.feature_users.domain.usecase.GetAllClientsUseCase
import com.amadiyawa.feature_users.domain.usecase.GetClientsPagedUseCase
import com.amadiyawa.feature_users.domain.usecase.ObserveClientUpdatesUseCase
import com.amadiyawa.feature_users.domain.usecase.SearchClientsUseCase
import com.amadiyawa.feature_users.presentation.screen.client.list.ClientListContract
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the Client List screen
 */
/**
 * ViewModel for the Client List screen
 */
class ClientListViewModel(
    private val getAllClientsUseCase: GetAllClientsUseCase,
    private val searchClientsUseCase: SearchClientsUseCase,
    private val getClientsPagedUseCase: GetClientsPagedUseCase,
    private val observeClientUpdatesUseCase: ObserveClientUpdatesUseCase,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ClientListContract.State())
    val state: StateFlow<ClientListContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ClientListContract.Effect>()
    val effect: SharedFlow<ClientListContract.Effect> = _effect.asSharedFlow()

    // Cache for loaded clients to avoid unnecessary network calls
    private val allLoadedClients = mutableListOf<Client>()
    private val pageSize = 20

    init {
        // Check if user can create clients (ADMIN only)
        _state.value = _state.value.copy(
            canCreateClient = userSessionManager.currentRole.value == UserRole.ADMIN
        )

        // Start observing client updates
        observeClientUpdates()

        // Load initial data
        loadClients()
    }

    /**
     * Handles actions dispatched from the UI
     */
    fun handleAction(action: ClientListContract.Action) {
        when (action) {
            is ClientListContract.Action.Search -> {
                updateSearchQuery(action.query)
            }
            is ClientListContract.Action.SetAreaFilter -> {
                updateSelectedArea(action.area)
            }
            is ClientListContract.Action.LoadNextPage -> {
                loadNextPage()
            }
            is ClientListContract.Action.RefreshData -> {
                refreshData()
            }
            is ClientListContract.Action.SelectClient -> {
                viewModelScope.launch {
                    _effect.emit(ClientListContract.Effect.NavigateToClientDetail(action.clientId))
                }
            }
            is ClientListContract.Action.CreateNewClient -> {
                viewModelScope.launch {
                    _effect.emit(ClientListContract.Effect.NavigateToCreateClient)
                }
            }
            is ClientListContract.Action.NavigateBack -> {
                viewModelScope.launch {
                    _effect.emit(ClientListContract.Effect.NavigateBack)
                }
            }
        }
    }

    /**
     * Observes client updates to refresh UI when data changes
     */
    private fun observeClientUpdates() {
        viewModelScope.launch {
            try {
                observeClientUpdatesUseCase().collect { updatedClients ->
                    // Only update if we have already loaded clients
                    if (allLoadedClients.isNotEmpty()) {
                        allLoadedClients.clear()
                        allLoadedClients.addAll(updatedClients)

                        // Update the filtered list based on current search and filter
                        applyFilters()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error observing client updates")
            }
        }
    }

    /**
     * Loads all clients
     */
    private fun loadClients() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            when (val result = getAllClientsUseCase()) {
                is OperationResult.Success -> {
                    val clients = result.data

                    // Extract unique areas for filtering
                    val areas = clients.map { it.area }.distinct().sorted()

                    // Store all clients
                    allLoadedClients.clear()
                    allLoadedClients.addAll(clients)

                    // Update state
                    _state.value = _state.value.copy(
                        clients = clients,
                        filteredClients = clients,
                        availableAreas = areas,
                        isLoading = false,
                        error = null,
                        isLastPage = true, // Since we loaded all at once
                        noResultsFound = clients.isEmpty()
                    )
                }
                is OperationResult.Failure -> {
                    Timber.e("Business failure loading clients: ${result.message}")
                    _state.value = _state.value.copy(
                        error = result.message ?: "Failed to load clients due to a business rule",
                        isLoading = false,
                        noResultsFound = true
                    )

                    _effect.emit(ClientListContract.Effect.ShowError(
                        result.message ?: "Failed to load clients"
                    ))
                }
                is OperationResult.Error -> {
                    Timber.e(result.throwable, "Error loading clients: ${result.message}")
                    _state.value = _state.value.copy(
                        error = result.message ?: "An unexpected error occurred while loading clients",
                        isLoading = false,
                        noResultsFound = true
                    )

                    _effect.emit(ClientListContract.Effect.ShowError(
                        result.message ?: "An unexpected error occurred"
                    ))
                }
            }
        }
    }

    /**
     * Loads clients with pagination
     */
    private fun loadClientsPage(page: Int) {
        viewModelScope.launch {
            if (page == 0) {
                _state.value = _state.value.copy(isLoading = true)
            }

            when (val result = getClientsPagedUseCase(page, pageSize)) {
                is OperationResult.Success -> {
                    val newClients = result.data

                    // If it's the first page, clear the list
                    if (page == 0) {
                        allLoadedClients.clear()
                    }

                    // Add new clients to the full list
                    allLoadedClients.addAll(newClients)

                    // Extract unique areas for filtering
                    val areas = allLoadedClients.map { it.area }.distinct().sorted()

                    // Update state
                    _state.value = _state.value.copy(
                        clients = allLoadedClients.toList(),
                        filteredClients = applyFiltersToClients(allLoadedClients),
                        availableAreas = areas,
                        isLoading = false,
                        error = null,
                        currentPage = page,
                        isLastPage = newClients.size < pageSize,
                        noResultsFound = allLoadedClients.isEmpty()
                    )
                }
                is OperationResult.Failure -> {
                    Timber.e("Business failure loading clients page $page: ${result.message}")
                    _state.value = _state.value.copy(
                        error = result.message,
                        isLoading = false
                    )

                    _effect.emit(ClientListContract.Effect.ShowError(
                        result.message ?: "Failed to load more clients"
                    ))
                }
                is OperationResult.Error -> {
                    Timber.e(result.throwable, "Error loading clients page $page: ${result.message}")
                    _state.value = _state.value.copy(
                        error = result.message,
                        isLoading = false
                    )

                    _effect.emit(ClientListContract.Effect.ShowError(
                        result.message ?: "An unexpected error occurred while loading more clients"
                    ))
                }
            }
        }
    }

    /**
     * Loads the next page of clients
     */
    private fun loadNextPage() {
        if (!_state.value.isLastPage && !_state.value.isLoading) {
            loadClientsPage(_state.value.currentPage + 1)
        }
    }

    /**
     * Updates the search query and applies filters
     */
    private fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)

        // If query is not empty and at least 3 characters, search on the server
        if (query.length >= 3) {
            performServerSearch(query)
        } else {
            // Otherwise just filter the loaded clients
            applyFilters()
        }
    }

    /**
     * Performs a server-side search
     */
    private fun performServerSearch(query: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            when (val result = searchClientsUseCase(query)) {
                is OperationResult.Success -> {
                    val searchResults = result.data

                    // Apply area filter to search results if needed
                    val filteredResults = if (_state.value.selectedArea != null) {
                        searchResults.filter { it.area == _state.value.selectedArea }
                    } else {
                        searchResults
                    }

                    _state.value = _state.value.copy(
                        filteredClients = filteredResults,
                        isLoading = false,
                        noResultsFound = filteredResults.isEmpty()
                    )
                }
                is OperationResult.Failure -> {
                    Timber.e("Business failure searching clients: ${result.message}")

                    // Fallback to local filtering if search fails
                    applyFilters()

                    // Show a warning but don't disrupt the UI flow
                    result.message?.let { message ->
                        _effect.emit(ClientListContract.Effect.ShowError(message))
                    }

                    _state.value = _state.value.copy(isLoading = false)
                }
                is OperationResult.Error -> {
                    Timber.e(result.throwable, "Error searching clients with query: $query")

                    // Fallback to local filtering if search fails
                    applyFilters()

                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }
    }

    /**
     * Updates the selected area and applies filters
     */
    private fun updateSelectedArea(area: String?) {
        _state.value = _state.value.copy(selectedArea = area)
        applyFilters()
    }

    /**
     * Applies current search query and area filter to the loaded clients
     */
    private fun applyFilters() {
        _state.value = _state.value.copy(
            filteredClients = applyFiltersToClients(allLoadedClients),
            noResultsFound = false
        )

        // Check if we have any results after filtering
        if (_state.value.filteredClients.isEmpty() && allLoadedClients.isNotEmpty()) {
            _state.value = _state.value.copy(noResultsFound = true)
        }
    }

    /**
     * Applies filters to the given list of clients
     */
    private fun applyFiltersToClients(clients: List<Client>): List<Client> {
        var filtered = clients

        // Apply search query filter (if less than 3 chars, we do local filtering)
        val query = _state.value.searchQuery
        if (query.isNotEmpty() && query.length < 3) {
            filtered = filtered.filter { client ->
                client.fullName.contains(query, ignoreCase = true) ||
                        client.email.contains(query, ignoreCase = true) ||
                        client.phoneNumber.contains(query, ignoreCase = true) ||
                        client.accountNumber.contains(query, ignoreCase = true) ||
                        client.meterNumber.contains(query, ignoreCase = true)
            }
        }

        // Apply area filter
        _state.value.selectedArea?.let { area ->
            filtered = filtered.filter { it.area == area }
        }

        return filtered
    }

    /**
     * Refreshes the client data
     */
    fun refreshData() {
        _state.value = _state.value.copy(
            searchQuery = "",
            selectedArea = null,
            currentPage = 0
        )
        loadClients()
    }
}