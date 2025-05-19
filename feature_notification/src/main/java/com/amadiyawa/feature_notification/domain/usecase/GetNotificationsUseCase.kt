package com.amadiyawa.feature_notification.domain.usecase

import com.amadiyawa.feature_notification.domain.model.Notification
import com.amadiyawa.feature_notification.domain.model.NotificationFilter
import com.amadiyawa.feature_notification.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationsUseCase(
    private val repository: NotificationRepository
) {
    fun getByUser(userId: String): Flow<List<Notification>> {
        return repository.getNotificationsByUser(userId)
    }

    fun getUnreadByUser(userId: String): Flow<List<Notification>> {
        return repository.getUnreadNotificationsByUser(userId)
    }

    fun getFilteredByUser(userId: String, filter: NotificationFilter): Flow<List<Notification>> {
        return repository.getNotificationsByUser(userId, filter)
    }
}