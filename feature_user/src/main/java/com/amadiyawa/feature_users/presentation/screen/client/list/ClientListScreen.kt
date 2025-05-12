package com.amadiyawa.feature_users.presentation.screen.client.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.amadiyawa.feature_base.domain.util.UserStatus
import com.amadiyawa.feature_base.presentation.compose.composable.ButtonIconType
import com.amadiyawa.feature_base.presentation.compose.composable.CircularButton
import com.amadiyawa.feature_base.presentation.compose.composable.CircularButtonParams
import com.amadiyawa.feature_base.presentation.compose.composable.TextBodyMedium
import com.amadiyawa.feature_base.presentation.compose.composable.TextTitleLarge
import com.amadiyawa.feature_base.presentation.compose.composable.TextTitleMedium
import com.amadiyawa.feature_base.presentation.compose.composable.Toolbar
import com.amadiyawa.feature_base.presentation.compose.composable.ToolbarParams
import com.amadiyawa.feature_user.R
import com.amadiyawa.feature_users.domain.model.Client
import com.amadiyawa.feature_users.presentation.viewmodel.ClientListViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

/**
 * Client List Screen displaying all clients with search and filtering capabilities
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(
    viewModel: ClientListViewModel = koinViewModel(),
    onClientClick: (String) -> Unit,
    onAddClient: () -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val lazyListState = rememberLazyListState()

    // Filter dialog state
    var showFilterDialog by remember { mutableStateOf(false) }

    // Process side effects from ViewModel
    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ClientListContract.Effect.NavigateToClientDetail -> {
                    onClientClick(effect.clientId)
                }
                is ClientListContract.Effect.NavigateToCreateClient -> {
                    onAddClient()
                }
                is ClientListContract.Effect.NavigateBack -> {
                    onBackClick()
                }
                is ClientListContract.Effect.ShowError -> {
                    // Show error message (in a real app, would use Snackbar or Toast)
                    // For this implementation, we'll just use our error display
                }
            }
        }
    }

    // Pagination logic - load more when nearing the end of the list
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleItemIndex ->
                lastVisibleItemIndex?.let {
                    if (!state.isLoading && !state.isLastPage &&
                        it >= state.filteredClients.size - 5) {
                        viewModel.handleAction(ClientListContract.Action.LoadNextPage)
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            Toolbar(
                params = ToolbarParams(
                    title = stringResource(R.string.clients)
                ),
                actions = {
                    CircularButton(
                        params = CircularButtonParams(
                            iconType = ButtonIconType.Vector(Icons.Default.FilterList),
                            onClick = { showFilterDialog = true },
                            description = stringResource(R.string.filter),
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            iconTint = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            )
        },
        floatingActionButton = {
            if (state.canCreateClient) {
                FloatingActionButton(
                    onClick = {
                        viewModel.handleAction(ClientListContract.Action.CreateNewClient)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_client)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { query ->
                    viewModel.handleAction(ClientListContract.Action.Search(query))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Selected filter chips
            if (state.selectedArea != null) {
                FilterChip(
                    modifier = Modifier.padding(start = 16.dp),
                    selected = true,
                    onClick = {
                        viewModel.handleAction(ClientListContract.Action.SetAreaFilter(null))
                    },
                    label = { Text("Zone: ${state.selectedArea}") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Main content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    // Loading state
                    state.isLoading && state.filteredClients.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    // Error state
                    state.error != null && state.filteredClients.isEmpty() -> {
                        ErrorDisplay(
                            message = state.error ?: "",
                            onRetry = { viewModel.handleAction(ClientListContract.Action.RefreshData) }
                        )
                    }
                    // No results state
                    state.noResultsFound -> {
                        NoResultsDisplay(
                            searchQuery = state.searchQuery,
                            selectedArea = state.selectedArea
                        )
                    }
                    // Content state
                    else -> {
                        ClientList(
                            clients = state.filteredClients,
                            onClientClick = { clientId ->
                                viewModel.handleAction(ClientListContract.Action.SelectClient(clientId))
                            },
                            listState = lazyListState,
                            isLoadingMore = state.isLoading && state.filteredClients.isNotEmpty()
                        )
                    }
                }
            }
        }
    }

    // Filter dialog
    if (showFilterDialog) {
        FilterDialog(
            selectedArea = state.selectedArea,
            availableAreas = state.availableAreas,
            onAreaSelected = { area ->
                viewModel.handleAction(ClientListContract.Action.SetAreaFilter(area))
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

// UI Components
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_clients)) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null
            )
        },
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        singleLine = true
    )
}

@Composable
fun FilterDialog(
    selectedArea: String?,
    availableAreas: List<String>,
    onAreaSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_clients)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.select_area),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    item {
                        FilterOption(
                            name = stringResource(R.string.all_areas),
                            isSelected = selectedArea == null,
                            onClick = { onAreaSelected(null) }
                        )
                    }

                    items(availableAreas) { area ->
                        FilterOption(
                            name = area,
                            isSelected = area == selectedArea,
                            onClick = { onAreaSelected(area) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun FilterOption(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = name
        )
    }
}

@Composable
fun ClientList(
    clients: List<Client>,
    onClientClick: (String) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    isLoadingMore: Boolean
) {
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = clients,
            key = { it.id }
        ) { client ->
            ClientCard(
                client = client,
                onClick = { onClientClick(client.id) }
            )
        }

        // Loading indicator at the bottom when loading more
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientCard(
    client: Client,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar or icon
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Client info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = client.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = client.area,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Compte: ${client.accountNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status indicator
            StatusBadge(status = client.status)
        }
    }
}

@Composable
fun StatusBadge(status: UserStatus) {
    val color = when (status) {
        UserStatus.ACTIVE -> MaterialTheme.colorScheme.primary
        UserStatus.PENDING_VERIFICATION -> MaterialTheme.colorScheme.tertiary
        UserStatus.SUSPENDED -> MaterialTheme.colorScheme.error
        UserStatus.INACTIVE -> MaterialTheme.colorScheme.outline
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = when(status) {
                UserStatus.ACTIVE -> stringResource(R.string.active)
                UserStatus.PENDING_VERIFICATION -> stringResource(R.string.pending)
                UserStatus.SUSPENDED -> stringResource(R.string.suspended)
                UserStatus.INACTIVE -> stringResource(R.string.inactive)
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun ErrorDisplay(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextTitleMedium(
            text = stringResource(R.string.error_loading_clients),
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextBodyMedium(
            text = message
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
fun NoResultsDisplay(
    searchQuery: String,
    selectedArea: String?
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextTitleLarge(
            text = stringResource(R.string.no_clients_found)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (searchQuery.isNotEmpty() || selectedArea != null) {
            TextBodyMedium(
                text = stringResource(R.string.try_adjusting_filters)
            )
        } else {
            TextBodyMedium(
                text = stringResource(R.string.no_clients_in_system)
            )
        }
    }
}