package com.amadiyawa.feature_notification.domain.model

import java.time.LocalDateTime

data class NotificationFilter(
    val isRead: Boolean? = null,
    val type: NotificationType? = null,
    val priority: NotificationPriority? = null,
    val fromDate: LocalDateTime? = null,
    val toDate: LocalDateTime? = null
)