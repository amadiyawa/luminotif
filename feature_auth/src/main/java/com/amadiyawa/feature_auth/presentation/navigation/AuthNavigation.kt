package com.amadiyawa.feature_auth.presentation.navigation

import com.amadiyawa.feature_base.presentation.navigation.AppNavigationDestination

// Navigation destinations
object WelcomeNavigation : AppNavigationDestination {
    override val route = "auth_welcome"
    override val destination = "auth_welcome_destination"
}

object SignInNavigation : AppNavigationDestination {
    override val route = "auth_sign_in"
    override val destination = "auth_sign_in_destination"
}

object SignUpNavigation : AppNavigationDestination {
    override val route = "auth_sign_up"
    override val destination = "auth_sign_up_destination"
}