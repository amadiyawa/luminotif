package com.amadiyawa.feature_notification.domain.repository

import com.amadiyawa.feature_notification.domain.model.Notification
import com.amadiyawa.feature_notification.domain.model.NotificationFilter
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    // CRUD operations
    suspend fun createNotification(notification: Notification): Notification
    suspend fun getNotificationById(id: String): Notification?
    suspend fun updateNotification(notification: Notification): Notification
    suspend fun deleteNotification(id: String)

    // Query operations
    fun getNotificationsByUser(userId: String): Flow<List<Notification>>
    fun getUnreadNotificationsByUser(userId: String): Flow<List<Notification>>
    fun getNotificationsByUser(userId: String, filter: NotificationFilter): Flow<List<Notification>>

    // Status operations
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead(userId: String)
    suspend fun deleteAllForUser(userId: String)

    // Utility operations
    suspend fun getUnreadCount(userId: String): Int
    suspend fun deleteExpiredNotifications()
}