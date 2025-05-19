package com.amadiyawa.feature_notification.domain.usecase

import com.amadiyawa.feature_notification.domain.model.Notification
import com.amadiyawa.feature_notification.domain.repository.NotificationRepository

class GetNotificationByIdUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String): Notification? {
        return repository.getNotificationById(notificationId)
    }
}