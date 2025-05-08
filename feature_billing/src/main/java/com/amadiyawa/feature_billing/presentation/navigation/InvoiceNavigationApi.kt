package com.amadiyawa.feature_billing.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.Receipt
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_base.presentation.navigation.DestinationPlacement
import com.amadiyawa.feature_base.presentation.navigation.FeatureNavigationApi
import com.amadiyawa.feature_base.presentation.navigation.NavigationDestination
import com.amadiyawa.feature_billing.R
import com.amadiyawa.feature_billing.presentation.screen.billingdetail.InvoiceDetailScreen
import com.amadiyawa.feature_billing.presentation.screen.billinglist.InvoiceListScreen
import timber.log.Timber

/**
 * Navigation API implementation for the Invoice feature
 */
class InvoiceNavigationApi : FeatureNavigationApi {
    override val featureId: String = "invoice"

    override val allowedRoles: Set<UserRole> = setOf(UserRole.CLIENT, UserRole.ADMIN)

    override val isMainDestination: Boolean = true

    override fun NavGraphBuilder.registerNavigation(navController: NavHostController) {
        Timber.d("Registering Invoice navigation graph")

        navigation(
            startDestination = Routes.INVOICE_LIST,
            route = Routes.INVOICE_GRAPH
        ) {
            composable(Routes.INVOICE_LIST) {
                Timber.d("Rendering InvoiceListScreen")
                InvoiceListScreen(
                    onInvoiceClick = { invoiceId ->
                        navController.navigate("${Routes.INVOICE_DETAIL}/$invoiceId")
                    }
                )
            }

            composable("${Routes.INVOICE_DETAIL}/{invoiceId}") { backStackEntry ->
                val invoiceId = backStackEntry.arguments?.getString("invoiceId")
                Timber.d("Rendering InvoiceDetailScreen with ID: $invoiceId")
                invoiceId?.let {
                    InvoiceDetailScreen(
                        invoiceId = it,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }

        Timber.d("Invoice navigation graph registered")
    }

    override fun getNavigationDestinations(): List<NavigationDestination> {
        return listOf(
            NavigationDestination(
                route = Routes.INVOICE_GRAPH,
                title = R.string.bills,
                selectedIcon = Icons.Filled.Receipt,
                unselectedIcon = Icons.Outlined.Receipt,
                placement = DestinationPlacement.BottomBar,
                order = 1 // First position in bottom bar
            )
        )
    }

    // Nested object for route constants
    object Routes {
        const val INVOICE_GRAPH = "invoice_graph"
        const val INVOICE_LIST = "invoice_list"
        const val INVOICE_DETAIL = "invoice_detail"
    }
}