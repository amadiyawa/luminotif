package com.amadiyawa.feature_users.presentation.contract

import com.amadiyawa.feature_base.domain.util.UserStatus
import com.amadiyawa.feature_users.presentation.navigation.UserNavigationApiComplete

/**
 * Contract for Create User screen to manage state, actions and effects
 */
object CreateUserContract {

    /**
     * Represents the UI state for the Create User screen
     */
    data class State(
        // Form input fields
        val fullName: String = "",
        val email: String = "",
        val phoneNumber: String = "",
        val status: UserStatus = UserStatus.PENDING_VERIFICATION,

        // Client-specific fields
        val address: String = "",
        val meterNumber: String = "",
        val accountNumber: String = "",
        val area: String = "",

        // Agent-specific fields
        val employeeId: String = "",
        val territories: List<String> = emptyList(),
        val availableTerritories: List<String> = emptyList(),

        // Form validation state
        val fullNameError: String? = null,
        val emailError: String? = null,
        val phoneNumberError: String? = null,
        val addressError: String? = null,
        val meterNumberError: String? = null,
        val accountNumberError: String? = null,
        val areaError: String? = null,
        val employeeIdError: String? = null,
        val territoriesError: String? = null,

        // Screen state
        val userType: UserNavigationApiComplete.UserType? = null,
        val isLoading: Boolean = false,
        val isSubmitting: Boolean = false,
        val hasFormErrors: Boolean = false,
        val availableAreas: List<String> = emptyList(),
        val showTerritorySelectionDialog: Boolean = false
    )

    /**
     * Actions that can be triggered from the UI
     */
    sealed class Action {
        // Navigation actions
        object NavigateBack : Action()

        // Form input actions
        data class UpdateFullName(val value: String) : Action()
        data class UpdateEmail(val value: String) : Action()
        data class UpdatePhoneNumber(val value: String) : Action()
        data class UpdateStatus(val value: UserStatus) : Action()

        // Client-specific actions
        data class UpdateAddress(val value: String) : Action()
        data class UpdateMeterNumber(val value: String) : Action()
        data class UpdateAccountNumber(val value: String) : Action()
        data class UpdateArea(val value: String) : Action()

        // Agent-specific actions
        data class UpdateEmployeeId(val value: String) : Action()
        data class AddTerritory(val value: String) : Action()
        data class RemoveTerritory(val value: String) : Action()
        data class UpdateTerritories(val values: List<String>) : Action()

        // Dialog actions
        object ShowTerritorySelectionDialog : Action()
        object HideTerritorySelectionDialog : Action()

        // Form validation and submission
        object ValidateForm : Action()
        object SubmitForm : Action()
        object ResetForm : Action()
        object LoadAreasAndTerritories : Action()
    }

    /**
     * Side effects triggered by the ViewModel
     */
    sealed class Effect {
        object NavigateBack : Effect()
        object UserCreatedSuccessfully : Effect()
        data class ShowError(val message: String) : Effect()
        data class ShowSuccess(val message: String) : Effect()
    }
}