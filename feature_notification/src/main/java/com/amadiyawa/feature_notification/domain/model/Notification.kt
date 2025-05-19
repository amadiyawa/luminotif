package com.amadiyawa.feature_notification.domain.model

import com.amadiyawa.feature_base.domain.util.UserRole
import java.time.LocalDateTime

data class Notification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val details: String? = null,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val isRead: Boolean = false,
    val createdAt: LocalDateTime,
    val readAt: LocalDateTime? = null,
    val expiresAt: LocalDateTime? = null,
    val actionData: Map<String, String>? = null
)

enum class NotificationType(val targetRoles: Set<UserRole>) {
    // CLIENT notifications
    REMAINING_BALANCE(setOf(UserRole.CLIENT)),
    PLANNED_OUTAGE(setOf(UserRole.CLIENT)),
    SERVICE_RESTORED(setOf(UserRole.CLIENT)),
    CONSUMPTION_TIPS(setOf(UserRole.CLIENT)),
    OVERCONSUMPTION_ALERT(setOf(UserRole.CLIENT)),
    BILL_GENERATED(setOf(UserRole.CLIENT)),
    PAYMENT_RECEIVED(setOf(UserRole.CLIENT)),
    SERVICE_REQUEST_UPDATE(setOf(UserRole.CLIENT)),

    // AGENT notifications
    NEW_SERVICE_REQUEST(setOf(UserRole.AGENT)),
    URGENT_REQUEST(setOf(UserRole.AGENT)),
    WORK_SCHEDULE_UPDATE(setOf(UserRole.AGENT)),
    TERRITORY_CHANGE(setOf(UserRole.AGENT)),
    METER_READING_REMINDER(setOf(UserRole.AGENT)),
    CLIENT_FEEDBACK(setOf(UserRole.AGENT)),
    PERFORMANCE_REPORT(setOf(UserRole.AGENT)),

    // ADMIN notifications
    SYSTEM_ALERT(setOf(UserRole.ADMIN)),
    UNRESOLVED_REQUESTS(setOf(UserRole.ADMIN)),
    AGENT_PERFORMANCE(setOf(UserRole.ADMIN)),
    REVENUE_REPORT(setOf(UserRole.ADMIN)),
    MAINTENANCE_SCHEDULED(setOf(UserRole.ADMIN)),
    NEW_USER_REGISTERED(setOf(UserRole.ADMIN)),
    PAYMENT_ISSUES(setOf(UserRole.ADMIN)),
    CAPACITY_WARNING(setOf(UserRole.ADMIN)),

    // Notifications that can be sent to multiple roles
    EMERGENCY_ALERT(setOf(UserRole.CLIENT, UserRole.AGENT, UserRole.ADMIN)),
    SYSTEM_MAINTENANCE(setOf(UserRole.CLIENT, UserRole.AGENT, UserRole.ADMIN))
}

enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

enum class NotificationStatus {
    UNREAD,
    READ,
    ARCHIVED,
    DELETED
}