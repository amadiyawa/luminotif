package com.amadiyawa.feature_notification

import com.amadiyawa.feature_notification.data.dataModule
import com.amadiyawa.feature_notification.domain.domainModule
import com.amadiyawa.feature_notification.presentation.presentationModule

val featureNotificationModule = listOf(
    dataModule,
    domainModule,
    presentationModule
)