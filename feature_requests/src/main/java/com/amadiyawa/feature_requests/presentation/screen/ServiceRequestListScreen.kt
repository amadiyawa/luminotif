package com.amadiyawa.feature_requests.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_base.presentation.compose.composable.ButtonIconType
import com.amadiyawa.feature_base.presentation.compose.composable.CircularButton
import com.amadiyawa.feature_base.presentation.compose.composable.CircularButtonParams
import com.amadiyawa.feature_base.presentation.compose.composable.EmptyScreen
import com.amadiyawa.feature_base.presentation.compose.composable.ErrorScreen
import com.amadiyawa.feature_base.presentation.compose.composable.LoadingAnimation
import com.amadiyawa.feature_base.presentation.compose.composable.TextBodyMedium
import com.amadiyawa.feature_base.presentation.compose.composable.TextBodySmall
import com.amadiyawa.feature_base.presentation.compose.composable.TextHeadlineSmall
import com.amadiyawa.feature_base.presentation.compose.composable.TextLabelSmall
import com.amadiyawa.feature_base.presentation.compose.composable.TextTitleMedium
import com.amadiyawa.feature_base.presentation.compose.composable.Toolbar
import com.amadiyawa.feature_base.presentation.compose.composable.ToolbarParams
import com.amadiyawa.feature_requests.R
import com.amadiyawa.feature_requests.domain.model.RequestCategory
import com.amadiyawa.feature_requests.domain.model.RequestPriority
import com.amadiyawa.feature_requests.domain.model.RequestStatus
import com.amadiyawa.feature_requests.domain.model.ServiceRequest
import com.amadiyawa.feature_requests.presentation.state.FilterOptions
import com.amadiyawa.feature_requests.presentation.state.ServiceRequestListAction
import com.amadiyawa.feature_requests.presentation.state.ServiceRequestListEvent
import com.amadiyawa.feature_requests.presentation.state.ServiceRequestListUiState
import com.amadiyawa.feature_requests.presentation.viewmodel.ServiceRequestListViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceRequestListScreen(
    onRequestClick: (String) -> Unit,
    onCreateClick: () -> Unit,
    viewModel: ServiceRequestListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterOptions by viewModel.filterOptions.collectAsStateWithLifecycle()
    val canCreateRequest = remember { viewModel.currentRole == UserRole.CLIENT }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }

    val isRefreshing = when (val state = uiState) {
        is ServiceRequestListUiState.Success -> state.isRefreshing
        else -> false
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ServiceRequestListEvent.NavigateToDetail -> onRequestClick(event.requestId)
                is ServiceRequestListEvent.NavigateToCreate -> onCreateClick()
                is ServiceRequestListEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Toolbar(
                params = ToolbarParams(
                    title = stringResource(R.string.service_requests),
                    showBackButton = false
                ),
                actions = {
                    CircularButton(
                        params = CircularButtonParams(
                            iconType = ButtonIconType.Vector(Icons.Default.FilterList),
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            iconTint = MaterialTheme.colorScheme.primary,
                            onClick = { showFilterSheet = true },
                            description = "Filter requests"
                        )
                    )
                }
            )
        },
        floatingActionButton = {
            if (canCreateRequest) {
                FloatingActionButton(
                    onClick = { viewModel.navigateToCreate() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Request"
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.onAction(ServiceRequestListAction.RefreshRequests) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val currentState = uiState

            when (currentState) {
                is ServiceRequestListUiState.Loading -> {
                    LoadingAnimation(visible = true)
                }

                is ServiceRequestListUiState.Error -> {
                    ErrorScreen(
                        error = currentState.message,  // Changed from uiState to currentState
                        onRetry = { viewModel.onAction(ServiceRequestListAction.LoadRequests) }
                    )
                }

                is ServiceRequestListUiState.Success -> {
                    if (currentState.requests.isEmpty()) {
                        EmptyScreen(
                            title = stringResource(R.string.no_requests_found),
                            message = stringResource(R.string.no_requests_help)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = currentState.requests,
                                key = { it.id }
                            ) { request ->
                                RequestListItem(
                                    request = request,
                                    onClick = { viewModel.navigateToDetail(request.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilters = filterOptions,
            onDismiss = { showFilterSheet = false },
            onApplyFilters = { status, category, priority ->
                viewModel.onAction(ServiceRequestListAction.FilterByStatus(status))
                viewModel.onAction(ServiceRequestListAction.FilterByCategory(category))
                viewModel.onAction(ServiceRequestListAction.FilterByPriority(priority))
                showFilterSheet = false
            },
            onClearFilters = {
                viewModel.onAction(ServiceRequestListAction.ClearFilters)
                showFilterSheet = false
            }
        )
    }
}

@Composable
fun RequestListItem(
    request: ServiceRequest,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextTitleMedium(
                    text = request.title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status = request.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextBodyMedium(
                text = request.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CategoryChip(category = request.category)
                PriorityChip(priority = request.priority)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextBodySmall(
                text = "${stringResource(R.string.created_on)} ${request.createdAt.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                )}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatusChip(status: RequestStatus) {
    val (backgroundColor, contentColor) = when (status) {
        RequestStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        RequestStatus.ASSIGNED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        RequestStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        RequestStatus.RESOLVED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        RequestStatus.CLOSED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        RequestStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        TextLabelSmall(
            text = getStatusString(status),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = contentColor
        )
    }
}

@Composable
fun CategoryChip(category: RequestCategory) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        TextLabelSmall(
            text = getCategoryString(category),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PriorityChip(priority: RequestPriority) {
    val (backgroundColor, contentColor) = when (priority) {
        RequestPriority.LOW -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        RequestPriority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        RequestPriority.HIGH -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        RequestPriority.URGENT -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        TextLabelSmall(
            text = getPriorityString(priority),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = contentColor
        )
    }
}

@Composable
private fun getStatusString(status: RequestStatus): String {
    return when (status) {
        RequestStatus.PENDING -> stringResource(R.string.pending)
        RequestStatus.ASSIGNED -> stringResource(R.string.assigned)
        RequestStatus.IN_PROGRESS -> stringResource(R.string.in_progress)
        RequestStatus.RESOLVED -> stringResource(R.string.resolved)
        RequestStatus.CLOSED -> stringResource(R.string.closed)
        RequestStatus.CANCELLED -> stringResource(R.string.cancelled)
    }
}

@Composable
private fun getCategoryString(category: RequestCategory): String {
    return when (category) {
        RequestCategory.CONNECTION_ISSUE -> stringResource(R.string.connection_issue)
        RequestCategory.BILLING_QUERY -> stringResource(R.string.billing_query)
        RequestCategory.METER_PROBLEM -> stringResource(R.string.meter_problem)
        RequestCategory.POWER_OUTAGE -> stringResource(R.string.power_outage)
        RequestCategory.OTHER -> stringResource(R.string.other)
    }
}

@Composable
private fun getPriorityString(priority: RequestPriority): String {
    return when (priority) {
        RequestPriority.LOW -> stringResource(R.string.low)
        RequestPriority.MEDIUM -> stringResource(R.string.medium)
        RequestPriority.HIGH -> stringResource(R.string.high)
        RequestPriority.URGENT -> stringResource(R.string.urgent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilters: FilterOptions,
    onDismiss: () -> Unit,
    onApplyFilters: (RequestStatus?, RequestCategory?, RequestPriority?) -> Unit,
    onClearFilters: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentFilters.selectedStatus) }
    var selectedCategory by remember { mutableStateOf(currentFilters.selectedCategory) }
    var selectedPriority by remember { mutableStateOf(currentFilters.selectedPriority) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextHeadlineSmall(
                text = "Filter Requests",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Status Filter
            TextTitleMedium(
                text = stringResource(R.string.status),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RequestStatus.values().forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = {
                            selectedStatus = if (selectedStatus == status) null else status
                        },
                        label = { Text(getStatusString(status)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Filter
            TextTitleMedium(
                text = stringResource(R.string.category),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RequestCategory.entries.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = if (selectedCategory == category) null else category
                        },
                        label = { Text(getCategoryString(category)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority Filter
            TextTitleMedium(
                text = stringResource(R.string.priority),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RequestPriority.entries.forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = {
                            selectedPriority = if (selectedPriority == priority) null else priority
                        },
                        label = { Text(getPriorityString(priority)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedStatus = null
                        selectedCategory = null
                        selectedPriority = null
                        onClearFilters()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }

                Button(
                    onClick = {
                        onApplyFilters(selectedStatus, selectedCategory, selectedPriority)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}