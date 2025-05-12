package com.amadiyawa.feature_users.presentation.screen

import androidx.compose.runtime.Composable
import com.amadiyawa.feature_base.presentation.compose.composable.EmptyScreen
import com.amadiyawa.feature_users.presentation.navigation.UserNavigationApiComplete.UserType


// Client Detail Screen
@Composable
fun ClientDetailScreen(
    clientId: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    EmptyScreen(
        title = "Client Detail",
        message = "Client ID: $clientId"
    )
}

// Agent Detail Screen
@Composable
fun AgentDetailScreen(
    agentId: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    EmptyScreen(
        title = "Agent Detail",
        message = "Agent ID: $agentId"
    )
}

// Edit User Screen
@Composable
fun EditUserScreen(
    userType: com.amadiyawa.feature_users.presentation.navigation.UserNavigationApiComplete.UserType,
    userId: String,
    onBackClick: () -> Unit,
    onUserUpdated: () -> Unit
) {
    EmptyScreen(
        title = "Edit User",
        message = "Editing ${userType.name} with ID: $userId"
    )
}