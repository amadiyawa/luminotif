package com.amadiyawa.feature_notification.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.amadiyawa.feature_notification.R
import com.amadiyawa.feature_notification.domain.model.NotificationPriority
import com.amadiyawa.feature_notification.domain.model.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationFilterSheet(
    currentReadStatus: Boolean?,
    currentType: NotificationType?,
    currentPriority: NotificationPriority?,
    onReadStatusChange: (Boolean?) -> Unit,
    onTypeChange: (NotificationType?) -> Unit,
    onPriorityChange: (NotificationPriority?) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Filter Notifications",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Read Status Filter
            Text(
                text = "Read Status",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterOptionItem(
                    text = "All",
                    isSelected = currentReadStatus == null,
                    onClick = { onReadStatusChange(null) }
                )
                FilterOptionItem(
                    text = stringResource(R.string.unread),
                    isSelected = currentReadStatus == false,
                    onClick = { onReadStatusChange(false) }
                )
                FilterOptionItem(
                    text = stringResource(R.string.read),
                    isSelected = currentReadStatus == true,
                    onClick = { onReadStatusChange(true) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Priority Filter
            Text(
                text = stringResource(R.string.priority),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = currentPriority == null,
                    onClick = { onPriorityChange(null) },
                    label = { Text("All") }
                )
                NotificationPriority.entries.forEach { priority ->
                    FilterChip(
                        selected = currentPriority == priority,
                        onClick = { onPriorityChange(priority) },
                        label = { Text(getPriorityString(priority)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Type Filter
            Text(
                text = "Notification Type",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = currentType == null,
                    onClick = { onTypeChange(null) },
                    label = { Text("All Types") }
                )
                // Group notification types by user role for better organization
                val clientTypes = NotificationType.entries.filter {
                    com.amadiyawa.feature_base.domain.util.UserRole.CLIENT in it.targetRoles
                }
                val agentTypes = NotificationType.entries.filter {
                    com.amadiyawa.feature_base.domain.util.UserRole.AGENT in it.targetRoles
                }
                val adminTypes = NotificationType.entries.filter {
                    com.amadiyawa.feature_base.domain.util.UserRole.ADMIN in it.targetRoles
                }

                // Show only relevant types based on current user's role
                // For now, we'll show all types
                NotificationType.entries.toTypedArray().take(10).forEach { type -> // Show first 10 to avoid overcrowding
                    FilterChip(
                        selected = currentType == type,
                        onClick = { onTypeChange(type) },
                        label = { Text(getTypeString(type)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClearFilters,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FilterOptionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(
            onClick = onClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun getPriorityString(priority: NotificationPriority): String {
    return when (priority) {
        NotificationPriority.LOW -> stringResource(R.string.low)
        NotificationPriority.NORMAL -> "Normal"
        NotificationPriority.HIGH -> stringResource(R.string.priority_high)
        NotificationPriority.URGENT -> stringResource(R.string.priority_urgent)
    }
}

@Composable
private fun getTypeString(type: NotificationType): String {
    return when (type) {
        NotificationType.REMAINING_BALANCE -> stringResource(R.string.remaining_balance)
        NotificationType.PLANNED_OUTAGE -> stringResource(R.string.planned_outage)
        NotificationType.SERVICE_RESTORED -> stringResource(R.string.service_restored)
        NotificationType.CONSUMPTION_TIPS -> stringResource(R.string.consumption_tips)
        NotificationType.OVERCONSUMPTION_ALERT -> stringResource(R.string.overconsumption_alert)
        NotificationType.BILL_GENERATED -> stringResource(R.string.bill_generated)
        NotificationType.PAYMENT_RECEIVED -> stringResource(R.string.payment_received)
        NotificationType.SERVICE_REQUEST_UPDATE -> stringResource(R.string.service_request_update)
        NotificationType.NEW_SERVICE_REQUEST -> stringResource(R.string.new_service_request)
        NotificationType.URGENT_REQUEST -> stringResource(R.string.urgent_request)
        NotificationType.WORK_SCHEDULE_UPDATE -> stringResource(R.string.work_schedule_update)
        NotificationType.TERRITORY_CHANGE -> stringResource(R.string.territory_change)
        NotificationType.METER_READING_REMINDER -> stringResource(R.string.meter_reading_reminder)
        NotificationType.CLIENT_FEEDBACK -> stringResource(R.string.client_feedback)
        NotificationType.PERFORMANCE_REPORT -> stringResource(R.string.performance_report)
        NotificationType.SYSTEM_ALERT -> stringResource(R.string.system_alert)
        NotificationType.UNRESOLVED_REQUESTS -> stringResource(R.string.unresolved_requests)
        NotificationType.AGENT_PERFORMANCE -> stringResource(R.string.agent_performance)
        NotificationType.REVENUE_REPORT -> stringResource(R.string.revenue_report)
        NotificationType.MAINTENANCE_SCHEDULED -> stringResource(R.string.maintenance_scheduled)
        NotificationType.NEW_USER_REGISTERED -> stringResource(R.string.new_user_registered)
        NotificationType.PAYMENT_ISSUES -> stringResource(R.string.payment_issues)
        NotificationType.CAPACITY_WARNING -> stringResource(R.string.capacity_warning)
        NotificationType.EMERGENCY_ALERT, NotificationType.SYSTEM_MAINTENANCE -> "Emergency Alert"
    }
}