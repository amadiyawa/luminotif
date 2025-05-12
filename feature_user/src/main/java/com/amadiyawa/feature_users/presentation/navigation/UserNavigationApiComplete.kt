package com.amadiyawa.feature_users.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SupervisedUserCircle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_base.presentation.navigation.DestinationPlacement
import com.amadiyawa.feature_base.presentation.navigation.FeatureNavigationApi
import com.amadiyawa.feature_base.presentation.navigation.NavigationDestination
import com.amadiyawa.feature_user.R
import com.amadiyawa.feature_users.presentation.screen.AgentDetailScreen
import com.amadiyawa.feature_users.presentation.screen.ClientDetailScreen
import com.amadiyawa.feature_users.presentation.screen.CreateUserScreen
import com.amadiyawa.feature_users.presentation.screen.EditUserScreen
import com.amadiyawa.feature_users.presentation.screen.agent.list.AgentListScreen
import com.amadiyawa.feature_users.presentation.screen.client.list.ClientListScreen
import com.amadiyawa.feature_users.presentation.screen.dashboard.UserDashboardScreen
import timber.log.Timber

/**
 * Complete Navigation API implementation for the User Management feature
 * Includes all screens for full user management functionality
 */
class UserNavigationApiComplete : FeatureNavigationApi {
    override val featureId: String = "user"

    override val allowedRoles: Set<UserRole> = setOf(UserRole.AGENT, UserRole.ADMIN)

    override val isMainDestination: Boolean = false

    override fun NavGraphBuilder.registerNavigation(navController: NavHostController) {
        Timber.d("Registering Complete User Management navigation graph")

        navigation(
            startDestination = Routes.USER_DASHBOARD,
            route = Routes.USER_GRAPH
        ) {
            // Dashboard - Main entry point
            composable(Routes.USER_DASHBOARD) {
                Timber.d("Rendering UserDashboardScreen")
                UserDashboardScreen(
                    onNavigateToClients = {
                        navController.navigate(Routes.CLIENT_LIST)
                    },
                    onNavigateToAgents = {
                        navController.navigate(Routes.AGENT_LIST)
                    }
                )
            }

            // Client List
            composable(Routes.CLIENT_LIST) {
                Timber.d("Rendering ClientListScreen")
                ClientListScreen(
                    onClientClick = { clientId ->
                        navController.navigate(Routes.clientDetailRoute(clientId))
                    },
                    onAddClient = {
                        navController.navigate(Routes.createUserRoute(UserType.CLIENT))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Client Detail
            composable(
                route = "${Routes.CLIENT_DETAIL}/{${Routes.CLIENT_ID_ARG}}",
                arguments = listOf(
                    navArgument(Routes.CLIENT_ID_ARG) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString(Routes.CLIENT_ID_ARG)
                    ?: return@composable

                ClientDetailScreen(
                    clientId = clientId,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = {
                        navController.navigate(Routes.editUserRoute(UserType.CLIENT, clientId))
                    }
                )
            }

            // Agent List (Admin only)
            composable(Routes.AGENT_LIST) {
                Timber.d("Rendering AgentListScreen")
                AgentListScreen(
                    onAgentClick = { agentId ->
                        navController.navigate(Routes.agentDetailRoute(agentId))
                    },
                    onAddAgent = {
                        navController.navigate(Routes.createUserRoute(UserType.AGENT))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Agent Detail (Admin only)
            composable(
                route = "${Routes.AGENT_DETAIL}/{${Routes.AGENT_ID_ARG}}",
                arguments = listOf(
                    navArgument(Routes.AGENT_ID_ARG) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val agentId = backStackEntry.arguments?.getString(Routes.AGENT_ID_ARG)
                    ?: return@composable

                AgentDetailScreen(
                    agentId = agentId,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = {
                        navController.navigate(Routes.editUserRoute(UserType.AGENT, agentId))
                    }
                )
            }

            // Create User (Admin only)
            composable(
                route = "${Routes.CREATE_USER}/{${Routes.USER_TYPE_ARG}}",
                arguments = listOf(
                    navArgument(Routes.USER_TYPE_ARG) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val userType = backStackEntry.arguments?.getString(Routes.USER_TYPE_ARG)
                    ?.let { UserType.valueOf(it) } ?: return@composable

                CreateUserScreen(
                    userType = userType,
                    onBackClick = { navController.popBackStack() },
                    onUserCreated = { navController.popBackStack() }
                )
            }

            // Edit User (Admin only)
            composable(
                route = "${Routes.EDIT_USER}/{${Routes.USER_TYPE_ARG}}/{${Routes.USER_ID_ARG}}",
                arguments = listOf(
                    navArgument(Routes.USER_TYPE_ARG) {
                        type = NavType.StringType
                    },
                    navArgument(Routes.USER_ID_ARG) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val userType = backStackEntry.arguments?.getString(Routes.USER_TYPE_ARG)
                    ?.let { UserType.valueOf(it) } ?: return@composable
                val userId = backStackEntry.arguments?.getString(Routes.USER_ID_ARG)
                    ?: return@composable

                EditUserScreen(
                    userType = userType,
                    userId = userId,
                    onBackClick = { navController.popBackStack() },
                    onUserUpdated = { navController.popBackStack() }
                )
            }
        }

        Timber.d("Complete User Management navigation graph registered")
    }

    override fun getNavigationDestinations(): List<NavigationDestination> {
        // For ADMIN: Show all user management options
        // For AGENT: Show only client management
        return when {
            allowedRoles.contains(UserRole.ADMIN) -> listOf(
                NavigationDestination(
                    route = Routes.USER_GRAPH,
                    title = R.string.users,
                    selectedIcon = Icons.Filled.SupervisedUserCircle,
                    unselectedIcon = Icons.Outlined.SupervisedUserCircle,
                    placement = DestinationPlacement.BottomBar,
                    order = 3 // Second position in bottom bar
                )
            )
            allowedRoles.contains(UserRole.AGENT) -> listOf(
                NavigationDestination(
                    route = Routes.CLIENT_LIST,
                    title = R.string.clients,
                    selectedIcon = Icons.Filled.Person,
                    unselectedIcon = Icons.Outlined.Person,
                    placement = DestinationPlacement.BottomBar,
                    order = 3 // Second position in bottom bar
                )
            )
            else -> emptyList()
        }
    }

    // Nested object for route constants
    object Routes {
        const val USER_GRAPH = "user_graph"
        const val USER_DASHBOARD = "user_dashboard"
        const val CLIENT_LIST = "client_list"
        const val CLIENT_DETAIL = "client_detail"
        const val AGENT_LIST = "agent_list"
        const val AGENT_DETAIL = "agent_detail"
        const val CREATE_USER = "create_user"
        const val EDIT_USER = "edit_user"

        const val CLIENT_ID_ARG = "clientId"
        const val AGENT_ID_ARG = "agentId"
        const val USER_TYPE_ARG = "userType"
        const val USER_ID_ARG = "userId"

        fun clientDetailRoute(clientId: String) = "$CLIENT_DETAIL/$clientId"
        fun agentDetailRoute(agentId: String) = "$AGENT_DETAIL/$agentId"
        fun createUserRoute(userType: UserType) = "$CREATE_USER/${userType.name}"
        fun editUserRoute(userType: UserType, userId: String) = "$EDIT_USER/${userType.name}/$userId"
    }

    enum class UserType {
        CLIENT,
        AGENT,
        ADMIN
    }
}