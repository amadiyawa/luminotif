package com.amadiyawa.feature_requests.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.outlined.Engineering
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
import com.amadiyawa.feature_requests.R
import com.amadiyawa.feature_requests.presentation.screen.CreateServiceRequestScreen
import com.amadiyawa.feature_requests.presentation.screen.ServiceRequestDetailScreen
import com.amadiyawa.feature_requests.presentation.screen.ServiceRequestListScreen
import timber.log.Timber

/**
 * Navigation API implementation for the Service Request feature
 */
class ServiceRequestNavigationApi : FeatureNavigationApi {
    override val featureId: String = "service_request"

    override val allowedRoles: Set<UserRole> = setOf(
        UserRole.CLIENT,
        UserRole.AGENT,
        UserRole.ADMIN
    )

    override val isMainDestination: Boolean = false

    override fun NavGraphBuilder.registerNavigation(navController: NavHostController) {
        Timber.d("Registering Service Request navigation graph")

        navigation(
            startDestination = Routes.SERVICE_REQUEST_LIST,
            route = Routes.SERVICE_REQUEST_GRAPH
        ) {
            composable(Routes.SERVICE_REQUEST_LIST) {
                Timber.d("Rendering ServiceRequestListScreen")
                ServiceRequestListScreen(
                    onRequestClick = { requestId ->
                        navController.navigate(Routes.detailRoute(requestId))
                    },
                    onCreateClick = {
                        navController.navigate(Routes.SERVICE_REQUEST_CREATE)
                    }
                )
            }

            composable(
                route = "${Routes.SERVICE_REQUEST_DETAIL}/{${Routes.DETAIL_ID_ARG}}",
                arguments = listOf(
                    navArgument(Routes.DETAIL_ID_ARG) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val requestId = backStackEntry.arguments?.getString(Routes.DETAIL_ID_ARG)
                    ?: return@composable

                ServiceRequestDetailScreen(
                    requestId = requestId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.SERVICE_REQUEST_CREATE) {
                Timber.d("Rendering CreateServiceRequestScreen")
                CreateServiceRequestScreen(
                    onBackClick = { navController.popBackStack() },
                    onRequestCreated = { requestId ->
                        navController.navigate(Routes.detailRoute(requestId)) {
                            popUpTo(Routes.SERVICE_REQUEST_LIST)
                        }
                    }
                )
            }
        }

        Timber.d("Service Request navigation graph registered")
    }

    override fun getNavigationDestinations(): List<NavigationDestination> {
        return listOf(
            NavigationDestination(
                route = Routes.SERVICE_REQUEST_GRAPH,
                title = R.string.service_requests,
                selectedIcon = Icons.Filled.Engineering,
                unselectedIcon = Icons.Outlined.Engineering,
                placement = DestinationPlacement.BottomBar,
                order = 2 // Second position in bottom bar
            )
        )
    }

    // Nested object for route constants
    object Routes {
        const val SERVICE_REQUEST_GRAPH = "service_request_graph"
        const val SERVICE_REQUEST_LIST = "service_request_list"
        const val SERVICE_REQUEST_DETAIL = "service_request_detail"
        const val SERVICE_REQUEST_CREATE = "service_request_create"
        const val DETAIL_ID_ARG = "requestId"

        fun detailRoute(requestId: String) = "$SERVICE_REQUEST_DETAIL/$requestId"
    }
}