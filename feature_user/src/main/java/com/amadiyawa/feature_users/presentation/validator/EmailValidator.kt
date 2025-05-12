package com.amadiyawa.feature_users.presentation.validator

import android.util.Patterns

/**
 * Validator class for email validation
 */
object EmailValidator {

    /**
     * Validates if the provided email string is in a valid format
     * @param email The email string to validate
     * @return true if the email is valid, false otherwise
     */
    fun validate(email: String): Boolean {
        if (email.isBlank()) {
            return false
        }

        // Use Android's built-in Patterns for email validation
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}