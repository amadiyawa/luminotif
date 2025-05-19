package com.amadiyawa.feature_notification.presentation.screen.notificationlist

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amadiyawa.feature_base.presentation.compose.composable.*
import com.amadiyawa.feature_base.presentation.theme.customColor
import com.amadiyawa.feature_notification.R
import com.amadiyawa.feature_notification.domain.model.*
import com.amadiyawa.feature_notification.presentation.component.NotificationFilterSheet
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(
    viewModel: NotificationViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Process side effects from ViewModel
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NotificationContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is NotificationContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is NotificationContract.Effect.NavigateToServiceRequest -> {
                    // Handle navigation to service request
                    // Navigate to service request detail
                }
                is NotificationContract.Effect.NavigateToBill -> {
                    // Handle navigation to bill
                    // Navigate to bill detail
                }
                is NotificationContract.Effect.OpenUrl -> {
                    // Handle opening URL
                    // Open URL in browser
                }
                is NotificationContract.Effect.CallPhone -> {
                    // Handle phone call
                    // Open dialer with phone number
                }
                is NotificationContract.Effect.OpenPayment -> {
                    // Handle opening payment screen
                    // Navigate to payment screen
                }
                is NotificationContract.Effect.OpenConsumptionTips -> {
                    // Handle opening consumption tips
                    // Navigate to consumption tips screen
                }
                is NotificationContract.Effect.ProcessCustomAction -> {
                    // Handle custom actions
                    // Process custom action based on type and data
                }
            }
        }
    }

    Scaffold(
        topBar = {
            NotificationTopBar(
                unreadCount = state.unreadCount,
                showBulkActions = state.showBulkActions,
                selectedCount = state.selectedNotifications.size,
                onFilterClick = { viewModel.onAction(NotificationContract.Action.ShowFilterSheet) },
                onBulkActionsClick = { viewModel.onAction(NotificationContract.Action.ShowBulkActions) },
                onMarkAllAsReadClick = { viewModel.onAction(NotificationContract.Action.ShowMarkAllAsReadDialog) },
                onDeleteAllClick = { viewModel.onAction(NotificationContract.Action.ShowDeleteAllDialog) },
                onCloseBulkActions = { viewModel.onAction(NotificationContract.Action.HideBulkActions) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.onAction(NotificationContract.Action.RefreshNotifications) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val currentState = state

            when {
                currentState.isLoading && currentState.notifications.isEmpty() -> {
                    LoadingAnimation(visible = true)
                }
                currentState.error != null && currentState.notifications.isEmpty() -> {
                    ErrorScreen(
                        error = currentState.error,
                        onRetry = { viewModel.onAction(NotificationContract.Action.LoadNotifications) }
                    )
                }
                currentState.isEmpty -> {
                    EmptyScreen(
                        title = stringResource(R.string.no_notifications_found),
                        message = stringResource(R.string.no_notifications_help)
                    )
                }
                else -> {
                    NotificationList(
                        notifications = currentState.notifications,
                        expandedNotificationId = currentState.expandedNotificationId,
                        showBulkActions = currentState.showBulkActions,
                        selectedNotifications = currentState.selectedNotifications,
                        onNotificationClick = { notification ->
                            viewModel.onAction(NotificationContract.Action.TapNotification(notification))
                        },
                        onExpandToggle = { notificationId ->
                            if (currentState.expandedNotificationId == notificationId) {
                                viewModel.onAction(NotificationContract.Action.CollapseNotification)
                            } else {
                                viewModel.onAction(NotificationContract.Action.ExpandNotification(notificationId))
                            }
                        },
                        onMarkAsRead = { notificationId ->
                            viewModel.onAction(NotificationContract.Action.MarkAsRead(notificationId))
                        },
                        onDelete = { notificationId ->
                            viewModel.onAction(NotificationContract.Action.ShowDeleteConfirmDialog(notificationId))
                        },
                        onSelectionToggle = { notificationId ->
                            viewModel.onAction(NotificationContract.Action.ToggleNotificationSelection(notificationId))
                        }
                    )
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (state.showFilterSheet) {
        NotificationFilterSheet(
            currentReadStatus = state.filterReadStatus,
            currentType = state.filterType,
            currentPriority = state.filterPriority,
            onReadStatusChange = { status ->
                viewModel.onAction(NotificationContract.Action.FilterByReadStatus(status))
            },
            onTypeChange = { type ->
                viewModel.onAction(NotificationContract.Action.FilterByType(type))
            },
            onPriorityChange = { priority ->
                viewModel.onAction(NotificationContract.Action.FilterByPriority(priority))
            },
            onClearFilters = {
                viewModel.onAction(NotificationContract.Action.ClearFilters)
            },
            onDismiss = {
                viewModel.onAction(NotificationContract.Action.HideFilterSheet)
            }
        )
    }

    // Confirmation Dialogs
    if (state.showDeleteConfirmDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.delete_notification),
            message = "Are you sure you want to delete this notification?",
            onConfirm = {
                viewModel.onAction(NotificationContract.Action.ConfirmDelete)
            },
            onDismiss = {
                viewModel.onAction(NotificationContract.Action.HideDeleteConfirmDialog)
            }
        )
    }

    if (state.showMarkAllAsReadDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.mark_all_as_read),
            message = "Mark all notifications as read?",
            onConfirm = {
                viewModel.onAction(NotificationContract.Action.ConfirmMarkAllAsRead)
            },
            onDismiss = {
                viewModel.onAction(NotificationContract.Action.HideMarkAllAsReadDialog)
            }
        )
    }

    if (state.showDeleteAllDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.delete_all),
            message = "Are you sure you want to delete all notifications? This action cannot be undone.",
            onConfirm = {
                viewModel.onAction(NotificationContract.Action.ConfirmDeleteAll)
            },
            onDismiss = {
                viewModel.onAction(NotificationContract.Action.HideDeleteAllDialog)
            }
        )
    }
}

