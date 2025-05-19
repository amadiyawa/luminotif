package com.amadiyawa.feature_notification.domain.usecase

import com.amadiyawa.feature_notification.domain.model.Notification
import com.amadiyawa.feature_notification.domain.model.NotificationPriority
import com.amadiyawa.feature_notification.domain.model.NotificationType
import com.amadiyawa.feature_notification.domain.repository.NotificationRepository
import java.time.LocalDateTime
import java.util.UUID

class CreateNotificationUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(
        userId: String,
        type: NotificationType,
        title: String,
        message: String,
        details: String? = null,
        priority: NotificationPriority = NotificationPriority.NORMAL,
        expiresAt: LocalDateTime? = null,
        actionData: Map<String, String>? = null
    ): Notification {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            userId = userId,
            type = type,
            title = title,
            message = message,
            details = details,
            priority = priority,
            isRead = false,
            createdAt = LocalDateTime.now(),
            readAt = null,
            expiresAt = expiresAt,
            actionData = actionData
        )

        return repository.createNotification(notification)
    }
}