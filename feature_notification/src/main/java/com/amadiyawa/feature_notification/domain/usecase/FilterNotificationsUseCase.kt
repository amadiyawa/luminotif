package com.amadiyawa.feature_notification.domain.usecase

import com.amadiyawa.feature_notification.domain.model.Notification
import com.amadiyawa.feature_notification.domain.model.NotificationFilter
import com.amadiyawa.feature_notification.domain.model.NotificationPriority
import com.amadiyawa.feature_notification.domain.model.NotificationType
import com.amadiyawa.feature_notification.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class FilterNotificationsUseCase(
    private val repository: NotificationRepository
) {
    operator fun invoke(
        userId: String,
        isRead: Boolean? = null,
        type: NotificationType? = null,
        priority: NotificationPriority? = null,
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null
    ): Flow<List<Notification>> {
        val filter = NotificationFilter(
            isRead = isRead,
            type = type,
            priority = priority,
            fromDate = fromDate,
            toDate = toDate
        )
        return repository.getNotificationsByUser(userId, filter)
    }
}