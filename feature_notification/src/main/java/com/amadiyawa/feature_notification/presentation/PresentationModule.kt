package com.amadiyawa.feature_notification.presentation

import com.amadiyawa.feature_base.presentation.navigation.FeatureNavigationApi
import com.amadiyawa.feature_base.presentation.navigation.NavigationRegistry
import com.amadiyawa.feature_notification.presentation.navigation.NotificationNavigationApi
import com.amadiyawa.feature_notification.presentation.screen.notificationlist.NotificationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import timber.log.Timber

internal val presentationModule = module {
    // Log module loading
    Timber.d("Loading notification navigation module")

    // Register the navigation API as a FeatureNavigationApi
    single<FeatureNavigationApi>(named("notificationApi")) {
        NotificationNavigationApi().apply {
            Timber.d("Created NotificationNavigationApi instance")
        }
    }

    // Register with NavigationRegistry
    single(named("notificationRegistration")) {
        val registry = get<NavigationRegistry>()
        val api = get<FeatureNavigationApi>(named("notificationApi"))
        registry.registerFeature(api)
        Timber.d("Successfully registered NotificationNavigationApi with NavigationRegistry")
    }

    // Force registration initialization
    single(named("initNotificationFeature")) {
        get<Unit>(named("notificationRegistration"))
        Timber.d("Notification feature initialization completed")
    }

    viewModel {
        NotificationViewModel(
            getNotifications = get(),
            getUnreadCount = get(),
            markAsRead = get(),
            markAllAsRead = get(),
            deleteNotification = get(),
            deleteAllNotifications = get(),
            filterNotifications = get(),
            processNotificationAction = get(),
            userSessionManager = get()
        )
    }
}