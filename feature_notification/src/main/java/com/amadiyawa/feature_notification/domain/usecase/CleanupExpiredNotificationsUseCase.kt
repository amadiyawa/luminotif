package com.amadiyawa.feature_notification.domain.usecase

import com.amadiyawa.feature_notification.domain.repository.NotificationRepository

class CleanupExpiredNotificationsUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke() {
        repository.deleteExpiredNotifications()
    }
}