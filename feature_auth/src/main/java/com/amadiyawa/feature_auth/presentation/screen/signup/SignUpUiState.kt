package com.amadiyawa.feature_auth.presentation.screen.signup

import androidx.compose.runtime.Immutable
import com.amadiyawa.feature_auth.domain.model.SignUpForm
import com.amadiyawa.feature_base.presentation.screen.viewmodel.BaseState

@Immutable
sealed class SignUpUiState : BaseState {

    data class Idle(val form: SignUpForm = SignUpForm()) : SignUpUiState()

    sealed class Loading(open val form: SignUpForm) : SignUpUiState() {
        data class Authentication(override val form: SignUpForm) : Loading(form)
        data class SessionSaving(override val form: SignUpForm) : Loading(form)
        data class SessionActivation(override val form: SignUpForm) : Loading(form)
    }

    data class Error(val form: SignUpForm, val message: String) : SignUpUiState()
}
