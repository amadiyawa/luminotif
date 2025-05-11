package com.amadiyawa.feature_requests.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amadiyawa.feature_base.presentation.compose.composable.ErrorScreen
import com.amadiyawa.feature_base.presentation.compose.composable.LoadingAnimation
import com.amadiyawa.feature_base.presentation.compose.composable.TextTitleMedium
import com.amadiyawa.feature_base.presentation.compose.composable.Toolbar
import com.amadiyawa.feature_base.presentation.compose.composable.ToolbarParams
import com.amadiyawa.feature_requests.R
import com.amadiyawa.feature_requests.domain.model.RequestStatus
import com.amadiyawa.feature_requests.domain.model.RequestUpdate
import com.amadiyawa.feature_requests.domain.model.ServiceRequest
import com.amadiyawa.feature_requests.presentation.state.ServiceRequestDetailAction
import com.amadiyawa.feature_requests.presentation.state.ServiceRequestDetailEvent
import com.amadiyawa.feature_requests.presentation.state.ServiceRequestDetailUiState
import com.amadiyawa.feature_requests.presentation.viewmodel.ServiceRequestDetailViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceRequestDetailScreen(
    requestId: String,
    onBackClick: () -> Unit,
    viewModel: ServiceRequestDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val canUpdateStatus = remember { viewModel.canUpdateStatus() }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(requestId) {
        viewModel.onAction(ServiceRequestDetailAction.LoadRequestDetail(requestId))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ServiceRequestDetailEvent.NavigateBack -> onBackClick()
                is ServiceRequestDetailEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ServiceRequestDetailEvent.StatusUpdateSuccess -> {
                    snackbarHostState.showSnackbar("Status updated successfully")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Toolbar(
                params = ToolbarParams(
                    title = stringResource(R.string.request_details),
                    showBackButton = true,
                    onBackPressed = onBackClick
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val currentState = uiState

            when (currentState) {
                is ServiceRequestDetailUiState.Loading -> {
                    LoadingAnimation(visible = true)
                }
                is ServiceRequestDetailUiState.Error -> {
                    ErrorScreen(
                        error = currentState.message,  // Changed from uiState to currentState
                        onRetry = { viewModel.onAction(ServiceRequestDetailAction.LoadRequestDetail(requestId)) }
                    )
                }
                is ServiceRequestDetailUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            RequestDetailCard(
                                request = currentState.request,
                                onUpdateStatus = {
                                    if (canUpdateStatus) {
                                        viewModel.onAction(ServiceRequestDetailAction.ShowStatusUpdateDialog)
                                    }
                                },
                                onCancelRequest = {
                                    viewModel.onAction(ServiceRequestDetailAction.CancelRequest)
                                },
                                canUpdateStatus = canUpdateStatus
                            )
                        }

                        if (currentState.updates.isNotEmpty()) {
                            item {
                                TextTitleMedium(
                                    text = stringResource(R.string.request_history),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(
                                items = currentState.updates,
                                key = { it.id }
                            ) { update ->
                                RequestUpdateItem(update = update)
                            }
                        }
                    }

                    if (currentState.isUpdating) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    if (dialogState.isShowing) {
        StatusUpdateDialog(
            availableStatuses = dialogState.availableStatuses,
            selectedStatus = dialogState.selectedStatus,
            comment = dialogState.comment,
            onStatusSelect = { status ->
                viewModel.onAction(ServiceRequestDetailAction.SelectStatus(status))
            },
            onCommentChange = { comment ->
                viewModel.onAction(ServiceRequestDetailAction.UpdateComment(comment))
            },
            onDismiss = {
                viewModel.onAction(ServiceRequestDetailAction.HideStatusUpdateDialog)
            },
            onConfirm = {
                viewModel.onAction(ServiceRequestDetailAction.SubmitStatusUpdate)
            }
        )
    }
}

@Composable
fun RequestDetailCard(
    request: ServiceRequest,
    onUpdateStatus: () -> Unit,
    onCancelRequest: () -> Unit,
    canUpdateStatus: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = request.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status, Category, Priority Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(status = request.status)
                CategoryChip(category = request.category)
                PriorityChip(priority = request.priority)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = stringResource(R.string.description),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Request Details
            DetailRow(
                label = stringResource(R.string.request_id),
                value = request.id
            )
            DetailRow(
                label = stringResource(R.string.created_on),
                value = request.createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            )
            DetailRow(
                label = stringResource(R.string.updated_on),
                value = request.updatedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            )
            if (request.assignedAgentId != null) {
                DetailRow(
                    label = stringResource(R.string.assigned_agent),
                    value = request.assignedAgentId
                )
            }

            if (canUpdateStatus && request.status != RequestStatus.CLOSED && request.status != RequestStatus.CANCELLED) {
                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onUpdateStatus,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.update_status))
                    }

                    OutlinedButton(
                        onClick = onCancelRequest,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.cancel_request))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RequestUpdateItem(
    update: RequestUpdate,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = update.newStatus)
                Text(
                    text = update.timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = update.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "By: ${update.agentId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusUpdateDialog(
    availableStatuses: List<RequestStatus>,
    selectedStatus: RequestStatus?,
    comment: String,
    onStatusSelect: (RequestStatus) -> Unit,
    onCommentChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_status)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_new_status),
                    style = MaterialTheme.typography.bodyMedium
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableStatuses.forEach { status ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            RadioButton(
                                selected = selectedStatus == status,
                                onClick = { onStatusSelect(status) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = getStatusString(status),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    label = { Text(stringResource(R.string.add_comment)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text(stringResource(R.string.comment_placeholder)) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = selectedStatus != null && comment.isNotBlank()
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
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