package com.amadiyawa.feature_notification.domain.model

sealed class NotificationAction(val type: String) {
    object None : NotificationAction("none")
    data class OpenServiceRequest(val requestId: String) : NotificationAction("open_service_request")
    data class OpenBill(val billId: String) : NotificationAction("open_bill")
    data class OpenUrl(val url: String) : NotificationAction("open_url")
    data class CallPhone(val phoneNumber: String) : NotificationAction("call_phone")
    object OpenConsumptionTips : NotificationAction("open_consumption_tips")
    object OpenPayment : NotificationAction("open_payment")
    data class Custom(val actionType: String, val data: Map<String, String>) : NotificationAction(actionType)
}