package com.amadiyawa.feature_auth.presentation.navigation

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.amadiyawa.feature_auth.domain.model.VerificationResult
import com.amadiyawa.feature_auth.presentation.screen.forgotpassword.ForgotPasswordScreen
import com.amadiyawa.feature_auth.presentation.screen.otpverification.OtpVerificationScreen
import com.amadiyawa.feature_auth.presentation.screen.resetpassword.ResetPasswordScreen
import com.amadiyawa.feature_auth.presentation.screen.signin.SignInScreen
import com.amadiyawa.feature_auth.presentation.screen.signup.SignUpScreen
import com.amadiyawa.feature_auth.presentation.screen.welcome.WelcomeScreen
import kotlinx.serialization.json.Json

internal data class AuthGraphCallbacks(
    val onSignIn: () -> Unit,
    val onNavigateToSignUp: () -> Unit,
    val onSignInSuccess: () -> Unit,
    val onSignUpSuccess: () -> Unit
)

// Auth navigation graph
internal fun NavGraphBuilder.authGraph(callbacks: AuthGraphCallbacks) {
    navigation(
        startDestination = WelcomeNavigation.destination,
        route = "auth"
    ) {
        composable(WelcomeNavigation.destination) {
            WelcomeScreen(
                onSignIn = callbacks.onSignIn,
                onSignUp = callbacks.onNavigateToSignUp
            )
        }

        composable(SignInNavigation.route) {
            SignInScreen(
                onSignInSuccess = callbacks.onSignInSuccess
            )
        }

        composable(SignUpNavigation.route) {
            SignUpScreen(
                onSignUpSuccess = callbacks.onSignUpSuccess
            )
        }
    }
}

// Extension function to add auth graph to NavGraphBuilder
fun NavGraphBuilder.authGraph(navController: NavHostController) {
    val callbacks = AuthGraphCallbacks(
        onSignIn = {
            navController.navigate(SignInNavigation.route)
        },
        onNavigateToSignUp = {
            navController.navigate(SignUpNavigation.route)
        },
        onSignInSuccess = {
            // Save session and navigate to main
            navController.navigate("main") {
                popUpTo("auth") { inclusive = true }
                launchSingleTop = true
            }
        },
        onSignUpSuccess = {
            // Save session and navigate to main
            navController.navigate("main") {
                popUpTo("auth") { inclusive = true }
                launchSingleTop = true
            }
        }
    )

    authGraph(callbacks)
}