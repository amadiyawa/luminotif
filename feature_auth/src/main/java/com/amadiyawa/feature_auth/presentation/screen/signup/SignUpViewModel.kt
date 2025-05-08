package com.amadiyawa.feature_auth.presentation.screen.signup

import androidx.lifecycle.viewModelScope
import com.amadiyawa.feature_auth.data.dto.request.SignInRequest
import com.amadiyawa.feature_auth.domain.mapper.toSignUpForm
import com.amadiyawa.feature_auth.domain.model.AuthResult
import com.amadiyawa.feature_auth.domain.model.SignUpForm
import com.amadiyawa.feature_auth.domain.model.toJson
import com.amadiyawa.feature_auth.domain.model.updateAndValidateField
import com.amadiyawa.feature_auth.domain.usecase.SignInUseCase
import com.amadiyawa.feature_auth.domain.util.validation.SignUpFormValidator
import com.amadiyawa.feature_base.domain.model.ValidatedForm
import com.amadiyawa.feature_base.domain.repository.SessionRepository
import com.amadiyawa.feature_base.domain.result.OperationResult
import com.amadiyawa.feature_base.presentation.screen.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class SignUpViewModel(
    private val validator: SignUpFormValidator,
    private val signInUseCase: SignInUseCase,
    private val sessionRepository: SessionRepository
) : BaseViewModel<SignUpUiState, SignUpAction>(SignUpUiState.Idle()) {

    // Jobs
    private var signUpJob: Job? = null

    val form: SignUpForm
        get() = when (val s = state) {
            is SignUpUiState.Idle -> s.form
            is SignUpUiState.Loading -> s.form
            is SignUpUiState.Error -> s.form
        }

    override fun dispatch(action: SignUpAction) {
        logAction(action)

        when (action) {
            is SignUpAction.UpdateForm -> setState { SignUpUiState.Idle(form = action.form) }

            is SignUpAction.UpdateField -> {
                val updatedForm = form.updateAndValidateField(action.key, action.value, validator)
                setState { SignUpUiState.Idle(form = updatedForm) }
            }

            SignUpAction.TogglePasswordVisibility -> {
                val updated = form.togglePasswordVisibility()
                setState { SignUpUiState.Idle(form = updated) }
            }

            SignUpAction.Submit -> handleSubmit()

            is SignUpAction.ShowError -> {
                setState { SignUpUiState.Error(form = form, message = action.message) }
            }

            is SignUpAction.ShowValidationErrors -> {
                setState { SignUpUiState.Error(form = action.form, message = "Corrige les erreurs du formulaire") }
            }

            is SignUpAction.UpdatePreferredRecipient -> {
                val updated = form.copy(preferredRecipient = action.type)
                setState { SignUpUiState.Idle(form = updated) }
            }
        }
    }

    private fun handleSubmit() {
        val result = validator.validate(form)
        if (!result.isValid) {
            handleValidationError(result)
            return
        }

        setState { SignUpUiState.Loading.Authentication(form = form) }

        signUpJob?.cancel()
        signUpJob = viewModelScope.launch {
            val result = signInUseCase(
                SignInRequest(
                    identifier = form.email.value,
                    password = form.password.value
                )
            )
            handleAuthResult(result)
        }
    }

    private suspend fun handleAuthResult(
        result: OperationResult<AuthResult>
    ) {
        when (result) {
            is OperationResult.Success -> handleAuthSuccess(result.data)
            is OperationResult.Error -> handleAuthError(result.message!!)
            is OperationResult.Failure -> handleAuthError(result.message!!)
        }
    }

    private suspend fun handleAuthSuccess(authResult: AuthResult) {
        setState { SignUpUiState.Loading.SessionSaving(form = form) }
        delay(2000)

        val saveResult = sessionRepository.saveSessionUserJson(authResult.toJson())
        if (saveResult is OperationResult.Success) {
            val activeResult = sessionRepository.setSessionActive(true)

            setState { SignUpUiState.Loading.SessionActivation(form = form) }
            delay(2000)
            if (activeResult is OperationResult.Success) {
                emitEvent(SignUpUiEvent.ShowSnackbar("Sign in successful"))
                emitEvent(SignUpUiEvent.NavigateToMainScreen)
                setState { SignUpUiState.Idle(form = SignUpForm()) }
            } else if (activeResult is OperationResult.Error) {
                handleAuthError(
                    message = activeResult.message!!
                )
            }
        } else if (saveResult is OperationResult.Error) {
            handleAuthError(
                message = saveResult.message!!
            )
        }
    }

    private fun handleAuthError(
        message: String
    ) {
        setState { SignUpUiState.Error(form = form, message = message) }
        emitEvent(SignUpUiEvent.ShowSnackbar(message))
    }

    private fun handleValidationError(validated: ValidatedForm) {
        setState {
            SignUpUiState.Error(
                form = validated.toSignUpForm(),
                message = "Please correct the form errors"
            )
        }
        emitEvent(SignUpUiEvent.ShowSnackbar("Please correct the form errors"))
    }
}