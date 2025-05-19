package com.amadiyawa.feature_notification.domain.usecase

import com.amadiyawa.feature_notification.domain.model.Notification
import com.amadiyawa.feature_notification.domain.model.NotificationAction

class ProcessNotificationActionUseCase {
    operator fun invoke(notification: Notification): NotificationAction {
        return when {
            notification.actionData == null -> NotificationAction.None

            notification.actionData["action"] == "view_bill" -> {
                val billId = notification.actionData["bill_id"] ?: ""
                if (billId.isNotEmpty()) NotificationAction.OpenBill(billId) else NotificationAction.None
            }

            notification.actionData["action"] == "view_request" -> {
                val requestId = notification.actionData["request_id"] ?: ""
                if (requestId.isNotEmpty()) NotificationAction.OpenServiceRequest(requestId) else NotificationAction.None
            }

            notification.actionData["action"] == "top_up" -> {
                NotificationAction.OpenPayment
            }

            notification.actionData["action"] == "view_consumption_tips" -> {
                NotificationAction.OpenConsumptionTips
            }

            notification.actionData["action"] == "call_support" -> {
                val phoneNumber = notification.actionData["phone"] ?: ""
                if (phoneNumber.isNotEmpty()) NotificationAction.CallPhone(phoneNumber) else NotificationAction.None
            }

            notification.actionData["action"] == "open_url" -> {
                val url = notification.actionData["url"] ?: ""
                if (url.isNotEmpty()) NotificationAction.OpenUrl(url) else NotificationAction.None
            }

            else -> {
                val actionType = notification.actionData["action"] ?: "unknown"
                NotificationAction.Custom(actionType, notification.actionData)
            }
        }
    }
}