package com.amadiyawa.feature_users.presentation.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_base.presentation.compose.composable.ButtonIconType
import com.amadiyawa.feature_base.presentation.compose.composable.CircularButton
import com.amadiyawa.feature_base.presentation.compose.composable.CircularButtonParams
import com.amadiyawa.feature_base.presentation.compose.composable.ErrorScreen
import com.amadiyawa.feature_base.presentation.compose.composable.TextBodyMedium
import com.amadiyawa.feature_base.presentation.compose.composable.TextTitleLarge
import com.amadiyawa.feature_base.presentation.compose.composable.TextTitleMedium
import com.amadiyawa.feature_base.presentation.compose.composable.Toolbar
import com.amadiyawa.feature_base.presentation.compose.composable.ToolbarParams
import com.amadiyawa.feature_user.R
import com.amadiyawa.feature_users.domain.repository.UserStatistics
import com.amadiyawa.feature_users.presentation.viewmodel.UserDashboardViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

/**
 * Dashboard screen for the User Management feature
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(
    viewModel: UserDashboardViewModel = koinViewModel(),
    onNavigateToClients: () -> Unit,
    onNavigateToAgents: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Handle side effects
    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is UserDashboardContract.Effect.NavigateToClientList -> {
                    onNavigateToClients()
                }
                is UserDashboardContract.Effect.NavigateToAgentList -> {
                    onNavigateToAgents()
                }
                is UserDashboardContract.Effect.ShowError -> {
                    // In a real app, show a snackbar or toast
                    // For this implementation, we're just displaying errors in the UI
                }
            }
        }
    }

    // Auto-navigate for AGENT users
    LaunchedEffect(state.userRole) {
        if (state.userRole == UserRole.AGENT) {
            viewModel.handleAction(UserDashboardContract.Action.NavigateToClients)
        }
    }

    Scaffold(
        topBar = {
            Toolbar(
                params = ToolbarParams(
                    title = stringResource(R.string.user_management)
                ),
                actions = {
                    CircularButton(
                        params = CircularButtonParams(
                            iconType = ButtonIconType.Vector(Icons.Default.Refresh),
                            onClick = { viewModel.handleAction(UserDashboardContract.Action.RefreshData) },
                            description = stringResource(R.string.refresh),
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            iconTint = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    LoadingIndicator()
                }
                state.error != null -> {
                    ErrorScreen(
                        error = state.error ?: "Unknown error occurred",
                        onRetry = { viewModel.handleAction(UserDashboardContract.Action.RefreshData) }
                    )
                }
                else -> {
                    DashboardContent(
                        state = state,
                        onClientSectionClick = {
                            viewModel.handleAction(UserDashboardContract.Action.NavigateToClients)
                        },
                        onAgentSectionClick = {
                            viewModel.handleAction(UserDashboardContract.Action.NavigateToAgents)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: UserDashboardContract.State,
    onClientSectionClick: () -> Unit,
    onAgentSectionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome message
        WelcomeSection(userName = state.userName ?: "User", role = state.userRole)

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Client Management Card - Always visible
            NavigationCard(
                title = stringResource(R.string.client_management),
                description = stringResource(R.string.manage_clients_description),
                icon = Icons.Default.Person,
                onClick = onClientSectionClick,
                modifier = Modifier.weight(1f)
            )

            // Agent Management Card - Only visible to admins
            if (state.canViewAgents) {
                NavigationCard(
                    title = stringResource(R.string.agent_management),
                    description = stringResource(R.string.manage_agents_description),
                    icon = Icons.Default.Group,
                    onClick = onAgentSectionClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Statistics section for admins
        if (state.userRole == UserRole.ADMIN && state.userStatistics != null) {
            StatisticsSection(stats = state.userStatistics)
        }
    }
}

@Composable
private fun WelcomeSection(userName: String, role: UserRole?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextTitleLarge(
                text = stringResource(R.string.welcome_format, userName),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            val roleText = when (role) {
                UserRole.ADMIN -> stringResource(R.string.admin_role)
                UserRole.AGENT -> stringResource(R.string.agent_role)
                UserRole.CLIENT -> stringResource(R.string.client_role)
                else -> stringResource(R.string.unknown_role)
            }

            TextBodyMedium(
                text = stringResource(R.string.role_format, roleText),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun NavigationCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextTitleMedium(
                text = title,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            TextBodyMedium(
                text = description,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatisticsSection(stats: UserStatistics) {
    Column {
        TextTitleMedium(
            text = stringResource(R.string.system_statistics),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Statistics cards grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = stringResource(R.string.total_clients),
                value = stats.totalClients.toString(),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = stringResource(R.string.active_clients),
                value = stats.activeClients.toString(),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = stringResource(R.string.total_agents),
                value = stats.totalAgents.toString(),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = stringResource(R.string.active_agents),
                value = stats.activeAgents.toString(),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        StatCard(
            title = stringResource(R.string.recent_registrations),
            value = stats.recentRegistrations.toString(),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        // Top areas by client count
        Spacer(modifier = Modifier.height(16.dp))

        TextTitleMedium(
            text = stringResource(R.string.clients_by_area),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        stats.clientsByArea.entries.sortedByDescending { it.value }.take(5).forEach { (area, count) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextBodyMedium(text = area)
                TextBodyMedium(
                    text = count.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 2.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextTitleLarge(
                text = value,
                color = color,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            TextBodyMedium(
                text = title,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}