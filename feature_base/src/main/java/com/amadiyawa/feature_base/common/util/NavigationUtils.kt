package com.amadiyawa.feature_base.common.util

import com.amadiyawa.feature_base.domain.model.UserSessionManager
import com.amadiyawa.feature_base.presentation.navigation.AppNavGraphProvider

/**
 * Determines the main start destination based on the current user's role and available graph providers.
 *
 * @param graphProviders A collection of all available navigation graph providers.
 * @param userSessionManager The user session manager to get the current role.
 * @return The start destination route for the main navigation graph.
 */
fun getMainStartDestination(
    graphProviders: Collection<AppNavGraphProvider>,
    userSessionManager: UserSessionManager
): String {
    // Get current user role
    val currentRole = userSessionManager.currentRole.value ?: return ""

    // Filter graph providers based on user role
    val accessibleProviders = graphProviders.filter { provider ->
        provider.allowedRoles.contains(currentRole)
    }

    // Find the first provider marked as main start destination for this role
    val mainProvider = accessibleProviders.firstOrNull { it.isMainStartDestination }

    // If no main provider is found, use the first accessible provider
    return mainProvider?.startDestination ?: accessibleProviders.firstOrNull()?.startDestination ?: ""
}