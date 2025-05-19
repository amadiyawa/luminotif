package com.amadiyawa.feature_notification.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_base.presentation.navigation.DestinationPlacement
import com.amadiyawa.feature_base.presentation.navigation.FeatureNavigationApi
import com.amadiyawa.feature_base.presentation.navigation.NavigationDestination
import com.amadiyawa.feature_notification.R
import com.amadiyawa.feature_notification.presentation.screen.notificationlist.NotificationListScreen
import timber.log.Timber

/**
 * Navigation API implementation for the Notification feature
 */
class NotificationNavigationApi : FeatureNavigationApi {
    override val featureId: String = "notification"

    override val allowedRoles: Set<UserRole> = setOf(
        UserRole.CLIENT,
        UserRole.AGENT,
        UserRole.ADMIN
    )

    override val isMainDestination: Boolean = false

    override fun NavGraphBuilder.registerNavigation(navController: NavHostController) {
        Timber.d("Registering Notification navigation graph")

        navigation(
            startDestination = Routes.NOTIFICATION_LIST,
            route = Routes.NOTIFICATION_GRAPH
        ) {
            composable(Routes.NOTIFICATION_LIST) {
                Timber.d("Rendering NotificationListScreen")
                NotificationListScreen()
            }
        }

        Timber.d("Notification navigation graph registered")
    }

    override fun getNavigationDestinations(): List<NavigationDestination> {
        return listOf(
            NavigationDestination(
                route = Routes.NOTIFICATION_GRAPH,
                title = R.string.notifications,
                selectedIcon = Icons.Filled.Notifications,
                unselectedIcon = Icons.Outlined.Notifications,
                placement = DestinationPlacement.BottomBar,
                order = 4 // Fourth position in bottom bar
            )
        )
    }

    // Nested object for route constants
    object Routes {
        const val NOTIFICATION_GRAPH = "notification_graph"
        const val NOTIFICATION_LIST = "notification_list"
    }
}