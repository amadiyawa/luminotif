package com.amadiyawa.feature_notification.domain.usecase

import com.amadiyawa.feature_notification.domain.repository.NotificationRepository

class MarkAllNotificationsAsReadUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(userId: String) {
        repository.markAllAsRead(userId)
    }
}