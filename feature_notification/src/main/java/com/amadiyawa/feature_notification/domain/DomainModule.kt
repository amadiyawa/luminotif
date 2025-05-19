package com.amadiyawa.feature_notification.domain

import com.amadiyawa.feature_notification.domain.usecase.CleanupExpiredNotificationsUseCase
import com.amadiyawa.feature_notification.domain.usecase.CreateNotificationUseCase
import com.amadiyawa.feature_notification.domain.usecase.DeleteAllNotificationsUseCase
import com.amadiyawa.feature_notification.domain.usecase.DeleteNotificationUseCase
import com.amadiyawa.feature_notification.domain.usecase.FilterNotificationsUseCase
import com.amadiyawa.feature_notification.domain.usecase.GetNotificationByIdUseCase
import com.amadiyawa.feature_notification.domain.usecase.GetNotificationsUseCase
import com.amadiyawa.feature_notification.domain.usecase.GetUnreadNotificationCountUseCase
import com.amadiyawa.feature_notification.domain.usecase.MarkAllNotificationsAsReadUseCase
import com.amadiyawa.feature_notification.domain.usecase.MarkNotificationAsReadUseCase
import com.amadiyawa.feature_notification.domain.usecase.ProcessNotificationActionUseCase
import org.koin.dsl.module

internal val domainModule = module {
    factory { GetNotificationsUseCase(get()) }
    factory { GetNotificationByIdUseCase(get()) }
    factory { MarkNotificationAsReadUseCase(get()) }
    factory { MarkAllNotificationsAsReadUseCase(get()) }
    factory { DeleteNotificationUseCase(get()) }
    factory { DeleteAllNotificationsUseCase(get()) }
    factory { GetUnreadNotificationCountUseCase(get()) }
    factory { CreateNotificationUseCase(get()) }
    factory { FilterNotificationsUseCase(get()) }
    factory { ProcessNotificationActionUseCase() }
    factory { CleanupExpiredNotificationsUseCase(get()) }
}