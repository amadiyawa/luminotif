package com.amadiyawa.feature_users.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import coil.Coil.imageLoader
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_base.presentation.navigation.AppNavGraphProvider
import com.amadiyawa.feature_base.presentation.navigation.DynamicDestinationRegistry
import com.amadiyawa.feature_base.presentation.navigation.FeatureNavigationApi
import com.amadiyawa.feature_base.presentation.navigation.NavigationRegistry
import com.amadiyawa.feature_users.presentation.navigation.UserListNavigation
import com.amadiyawa.feature_users.presentation.navigation.UserNavigationApi
import com.amadiyawa.feature_users.presentation.navigation.userGraph
import com.amadiyawa.feature_users.presentation.screen.userdetail.UserDetailViewModelOld
import com.amadiyawa.feature_users.presentation.screen.userlist.UserListViewModelOld
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import timber.log.Timber

/**
 * Defines the Koin module for the presentation layer of the User feature.
 *
 * This module provides dependencies such as ViewModels, image loader, and navigation destinations
 * required for the presentation layer. It also registers the `UserDestination` with the
 * `DynamicDestinationRegistry` and defines the navigation graph for the User feature.
 */
internal val presentationModule = module {
    // Log module loading
    Timber.d("Loading user feature module")

    // Register the navigation API
    single {
        UserNavigationApi()
    } bind FeatureNavigationApi::class

    // Register the feature in the navigation registry
    factory(named("userFeatureRegistration")) {
        val registry = get<NavigationRegistry>()
        val api = get<UserNavigationApi>()
        registry.registerFeature(api)
        Timber.d("Registered UserNavigationApi with NavigationRegistry")

        // Return a value to satisfy Koin
        true
    }

    // View models and other dependencies
    viewModelOf(::UserListViewModelOld)
    viewModelOf(::UserDetailViewModelOld)

    single { imageLoader(get()) }
}