package com.amadiyawa.feature_users.presentation

import coil.Coil.imageLoader
import com.amadiyawa.feature_base.presentation.navigation.NavigationRegistry
import com.amadiyawa.feature_users.presentation.navigation.UserNavigationApiComplete
import com.amadiyawa.feature_users.presentation.screen.userdetail.UserDetailViewModelOld
import com.amadiyawa.feature_users.presentation.screen.userlist.UserListViewModelOld
import com.amadiyawa.feature_users.presentation.viewmodel.AgentListViewModel
import com.amadiyawa.feature_users.presentation.viewmodel.ClientListViewModel
import com.amadiyawa.feature_users.presentation.viewmodel.CreateUserViewModel
import com.amadiyawa.feature_users.presentation.viewmodel.UserDashboardViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
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
//    single {
//        UserNavigationApi()
//    } bind FeatureNavigationApi::class

    single {
        UserNavigationApiComplete()
    } bind UserNavigationApiComplete::class

    // Register the feature in the navigation registry
    factory(named("userFeatureRegistration")) {
        val registry = get<NavigationRegistry>()
        val api = get<UserNavigationApiComplete>()
        registry.registerFeature(api)
        Timber.d("Registered UserNavigationApi with NavigationRegistry")

        // Return a value to satisfy Koin
        true
    }



    viewModel {
        ClientListViewModel(
            getAllClientsUseCase = get(),
            searchClientsUseCase = get(),
            getClientsPagedUseCase = get(),
            observeClientUpdatesUseCase = get(),
            userSessionManager = get()
        )
    }

    viewModel {
        AgentListViewModel(
            getAllAgentsUseCase = get(),
            getAgentsByTerritoryUseCase = get(),
            getAgentsPagedUseCase = get(),
            observeAgentUpdatesUseCase = get(),
            userSessionManager = get()
        )
    }

    viewModel {
        CreateUserViewModel(
            userRepository = get(),
            phoneNumberValidator = get(),
            userSessionManager = get()
        )
    }

    viewModel {
        UserDashboardViewModel(
            getUserStatisticsUseCase = get(),
            userSessionManager = get()
        )
    }

    // View models and other dependencies
    viewModelOf(::UserListViewModelOld)
    viewModelOf(::UserDetailViewModelOld)

    single { imageLoader(get()) }
}