package com.amadiyawa.droidkotlin.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import com.amadiyawa.feature_base.presentation.navigation.AppState
import com.amadiyawa.feature_base.presentation.navigation.CustomBottomBar
import com.amadiyawa.feature_base.presentation.navigation.CustomNavRail
import com.amadiyawa.feature_base.presentation.navigation.DestinationPlacement
import com.amadiyawa.feature_base.presentation.navigation.NewDynamicDestinationRegistry
import org.koin.compose.koinInject
import timber.log.Timber

/**
 * Composable function that sets up the navigation structure for the application.
 *
 * This function defines the scaffold layout for the app, including the bottom bar
 * and navigation rail, based on the current navigation state and destination.
 * It also renders the provided content within the scaffold.
 *
 * @param appState The [AppState] instance managing the application's navigation state.
 * @param currentDestination The current [NavDestination] being displayed.
 * @param content A composable lambda that defines the content to be displayed within the scaffold.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppScaffoldNavigation(
    appState: AppState,
    currentDestination: NavDestination?,
    content: @Composable () -> Unit
) {
    // Get destinations from appState
    val destinations = appState.getDestinations()

    // val destinations = remember { DynamicDestinationRegistry.destinations }

    // Debug log
    Timber.d("Total destinations: ${destinations.size}")
    destinations.forEach {
        Timber.d("Destination: ${it.route}, Placement: ${it.placement}")
    }

    val bottomBarDestinations = remember(destinations) {
        destinations.filter { it.placement == DestinationPlacement.BottomBar }
    }

    // Debug log
    Timber.d("Bottom bar destinations: ${bottomBarDestinations.size}")

    // Debug condition checks
    val shouldShowBottomBar = bottomBarDestinations.isNotEmpty() && appState.shouldShowBottomBar
    Timber.d("Should show bottom bar: $shouldShowBottomBar")
    Timber.d("Has bottom destinations: ${bottomBarDestinations.isNotEmpty()}")
    Timber.d("appState.shouldShowBottomBar: ${appState.shouldShowBottomBar}")
    Timber.d("Current destination: ${currentDestination?.route}")

    val navRailDestinations = remember(destinations) {
        destinations.filter { it.placement == DestinationPlacement.NavRail }
    }

    Scaffold(
        modifier = Modifier
            .semantics { testTagsAsResourceId = true }
            .safeDrawingPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            if (bottomBarDestinations.isNotEmpty() && appState.shouldShowBottomBar) {
                CustomBottomBar(
                    destinations = bottomBarDestinations,
                    currentDestination = currentDestination,
                    onNavigate = { appState.navigate(it) }
                )
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (navRailDestinations.isNotEmpty() && appState.shouldShowNavRail) {
                CustomNavRail(
                    destinations = navRailDestinations,
                    currentDestination = currentDestination,
                    onNavigate = { appState.navigate(it) },
                    modifier = Modifier.width(72.dp)
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}