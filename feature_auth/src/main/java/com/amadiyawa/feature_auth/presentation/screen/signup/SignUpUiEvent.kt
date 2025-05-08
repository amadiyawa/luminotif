package com.amadiyawa.feature_auth.presentation.screen.signup

/**
 * Represents one-time UI events that should not be part of the persistent state
 * and can occur on the Sign-Up screen.
 *
 * This sealed interface defines different types of events that can be triggered
 * during user interaction with the Sign-Up screen. Each event corresponds to a specific
 * action or navigation flow.
 */
internal sealed interface SignUpUiEvent {

    data object NavigateToMainScreen : SignUpUiEvent

    /**
     * Event to display a snackbar with a message.
     *
     * @param message The message to be displayed in the snackbar.
     */
    data class ShowSnackbar(val message: String) : SignUpUiEvent
}