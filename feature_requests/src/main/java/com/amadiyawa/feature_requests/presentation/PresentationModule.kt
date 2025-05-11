package com.amadiyawa.feature_requests.presentation

import com.amadiyawa.feature_base.presentation.navigation.FeatureNavigationApi
import com.amadiyawa.feature_base.presentation.navigation.NavigationRegistry
import com.amadiyawa.feature_requests.presentation.navigation.ServiceRequestNavigationApi
import com.amadiyawa.feature_requests.presentation.viewmodel.CreateServiceRequestViewModel
import com.amadiyawa.feature_requests.presentation.viewmodel.ServiceRequestDetailViewModel
import com.amadiyawa.feature_requests.presentation.viewmodel.ServiceRequestListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import timber.log.Timber

internal val presentationModule = module {
    // Log module loading
    Timber.d("Loading service request feature module")

    // Register the navigation API
    single {
        ServiceRequestNavigationApi()
    } bind FeatureNavigationApi::class

    // Register the feature in the navigation registry
    // This is done as a side effect when the module is loaded
    factory(named("serviceRequestFeatureRegistration")) {
        val registry = get<NavigationRegistry>()
        val api = get<ServiceRequestNavigationApi>()
        registry.registerFeature(api)
        Timber.d("Registered ServiceRequestNavigationApi with NavigationRegistry")

        // Return a dummy value to satisfy Koin
        true
    }

    viewModel {
        ServiceRequestListViewModel(
            getServiceRequests = get(),
            filterServiceRequests = get(),
            userSessionManager = get()
        )
    }

    viewModel {
        ServiceRequestDetailViewModel(
            getServiceRequestById = get(),
            getServiceRequestUpdates = get(),
            updateServiceRequestStatus = get(),
            cancelServiceRequest = get(),
            userSessionManager = get()
        )
    }

    viewModel {
        CreateServiceRequestViewModel(
            createServiceRequest = get(),
            userSessionManager = get()
        )
    }
}