@Composable
private fun NotificationTopBar(
    unreadCount: Int,
    showBulkActions: Boolean,
    selectedCount: Int,
    onFilterClick: () -> Unit,
    onBulkActionsClick: () -> Unit,
    onMarkAllAsReadClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    onCloseBulkActions: () -> Unit
) {
    if (showBulkActions) {
        // Bulk actions toolbar
        Toolbar(
            params = ToolbarParams(
                title = "$selectedCount selected",
                showBackButton = true,
                onBackPressed = onCloseBulkActions
            ),
            actions = {
                if (selectedCount > 0) {
                    CircularButton(
                        params = CircularButtonParams(
                            iconType = ButtonIconType.Vector(Icons.Default.DoneAll),
                            onClick = onMarkAllAsReadClick,
                            description = "Mark as read",
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            iconTint = MaterialTheme.colorScheme.primary
                        )
                    )
                    CircularButton(
                        params = CircularButtonParams(
                            iconType = ButtonIconType.Vector(Icons.Default.Delete),
                            onClick = onDeleteAllClick,
                            description = "Delete",
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            iconTint = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        )
    } else {
        // Regular toolbar
        Toolbar(
            params = ToolbarParams(
                title = buildString {
                    append(stringResource(R.string.notifications))
                    if (unreadCount > 0) {
                        append(" ($unreadCount)")
                    }
                },
                showBackButton = false
            ),
            actions = {
                CircularButton(
                    params = CircularButtonParams(
                        iconType = ButtonIconType.Vector(Icons.Default.FilterList),
                        onClick = onFilterClick,
                        description = "Filter",
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        iconTint = MaterialTheme.colorScheme.primary
                    )
                )
                CircularButton(
                    params = CircularButtonParams(
                        iconType = ButtonIconType.Vector(Icons.Default.MoreVert),
                        onClick = onBulkActionsClick,
                        description = "More actions",
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        iconTint = MaterialTheme.colorScheme.primary
                    )
                )
            }
        )
    }
}

@Composable
private fun NotificationList(
    notifications: List<Notification>,
    expandedNotificationId: String?,
    showBulkActions: Boolean,
    selectedNotifications: Set<String>,
    onNotificationClick: (Notification) -> Unit,
    onExpandToggle: (String) -> Unit,
    onMarkAsRead: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSelectionToggle: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = notifications,
            key = { it.id }
        ) { notification ->
            NotificationItem(
                notification = notification,
                isExpanded = expandedNotificationId == notification.id,
                showBulkActions = showBulkActions,
                isSelected = notification.id in selectedNotifications,
                onClick = { onNotificationClick(notification) },
                onExpandToggle = { onExpandToggle(notification.id) },
                onMarkAsRead = { onMarkAsRead(notification.id) },
                onDelete = { onDelete(notification.id) },
                onSelectionToggle = { onSelectionToggle(notification.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationItem(
    notification: Notification,
    isExpanded: Boolean,
    showBulkActions: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onExpandToggle: () -> Unit,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    onSelectionToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                !notification.isRead -> MaterialTheme.colorScheme.surfaceContainerHighest
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!notification.isRead) 2.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (showBulkActions) {
                            onSelectionToggle()
                        } else {
                            onClick()
                        }
                    }
                    .padding(16.dp)
            ) {
                // Header row with type icon, status indicators, and actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Type indicator + selection checkbox
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (showBulkActions) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onSelectionToggle() }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        NotificationTypeIcon(
                            type = notification.type,
                            priority = notification.priority,
                            isRead = notification.isRead
                        )
                    }

                    // Right side: Status indicators and actions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Priority indicator (if high/urgent)
                        if (notification.priority == NotificationPriority.URGENT) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PriorityHigh,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.priority_urgent),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        } else if (notification.priority == NotificationPriority.HIGH) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.customColor.warning
                            ) {
                                Text(
                                    text = stringResource(R.string.priority_high),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.customColor.onWarning
                                )
                            }
                        }

                        // Unread indicator
                        if (!notification.isRead) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = stringResource(R.string.new_notification),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        // Action menu (non-bulk mode only)
                        if (!showBulkActions) {
                            var showMenu by remember { mutableStateOf(false) }

                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More options"
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    if (!notification.isRead) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.mark_as_read)) },
                                            onClick = {
                                                onMarkAsRead()
                                                showMenu = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.MarkEmailRead,
                                                    contentDescription = null
                                                )
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.delete_notification)) },
                                        onClick = {
                                            onDelete()
                                            showMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title - prominently displayed
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.titleMedium.lineHeight
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Message preview - clearly separated
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Footer with timestamp and expand button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timestamp
                    Text(
                        text = notification.createdAt.format(
                            DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Expand/collapse button with text
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onExpandToggle() }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Show less" else "Show more",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp
                    )

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Detailed description
                        if (!notification.details.isNullOrBlank()) {
                            Text(
                                text = "Details",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = notification.details,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                            )
                        }

                        // Action button (if available)
                        if (notification.actionData != null) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = getActionIcon(notification.actionData),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(getActionButtonText(notification.actionData))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to get appropriate icon for action
private fun getActionIcon(actionData: Map<String, String>) = when (actionData["action"]) {
    "view_bill" -> Icons.Default.Receipt
    "view_request" -> Icons.Default.Assignment
    "top_up" -> Icons.Default.Payment
    "view_consumption_tips" -> Icons.Default.Lightbulb
    "call_support" -> Icons.Default.Phone
    else -> Icons.Default.OpenInNew
}

@Composable
private fun NotificationTypeIcon(
    type: NotificationType,
    priority: NotificationPriority,
    isRead: Boolean
) {
    val icon = when (type) {
        NotificationType.REMAINING_BALANCE -> Icons.Default.AccountBalance
        NotificationType.PLANNED_OUTAGE, NotificationType.SERVICE_RESTORED -> Icons.Default.PowerOff
        NotificationType.CONSUMPTION_TIPS -> Icons.Default.Lightbulb
        NotificationType.OVERCONSUMPTION_ALERT -> Icons.Default.Warning
        NotificationType.BILL_GENERATED -> Icons.Default.Receipt
        NotificationType.PAYMENT_RECEIVED -> Icons.Default.Payment
        NotificationType.SERVICE_REQUEST_UPDATE -> Icons.Default.Engineering
        NotificationType.NEW_SERVICE_REQUEST -> Icons.AutoMirrored.Filled.Assignment
        NotificationType.URGENT_REQUEST -> Icons.Default.Emergency
        NotificationType.WORK_SCHEDULE_UPDATE -> Icons.Default.Schedule
        NotificationType.METER_READING_REMINDER -> Icons.Default.Speed
        NotificationType.CLIENT_FEEDBACK -> Icons.Default.Feedback
        NotificationType.PERFORMANCE_REPORT -> Icons.Default.Assessment
        NotificationType.TERRITORY_CHANGE -> Icons.Default.Place
        NotificationType.SYSTEM_ALERT -> Icons.Default.Computer
        NotificationType.UNRESOLVED_REQUESTS -> Icons.Default.PendingActions
        NotificationType.AGENT_PERFORMANCE -> Icons.Default.Person
        NotificationType.REVENUE_REPORT -> Icons.AutoMirrored.Filled.TrendingUp
        NotificationType.MAINTENANCE_SCHEDULED -> Icons.Default.Build
        NotificationType.NEW_USER_REGISTERED -> Icons.Default.PersonAdd
        NotificationType.PAYMENT_ISSUES -> Icons.Default.ErrorOutline
        NotificationType.CAPACITY_WARNING -> Icons.Default.Storage
        NotificationType.EMERGENCY_ALERT, NotificationType.SYSTEM_MAINTENANCE -> Icons.Default.EmergencyShare
    }

    val containerColor = when (priority) {
        NotificationPriority.URGENT -> MaterialTheme.colorScheme.errorContainer
        NotificationPriority.HIGH -> MaterialTheme.customColor.warning
        NotificationPriority.NORMAL -> MaterialTheme.colorScheme.primaryContainer
        NotificationPriority.LOW -> MaterialTheme.colorScheme.surfaceContainer
    }

    val contentColor = when (priority) {
        NotificationPriority.URGENT -> MaterialTheme.colorScheme.onErrorContainer
        NotificationPriority.HIGH -> MaterialTheme.customColor.onWarning
        NotificationPriority.NORMAL -> MaterialTheme.colorScheme.onPrimaryContainer
        NotificationPriority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor.copy(alpha = if (isRead) 0.5f else 1f),
        modifier = Modifier.size(40.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor.copy(alpha = if (isRead) 0.5f else 1f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PriorityChip(priority: NotificationPriority) {
    if (priority == NotificationPriority.NORMAL) return

    val (text, color) = when (priority) {
        NotificationPriority.URGENT -> stringResource(R.string.priority_urgent) to MaterialTheme.colorScheme.error
        NotificationPriority.HIGH -> stringResource(R.string.priority_high) to MaterialTheme.customColor.warning
        NotificationPriority.LOW -> stringResource(R.string.low) to MaterialTheme.colorScheme.outline
        else -> "" to MaterialTheme.colorScheme.outline
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
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

private fun getActionButtonText(actionData: Map<String, String>): String {
    return when (actionData["action"]) {
        "view_bill" -> "View Bill"
        "view_request" -> "View Request"
        "top_up" -> "Top Up Account"
        "view_consumption_tips" -> "View Tips"
        "call_support" -> "Call Support"
        else -> "View Details"
    }
}