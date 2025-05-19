package com.amadiyawa.feature_notification.presentation.screen.notificationlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amadiyawa.feature_base.domain.model.UserSessionManager
import com.amadiyawa.feature_notification.domain.model.NotificationAction
import com.amadiyawa.feature_notification.domain.usecase.DeleteAllNotificationsUseCase
import com.amadiyawa.feature_notification.domain.usecase.DeleteNotificationUseCase
import com.amadiyawa.feature_notification.domain.usecase.FilterNotificationsUseCase
import com.amadiyawa.feature_notification.domain.usecase.GetNotificationsUseCase
import com.amadiyawa.feature_notification.domain.usecase.GetUnreadNotificationCountUseCase
import com.amadiyawa.feature_notification.domain.usecase.MarkAllNotificationsAsReadUseCase
import com.amadiyawa.feature_notification.domain.usecase.MarkNotificationAsReadUseCase
import com.amadiyawa.feature_notification.domain.usecase.ProcessNotificationActionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val getNotifications: GetNotificationsUseCase,
    private val getUnreadCount: GetUnreadNotificationCountUseCase,
    private val markAsRead: MarkNotificationAsReadUseCase,
    private val markAllAsRead: MarkAllNotificationsAsReadUseCase,
    private val deleteNotification: DeleteNotificationUseCase,
    private val deleteAllNotifications: DeleteAllNotificationsUseCase,
    private val filterNotifications: FilterNotificationsUseCase,
    private val processNotificationAction: ProcessNotificationActionUseCase,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationContract.State())
    val state: StateFlow<NotificationContract.State> = _state.asStateFlow()

    private val _effects = Channel<NotificationContract.Effect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var deletingNotificationId: String? = null

    init {
        viewModelScope.launch {
            // Observe user session changes
            userSessionManager.currentUserId.collect { userId ->
                if (userId != null) {
                    loadNotifications()
                    loadUnreadCount()
                } else {
                    // Clear notifications when user logs out
                    _state.update {
                        NotificationContract.State()
                    }
                }
            }
        }
    }

    fun onAction(action: NotificationContract.Action) {
        when (action) {
            // Data loading actions
            is NotificationContract.Action.LoadNotifications -> loadNotifications()
            is NotificationContract.Action.RefreshNotifications -> refreshNotifications()

            // Notification interaction actions
            is NotificationContract.Action.ExpandNotification -> expandNotification(action.notificationId)
            is NotificationContract.Action.CollapseNotification -> collapseNotification()
            is NotificationContract.Action.MarkAsRead -> markNotificationAsRead(action.notificationId)
            is NotificationContract.Action.DeleteNotification -> deleteNotificationAction(action.notificationId)
            is NotificationContract.Action.TapNotification -> handleNotificationTap(action.notification)

            // Filter actions
            is NotificationContract.Action.ShowFilterSheet -> showFilterSheet()
            is NotificationContract.Action.HideFilterSheet -> hideFilterSheet()
            is NotificationContract.Action.FilterByReadStatus -> filterByReadStatus(action.isRead)
            is NotificationContract.Action.FilterByType -> filterByType(action.type)
            is NotificationContract.Action.FilterByPriority -> filterByPriority(action.priority)
            is NotificationContract.Action.ClearFilters -> clearFilters()

            // Bulk actions
            is NotificationContract.Action.ShowBulkActions -> showBulkActions()
            is NotificationContract.Action.HideBulkActions -> hideBulkActions()
            is NotificationContract.Action.ToggleNotificationSelection -> toggleNotificationSelection(action.notificationId)
            is NotificationContract.Action.SelectAllNotifications -> selectAllNotifications()
            is NotificationContract.Action.ClearNotificationSelection -> clearNotificationSelection()

            // Bulk operations
            is NotificationContract.Action.ShowMarkAllAsReadDialog -> showMarkAllAsReadDialog()
            is NotificationContract.Action.HideMarkAllAsReadDialog -> hideMarkAllAsReadDialog()
            is NotificationContract.Action.ConfirmMarkAllAsRead -> confirmMarkAllAsRead()
            is NotificationContract.Action.ShowDeleteAllDialog -> showDeleteAllDialog()
            is NotificationContract.Action.HideDeleteAllDialog -> hideDeleteAllDialog()
            is NotificationContract.Action.ConfirmDeleteAll -> confirmDeleteAll()

            // Delete confirmation
            is NotificationContract.Action.ShowDeleteConfirmDialog -> showDeleteConfirmDialog(action.notificationId)
            is NotificationContract.Action.HideDeleteConfirmDialog -> hideDeleteConfirmDialog()
            is NotificationContract.Action.ConfirmDelete -> confirmDelete()

            // Error handling
            is NotificationContract.Action.ClearError -> clearError()
        }
    }

    private fun loadNotifications() {
        val userId = userSessionManager.currentUserId.value ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val currentState = _state.value

                if (currentState.hasActiveFilters) {
                    filterNotifications(
                        userId = userId,
                        isRead = currentState.filterReadStatus,
                        type = currentState.filterType,
                        priority = currentState.filterPriority
                    ).collect { notifications ->
                        _state.update { state ->
                            state.copy(
                                notifications = notifications,
                                isLoading = false,
                                isEmpty = notifications.isEmpty(),
                                error = null
                            )
                        }
                    }
                } else {
                    getNotifications.getByUser(userId).collect { notifications ->
                        _state.update { state ->
                            state.copy(
                                notifications = notifications,
                                isLoading = false,
                                isEmpty = notifications.isEmpty(),
                                error = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load notifications"
                    )
                }
            }
        }
    }

    private fun refreshNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            loadNotifications()
            loadUnreadCount()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun loadUnreadCount() {
        val userId = userSessionManager.currentUserId.value ?: return

        viewModelScope.launch {
            try {
                val count = getUnreadCount(userId)
                _state.update { it.copy(unreadCount = count) }
            } catch (e: Exception) {
                // Silently fail for unread count
            }
        }
    }

    private fun expandNotification(notificationId: String) {
        _state.update {
            it.copy(expandedNotificationId = notificationId)
        }

        // Auto-mark as read when expanded
        markNotificationAsRead(notificationId)
    }

    private fun collapseNotification() {
        _state.update {
            it.copy(expandedNotificationId = null)
        }
    }

    private fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                markAsRead(notificationId)
                // Update the local state
                _state.update { state ->
                    state.copy(
                        notifications = state.notifications.map { notification ->
                            if (notification.id == notificationId) {
                                notification.copy(isRead = true)
                            } else {
                                notification
                            }
                        }
                    )
                }
                loadUnreadCount()
            } catch (e: Exception) {
                _effects.send(NotificationContract.Effect.ShowError("Failed to mark as read"))
            }
        }
    }

    private fun deleteNotificationAction(notificationId: String) {
        _state.update {
            it.copy(showDeleteConfirmDialog = true)
        }
        deletingNotificationId = notificationId
    }

    private fun handleNotificationTap(notification: com.amadiyawa.feature_notification.domain.model.Notification) {
        viewModelScope.launch {
            // Mark as read if not already
            if (!notification.isRead) {
                markNotificationAsRead(notification.id)
            }

            // Process the action
            val action = processNotificationAction(notification)
            when (action) {
                is NotificationAction.None -> {
                    // Just expand the notification
                    expandNotification(notification.id)
                }
                is NotificationAction.OpenServiceRequest -> {
                    _effects.send(NotificationContract.Effect.NavigateToServiceRequest(action.requestId))
                }
                is NotificationAction.OpenBill -> {
                    _effects.send(NotificationContract.Effect.NavigateToBill(action.billId))
                }
                is NotificationAction.OpenUrl -> {
                    _effects.send(NotificationContract.Effect.OpenUrl(action.url))
                }
                is NotificationAction.CallPhone -> {
                    _effects.send(NotificationContract.Effect.CallPhone(action.phoneNumber))
                }
                is NotificationAction.OpenPayment -> {
                    _effects.send(NotificationContract.Effect.OpenPayment)
                }
                is NotificationAction.OpenConsumptionTips -> {
                    _effects.send(NotificationContract.Effect.OpenConsumptionTips)
                }
                is NotificationAction.Custom -> {
                    _effects.send(NotificationContract.Effect.ProcessCustomAction(action.type, action.data))
                }
            }
        }
    }

    // Filter methods
    private fun showFilterSheet() {
        _state.update { it.copy(showFilterSheet = true) }
    }

    private fun hideFilterSheet() {
        _state.update { it.copy(showFilterSheet = false) }
    }

    private fun filterByReadStatus(isRead: Boolean?) {
        _state.update {
            it.copy(
                filterReadStatus = isRead,
                hasActiveFilters = isRead != null || it.filterType != null || it.filterPriority != null
            )
        }
        loadNotifications()
    }

    private fun filterByType(type: com.amadiyawa.feature_notification.domain.model.NotificationType?) {
        _state.update {
            it.copy(
                filterType = type,
                hasActiveFilters = it.filterReadStatus != null || type != null || it.filterPriority != null
            )
        }
        loadNotifications()
    }

    private fun filterByPriority(priority: com.amadiyawa.feature_notification.domain.model.NotificationPriority?) {
        _state.update {
            it.copy(
                filterPriority = priority,
                hasActiveFilters = it.filterReadStatus != null || it.filterType != null || priority != null
            )
        }
        loadNotifications()
    }

    private fun clearFilters() {
        _state.update {
            it.copy(
                filterReadStatus = null,
                filterType = null,
                filterPriority = null,
                hasActiveFilters = false
            )
        }
        loadNotifications()
    }

    // Bulk actions
    private fun showBulkActions() {
        _state.update { it.copy(showBulkActions = true) }
    }

    private fun hideBulkActions() {
        _state.update {
            it.copy(
                showBulkActions = false,
                selectedNotifications = emptySet()
            )
        }
    }

    private fun toggleNotificationSelection(notificationId: String) {
        _state.update { state ->
            val currentSelection = state.selectedNotifications
            val newSelection = if (notificationId in currentSelection) {
                currentSelection - notificationId
            } else {
                currentSelection + notificationId
            }
            state.copy(selectedNotifications = newSelection)
        }
    }

    private fun selectAllNotifications() {
        val allNotificationIds = _state.value.notifications.map { it.id }.toSet()
        _state.update { it.copy(selectedNotifications = allNotificationIds) }
    }

    private fun clearNotificationSelection() {
        _state.update { it.copy(selectedNotifications = emptySet()) }
    }

    // Bulk operations
    private fun showMarkAllAsReadDialog() {
        _state.update { it.copy(showMarkAllAsReadDialog = true) }
    }

    private fun hideMarkAllAsReadDialog() {
        _state.update { it.copy(showMarkAllAsReadDialog = false) }
    }

    private fun confirmMarkAllAsRead() {
        val userId = userSessionManager.currentUserId.value ?: return

        viewModelScope.launch {
            try {
                markAllAsRead(userId)
                _effects.send(NotificationContract.Effect.ShowSnackbar("All notifications marked as read"))
                _state.update {
                    it.copy(
                        showMarkAllAsReadDialog = false,
                        notifications = it.notifications.map { notif -> notif.copy(isRead = true) }
                    )
                }
                loadUnreadCount()
            } catch (e: Exception) {
                _effects.send(NotificationContract.Effect.ShowError("Failed to mark all as read"))
            }
        }
    }

    private fun showDeleteAllDialog() {
        _state.update { it.copy(showDeleteAllDialog = true) }
    }

    private fun hideDeleteAllDialog() {
        _state.update { it.copy(showDeleteAllDialog = false) }
    }

    private fun confirmDeleteAll() {
        val userId = userSessionManager.currentUserId.value ?: return

        viewModelScope.launch {
            try {
                deleteAllNotifications(userId)
                _effects.send(NotificationContract.Effect.ShowSnackbar("All notifications deleted"))
                _state.update {
                    it.copy(
                        showDeleteAllDialog = false,
                        notifications = emptyList(),
                        isEmpty = true
                    )
                }
                loadUnreadCount()
            } catch (e: Exception) {
                _effects.send(NotificationContract.Effect.ShowError("Failed to delete all notifications"))
            }
        }
    }

    // Delete confirmation
    private fun showDeleteConfirmDialog(notificationId: String) {
        _state.update { it.copy(showDeleteConfirmDialog = true) }
        deletingNotificationId = notificationId
    }

    private fun hideDeleteConfirmDialog() {
        _state.update { it.copy(showDeleteConfirmDialog = false) }
        deletingNotificationId = null
    }

    private fun confirmDelete() {
        val notificationId = deletingNotificationId ?: return

        viewModelScope.launch {
            try {
                deleteNotification(notificationId)
                _effects.send(NotificationContract.Effect.ShowSnackbar("Notification deleted"))
                _state.update { state ->
                    state.copy(
                        showDeleteConfirmDialog = false,
                        notifications = state.notifications.filterNot { it.id == notificationId }
                    )
                }
                loadUnreadCount()
            } catch (e: Exception) {
                _effects.send(NotificationContract.Effect.ShowError("Failed to delete notification"))
            } finally {
                deletingNotificationId = null
            }
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }
}