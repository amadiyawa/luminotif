package com.amadiyawa.feature_notification.presentation.screen.notificationlist

import com.amadiyawa.feature_notification.domain.model.Notification
import com.amadiyawa.feature_notification.domain.model.NotificationPriority
import com.amadiyawa.feature_notification.domain.model.NotificationType

/**
 * Contract for Notification screen to manage state, actions and effects
 */
object NotificationContract {

    /**
     * Represents the UI state for the Notification screen
     */
    data class State(
        // Notification data
        val notifications: List<Notification> = emptyList(),
        val unreadCount: Int = 0,
        val expandedNotificationId: String? = null,

        // Screen state
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val isEmpty: Boolean = false,
        val error: String? = null,

        // Filter state
        val showFilterSheet: Boolean = false,
        val filterReadStatus: Boolean? = null, // null = all, true = read, false = unread
        val filterType: NotificationType? = null,
        val filterPriority: NotificationPriority? = null,
        val hasActiveFilters: Boolean = false,

        // Bulk actions state
        val showBulkActions: Boolean = false,
        val selectedNotifications: Set<String> = emptySet(),
        val showDeleteConfirmDialog: Boolean = false,
        val showMarkAllAsReadDialog: Boolean = false,
        val showDeleteAllDialog: Boolean = false,

        // Action state
        val processingActions: Set<String> = emptySet() // IDs of notifications being processed
    )

    /**
     * Actions that can be triggered from the UI
     */
    sealed class Action {
        // Data loading actions
        object LoadNotifications : Action()
        object RefreshNotifications : Action()

        // Notification interaction actions
        data class ExpandNotification(val notificationId: String) : Action()
        object CollapseNotification : Action()
        data class MarkAsRead(val notificationId: String) : Action()
        data class DeleteNotification(val notificationId: String) : Action()
        data class TapNotification(val notification: Notification) : Action()

        // Filter actions
        object ShowFilterSheet : Action()
        object HideFilterSheet : Action()
        data class FilterByReadStatus(val isRead: Boolean?) : Action()
        data class FilterByType(val type: NotificationType?) : Action()
        data class FilterByPriority(val priority: NotificationPriority?) : Action()
        object ClearFilters : Action()

        // Bulk actions
        object ShowBulkActions : Action()
        object HideBulkActions : Action()
        data class ToggleNotificationSelection(val notificationId: String) : Action()
        object SelectAllNotifications : Action()
        object ClearNotificationSelection : Action()

        // Bulk operations
        object ShowMarkAllAsReadDialog : Action()
        object HideMarkAllAsReadDialog : Action()
        object ConfirmMarkAllAsRead : Action()

        object ShowDeleteAllDialog : Action()
        object HideDeleteAllDialog : Action()
        object ConfirmDeleteAll : Action()

        // Delete confirmation
        data class ShowDeleteConfirmDialog(val notificationId: String) : Action()
        object HideDeleteConfirmDialog : Action()
        object ConfirmDelete : Action()

        // Error handling
        object ClearError : Action()
    }

    /**
     * Side effects triggered by the ViewModel
     */
    sealed class Effect {
        data class ShowSnackbar(val message: String) : Effect()
        data class ShowError(val message: String) : Effect()
        data class NavigateToServiceRequest(val requestId: String) : Effect()
        data class NavigateToBill(val billId: String) : Effect()
        data class OpenUrl(val url: String) : Effect()
        data class CallPhone(val phoneNumber: String) : Effect()
        object OpenPayment : Effect()
        object OpenConsumptionTips : Effect()
        data class ProcessCustomAction(val actionType: String, val data: Map<String, String>) : Effect()
    }
}