package com.amadiyawa.feature_users.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amadiyawa.feature_base.common.util.PhoneNumberValidator
import com.amadiyawa.feature_base.domain.model.UserSessionManager
import com.amadiyawa.feature_base.domain.result.OperationResult
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_users.domain.model.Agent
import com.amadiyawa.feature_users.domain.model.Client
import com.amadiyawa.feature_users.domain.repository.UserRepository
import com.amadiyawa.feature_users.presentation.contract.CreateUserContract
import com.amadiyawa.feature_users.presentation.navigation.UserNavigationApiComplete.UserType
import com.amadiyawa.feature_users.presentation.validator.EmailValidator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.util.UUID

/**
 * ViewModel for creating new users (clients and agents)
 */
class CreateUserViewModel(
    private val userRepository: UserRepository,
    private val phoneNumberValidator: PhoneNumberValidator,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(CreateUserContract.State())
    val state: StateFlow<CreateUserContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CreateUserContract.Effect>()
    val effect: SharedFlow<CreateUserContract.Effect> = _effect.asSharedFlow()

    /**
     * Initialize the ViewModel with the user type
     * This should be called immediately after creating the ViewModel
     */
    fun initialize(userType: UserType) {
        // Check if current user is allowed to create this type of user
        val currentRole = userSessionManager.currentRole.value

        if (currentRole != UserRole.ADMIN) {
            // Only Admin can create users
            viewModelScope.launch {
                _effect.emit(CreateUserContract.Effect.ShowError("Only administrators can create users"))
                _effect.emit(CreateUserContract.Effect.NavigateBack)
            }
            return
        }

        // Set user type and load initial data
        _state.update { it.copy(userType = userType) }
        loadInitialData()
    }

    /**
     * Load areas and territories data
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // For a real implementation with the FakeUserRepository, we would:

                // 1. Get all clients to extract unique areas
                val clientsResult = userRepository.getAllClients()
                if (clientsResult is OperationResult.Success) {
                    val areas = clientsResult.data.map { it.area }.distinct().sorted()
                    _state.update { it.copy(availableAreas = areas) }
                }

                // 2. Get all agents to extract unique territories
                val agentsResult = userRepository.getAllAgents()
                if (agentsResult is OperationResult.Success) {
                    val territories = agentsResult.data.flatMap { it.territories }.distinct().sorted()
                    _state.update { it.copy(availableTerritories = territories) }
                }

                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Timber.e(e, "Error loading initial data")
                _state.update { it.copy(isLoading = false) }
                _effect.emit(CreateUserContract.Effect.ShowError("Failed to load initial data: ${e.message}"))
            }
        }
    }

    /**
     * Handles UI actions
     */
    fun handleAction(action: CreateUserContract.Action) {
        when (action) {
            // Navigation actions
            is CreateUserContract.Action.NavigateBack -> {
                viewModelScope.launch {
                    _effect.emit(CreateUserContract.Effect.NavigateBack)
                }
            }

            // Form input actions - Common fields
            is CreateUserContract.Action.UpdateFullName -> {
                _state.update { it.copy(
                    fullName = action.value,
                    fullNameError = if (action.value.isBlank()) "Name cannot be empty" else null
                ) }
            }

            is CreateUserContract.Action.UpdateEmail -> {
                val isValid = EmailValidator.validate(action.value)
                _state.update { it.copy(
                    email = action.value,
                    emailError = if (!isValid) "Invalid email format" else null
                ) }
            }

            is CreateUserContract.Action.UpdatePhoneNumber -> {
                val isValid = phoneNumberValidator.isValid(action.value)
                _state.update { it.copy(
                    phoneNumber = action.value,
                    phoneNumberError = if (!isValid) "Invalid phone number format" else null
                ) }
            }

            is CreateUserContract.Action.UpdateStatus -> {
                _state.update { it.copy(status = action.value) }
            }

            // Client-specific actions
            is CreateUserContract.Action.UpdateAddress -> {
                _state.update { it.copy(
                    address = action.value,
                    addressError = if (action.value.isBlank() && _state.value.userType == UserType.CLIENT)
                        "Address cannot be empty" else null
                ) }
            }

            is CreateUserContract.Action.UpdateMeterNumber -> {
                _state.update { it.copy(
                    meterNumber = action.value,
                    meterNumberError = if (action.value.isBlank() && _state.value.userType == UserType.CLIENT)
                        "Meter number cannot be empty" else null
                ) }
            }

            is CreateUserContract.Action.UpdateAccountNumber -> {
                _state.update { it.copy(
                    accountNumber = action.value,
                    accountNumberError = if (action.value.isBlank() && _state.value.userType == UserType.CLIENT)
                        "Account number cannot be empty" else null
                ) }
            }

            is CreateUserContract.Action.UpdateArea -> {
                _state.update { it.copy(
                    area = action.value,
                    areaError = if (action.value.isBlank() && _state.value.userType == UserType.CLIENT)
                        "Area cannot be empty" else null
                ) }
            }

            // Agent-specific actions
            is CreateUserContract.Action.UpdateEmployeeId -> {
                _state.update { it.copy(
                    employeeId = action.value,
                    employeeIdError = if (action.value.isBlank() && _state.value.userType == UserType.AGENT)
                        "Employee ID cannot be empty" else null
                ) }
            }

            is CreateUserContract.Action.AddTerritory -> {
                val currentTerritories = _state.value.territories.toMutableList()
                if (!currentTerritories.contains(action.value)) {
                    currentTerritories.add(action.value)
                }

                _state.update { it.copy(
                    territories = currentTerritories,
                    territoriesError = if (currentTerritories.isEmpty() && _state.value.userType == UserType.AGENT)
                        "At least one territory must be selected" else null
                ) }
            }

            is CreateUserContract.Action.RemoveTerritory -> {
                val currentTerritories = _state.value.territories.toMutableList()
                currentTerritories.remove(action.value)

                _state.update { it.copy(
                    territories = currentTerritories,
                    territoriesError = if (currentTerritories.isEmpty() && _state.value.userType == UserType.AGENT)
                        "At least one territory must be selected" else null
                ) }
            }

            is CreateUserContract.Action.UpdateTerritories -> {
                _state.update { it.copy(
                    territories = action.values,
                    territoriesError = if (action.values.isEmpty() && _state.value.userType == UserType.AGENT)
                        "At least one territory must be selected" else null
                ) }
            }

            // Dialog actions
            is CreateUserContract.Action.ShowTerritorySelectionDialog -> {
                _state.update { it.copy(showTerritorySelectionDialog = true) }
            }

            is CreateUserContract.Action.HideTerritorySelectionDialog -> {
                _state.update { it.copy(showTerritorySelectionDialog = false) }
            }

            // Form actions
            is CreateUserContract.Action.ValidateForm -> {
                validateForm()
            }

            is CreateUserContract.Action.SubmitForm -> {
                submitForm()
            }

            is CreateUserContract.Action.ResetForm -> {
                resetForm()
            }

            is CreateUserContract.Action.LoadAreasAndTerritories -> {
                loadInitialData()
            }
        }
    }

    /**
     * Validate the form based on user type and check for duplicate email/phone
     */
    private fun validateForm() {
        viewModelScope.launch {
            // Common validations
            val fullNameError = if (_state.value.fullName.isBlank()) "Name cannot be empty" else null
            val isEmailValid = EmailValidator.validate(_state.value.email)
            val emailError = if (!isEmailValid) "Invalid email format" else null
            val isPhoneValid = phoneNumberValidator.isValid(_state.value.phoneNumber)
            val phoneNumberError = if (!isPhoneValid) "Invalid phone number format" else null

            // Type-specific validations
            val addressError = if (_state.value.userType == UserType.CLIENT && _state.value.address.isBlank())
                "Address cannot be empty" else null
            val meterNumberError = if (_state.value.userType == UserType.CLIENT && _state.value.meterNumber.isBlank())
                "Meter number cannot be empty" else null
            val accountNumberError = if (_state.value.userType == UserType.CLIENT && _state.value.accountNumber.isBlank())
                "Account number cannot be empty" else null
            val areaError = if (_state.value.userType == UserType.CLIENT && _state.value.area.isBlank())
                "Area cannot be empty" else null

            val employeeIdError = if (_state.value.userType == UserType.AGENT && _state.value.employeeId.isBlank())
                "Employee ID cannot be empty" else null
            val territoriesError = if (_state.value.userType == UserType.AGENT && _state.value.territories.isEmpty())
                "At least one territory must be selected" else null

            // Check for duplicate email
            var emailTakenError: String? = null
            if (emailError == null && _state.value.email.isNotBlank()) {
                val emailTakenResult = userRepository.isEmailTaken(_state.value.email)
                if (emailTakenResult is OperationResult.Success && emailTakenResult.data) {
                    emailTakenError = "Email is already registered with another account"
                }
            }

            // Check for duplicate phone number
            var phoneTakenError: String? = null
            if (phoneNumberError == null && _state.value.phoneNumber.isNotBlank()) {
                val phoneTakenResult = userRepository.isPhoneTaken(_state.value.phoneNumber)
                if (phoneTakenResult is OperationResult.Success && phoneTakenResult.data) {
                    phoneTakenError = "Phone number is already registered with another account"
                }
            }

            // Update state with validation errors
            _state.update { it.copy(
                fullNameError = fullNameError,
                emailError = emailError ?: emailTakenError,
                phoneNumberError = phoneNumberError ?: phoneTakenError,
                addressError = addressError,
                meterNumberError = meterNumberError,
                accountNumberError = accountNumberError,
                areaError = areaError,
                employeeIdError = employeeIdError,
                territoriesError = territoriesError,
                hasFormErrors = fullNameError != null ||
                        (emailError ?: emailTakenError) != null ||
                        (phoneNumberError ?: phoneTakenError) != null ||
                        addressError != null ||
                        meterNumberError != null ||
                        accountNumberError != null ||
                        areaError != null ||
                        employeeIdError != null ||
                        territoriesError != null
            ) }
        }
    }

    /**
     * Submit the form if valid
     */
    private fun submitForm() {
        // First validate the form
        validateForm()

        viewModelScope.launch {
            // Wait for validation to complete
            // Don't proceed if there are errors
            if (_state.value.hasFormErrors) {
                _effect.emit(CreateUserContract.Effect.ShowError("Please fix the errors in the form before submitting"))
                return@launch
            }

            // Submit based on user type
            when (_state.value.userType) {
                UserType.CLIENT -> createClient()
                UserType.AGENT -> createAgent()
                else -> {
                    _effect.emit(CreateUserContract.Effect.ShowError("Invalid user type"))
                }
            }
        }
    }

    /**
     * Create a new client
     */
    private fun createClient() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }

            val client = Client(
                id = UUID.randomUUID().toString(),
                fullName = _state.value.fullName,
                email = _state.value.email,
                phoneNumber = _state.value.phoneNumber,
                createdAt = LocalDateTime.now(),
                status = _state.value.status,
                avatarUrl = null,
                address = _state.value.address,
                meterNumber = _state.value.meterNumber,
                accountNumber = _state.value.accountNumber,
                area = _state.value.area
            )

            when (val result = userRepository.createClient(client)) {
                is OperationResult.Success -> {
                    _state.update { it.copy(isSubmitting = false) }
                    _effect.emit(CreateUserContract.Effect.ShowSuccess("Client created successfully"))
                    _effect.emit(CreateUserContract.Effect.UserCreatedSuccessfully)
                }
                is OperationResult.Failure -> {
                    _state.update { it.copy(isSubmitting = false) }
                    _effect.emit(CreateUserContract.Effect.ShowError(
                        result.message ?: "Failed to create client"
                    ))
                }
                is OperationResult.Error -> {
                    Timber.e(result.throwable, "Error creating client: ${result.message}")
                    _state.update { it.copy(isSubmitting = false) }
                    _effect.emit(CreateUserContract.Effect.ShowError(
                        "An unexpected error occurred while creating client: ${result.message}"
                    ))
                }
            }
        }
    }

    /**
     * Create a new agent
     */
    private fun createAgent() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }

            val agent = Agent(
                id = UUID.randomUUID().toString(),
                fullName = _state.value.fullName,
                email = _state.value.email,
                phoneNumber = _state.value.phoneNumber,
                createdAt = LocalDateTime.now(),
                status = _state.value.status,
                avatarUrl = null,
                employeeId = _state.value.employeeId,
                territories = _state.value.territories
            )

            when (val result = userRepository.createAgent(agent)) {
                is OperationResult.Success -> {
                    _state.update { it.copy(isSubmitting = false) }
                    _effect.emit(CreateUserContract.Effect.ShowSuccess("Agent created successfully"))
                    _effect.emit(CreateUserContract.Effect.UserCreatedSuccessfully)
                }
                is OperationResult.Failure -> {
                    _state.update { it.copy(isSubmitting = false) }
                    _effect.emit(CreateUserContract.Effect.ShowError(
                        result.message ?: "Failed to create agent"
                    ))
                }
                is OperationResult.Error -> {
                    Timber.e(result.throwable, "Error creating agent: ${result.message}")
                    _state.update { it.copy(isSubmitting = false) }
                    _effect.emit(CreateUserContract.Effect.ShowError(
                        "An unexpected error occurred while creating agent: ${result.message}"
                    ))
                }
            }
        }
    }

    /**
     * Reset the form to initial values
     */
    private fun resetForm() {
        _state.update {
            CreateUserContract.State(
                userType = it.userType,
                availableAreas = it.availableAreas,
                availableTerritories = it.availableTerritories
            )
        }
    }
}