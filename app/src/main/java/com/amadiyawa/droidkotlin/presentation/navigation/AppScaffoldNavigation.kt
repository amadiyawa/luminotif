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
import com.amadiyawa.feature_base.presentation.navigation.NavigationDestination
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
    navigationDestinations: List<NavigationDestination>,
    content: @Composable () -> Unit
) {
    val currentDestination = appState.currentDestination

    // Debug logs for scaffold setup
    val bottomBarDestinations = navigationDestinations.filter {
        it.placement == DestinationPlacement.BottomBar
    }

    // Get the value of shouldShowBottomBar before using it
    val shouldShowBottomBar = appState.shouldShowBottomBar

    // Enhanced debug logging
    Timber.d("AppScaffoldNavigation setup:")
    Timber.d("- Bottom bar destinations: ${bottomBarDestinations.size}")
    Timber.d("- Current route: ${currentDestination?.route}")
    Timber.d("- shouldUseNavRail: ${appState.shouldUseNavRail}")
    Timber.d("- isInMainGraph: ${appState.isInMainGraph.value}")
    Timber.d("- shouldShowBottomBar: $shouldShowBottomBar")

    Scaffold(
        modifier = Modifier
            .semantics { testTagsAsResourceId = true }
            .safeDrawingPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            // Only show bottom bar if there are destinations to show
            if (bottomBarDestinations.isNotEmpty() && shouldShowBottomBar) {
                Timber.d("✅ Showing bottom bar with ${bottomBarDestinations.size} destinations")
                CustomBottomBar(
                    destinations = bottomBarDestinations,
                    currentDestination = currentDestination,
                    onNavigate = { route -> appState.navigate(route) }
                )
            } else {
                Timber.d("❌ Not showing bottom bar: hasDestinations=${bottomBarDestinations.isNotEmpty()}, shouldShow=$shouldShowBottomBar")
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val navRailDestinations = navigationDestinations.filter {
                it.placement == DestinationPlacement.NavRail
            }

            val shouldShowNavRail = appState.shouldShowNavRail

            // Only show nav rail if there are destinations to show
            if (navRailDestinations.isNotEmpty() && shouldShowNavRail) {
                Timber.d("Showing nav rail with ${navRailDestinations.size} destinations")
                CustomNavRail(
                    destinations = navRailDestinations,
                    currentDestination = currentDestination,
                    onNavigate = { route -> appState.navigate(route) },
                    modifier = Modifier.width(72.dp)
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}