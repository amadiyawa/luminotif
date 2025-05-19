package com.amadiyawa.feature_notification.domain.usecase

import com.amadiyawa.feature_notification.domain.repository.NotificationRepository

class GetUnreadNotificationCountUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(userId: String): Int {
        return repository.getUnreadCount(userId)
    }
}