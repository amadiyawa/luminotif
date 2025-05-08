package com.amadiyawa.feature_base.presentation

import com.amadiyawa.feature_base.common.resources.AndroidStringResourceProvider
import com.amadiyawa.feature_base.common.resources.StringResourceProvider
import com.amadiyawa.feature_base.presentation.navigation.NewDynamicDestinationRegistry
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val presentationModule = module {
    single<StringResourceProvider> { AndroidStringResourceProvider(androidContext()) }
    // DynamicDestinationRegistry
    single { NewDynamicDestinationRegistry(get()) }
}