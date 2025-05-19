package com.amadiyawa.feature_notification.data

import com.amadiyawa.feature_notification.data.repository.FakeNotificationRepository
import com.amadiyawa.feature_notification.domain.repository.NotificationRepository
import org.koin.dsl.module

internal val dataModule = module {
    single<NotificationRepository> {
        FakeNotificationRepository(
            userSessionManager = get()
        )
    }
}