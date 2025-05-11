package com.amadiyawa.feature_requests

import com.amadiyawa.feature_requests.data.dataModule
import com.amadiyawa.feature_requests.domain.domainModule
import com.amadiyawa.feature_requests.presentation.presentationModule

val featureRequestsModule = listOf(
    dataModule,
    domainModule,
    presentationModule
)