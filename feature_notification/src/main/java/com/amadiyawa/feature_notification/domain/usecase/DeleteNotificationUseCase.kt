package com.amadiyawa.feature_notification.domain.usecase

import com.amadiyawa.feature_notification.domain.repository.NotificationRepository

class DeleteNotificationUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String) {
        repository.deleteNotification(notificationId)
    }
}