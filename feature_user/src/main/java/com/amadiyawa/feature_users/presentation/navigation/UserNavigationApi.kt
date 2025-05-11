package com.amadiyawa.feature_users.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.People
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_base.presentation.navigation.DestinationPlacement
import com.amadiyawa.feature_base.presentation.navigation.FeatureNavigationApi
import com.amadiyawa.feature_base.presentation.navigation.NavigationDestination
import com.amadiyawa.feature_user.R
import com.amadiyawa.feature_users.presentation.screen.userdetail.UserDetailScreen
import com.amadiyawa.feature_users.presentation.screen.userlist.UserListScreen
import timber.log.Timber

class UserNavigationApi : FeatureNavigationApi {
    override val featureId: String = "user"

    override val allowedRoles: Set<UserRole> = setOf(UserRole.CLIENT, UserRole.ADMIN)

    override val isMainDestination: Boolean = false

    override fun NavGraphBuilder.registerNavigation(navController: NavHostController) {
        Timber.d("Registering User navigation graph")

        navigation(
            startDestination = Routes.USER_LIST,
            route = Routes.USER_GRAPH
        ) {
            composable(Routes.USER_LIST) {
                Timber.d("Rendering UserListScreen")
                UserListScreen(
                    onUserClick = { userId ->
                        navController.navigate("${Routes.USER_DETAIL}/$userId")
                    }
                )
            }

            composable("${Routes.USER_DETAIL}/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                Timber.d("Rendering UserDetailScreen with ID: $userId")
                userId?.let {
                    UserDetailScreen(
                        uuid = it,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }

        Timber.d("User navigation graph registered")
    }

    override fun getNavigationDestinations(): List<NavigationDestination> {
        return listOf(
            NavigationDestination(
                route = Routes.USER_GRAPH,
                title = R.string.users,
                selectedIcon = Icons.Filled.People,
                unselectedIcon = Icons.Outlined.People,
                placement = DestinationPlacement.BottomBar,
                order = 3 // Second position in bottom bar, after Invoice
            )
        )
    }

    // Nested object for route constants
    object Routes {
        const val USER_GRAPH = "user_graph"
        const val USER_LIST = "user_list"
        const val USER_DETAIL = "user_detail"
    }

}