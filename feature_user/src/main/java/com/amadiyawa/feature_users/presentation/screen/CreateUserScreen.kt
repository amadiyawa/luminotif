package com.amadiyawa.feature_users.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.amadiyawa.feature_base.domain.util.UserStatus
import com.amadiyawa.feature_base.presentation.compose.composable.TextTitleMedium
import com.amadiyawa.feature_base.presentation.compose.composable.Toolbar
import com.amadiyawa.feature_base.presentation.compose.composable.ToolbarParams
import com.amadiyawa.feature_user.R
import com.amadiyawa.feature_users.presentation.contract.CreateUserContract
import com.amadiyawa.feature_users.presentation.navigation.UserNavigationApiComplete.UserType
import com.amadiyawa.feature_users.presentation.viewmodel.CreateUserViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

/**
 * Screen for creating a new user (client or agent)
 */
@Composable
fun CreateUserScreen(
    viewModel: CreateUserViewModel = koinViewModel(),
    userType: UserType,
    onBackClick: () -> Unit,
    onUserCreated: () -> Unit
) {

    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }

    // Initialize the ViewModel with the user type
    LaunchedEffect(userType) {
        viewModel.initialize(userType)
    }

    // Process side effects
    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CreateUserContract.Effect.NavigateBack -> {
                    onBackClick()
                }
                is CreateUserContract.Effect.UserCreatedSuccessfully -> {
                    onUserCreated()
                }
                is CreateUserContract.Effect.ShowError -> {
                    // In a real app, you'd show a Snackbar or Toast
                }
                is CreateUserContract.Effect.ShowSuccess -> {
                    // In a real app, you'd show a Snackbar or Toast
                }
            }
        }
    }

    // Territory selection dialog
    if (state.showTerritorySelectionDialog) {
        TerritorySelectionDialog(
            selectedTerritories = state.territories,
            availableTerritories = state.availableTerritories,
            onTerritoriesSelected = { territories ->
                viewModel.handleAction(CreateUserContract.Action.UpdateTerritories(territories))
            },
            onDismiss = {
                viewModel.handleAction(CreateUserContract.Action.HideTerritorySelectionDialog)
            }
        )
    }

    Scaffold(
        topBar = {
            Toolbar(
                params = ToolbarParams(
                    showBackButton = true,
                    title = stringResource(
                        if (userType == UserType.CLIENT)
                            R.string.create_new_client
                        else
                            R.string.create_new_agent
                    ),
                    onBackPressed = {
                        viewModel.handleAction(CreateUserContract.Action.NavigateBack)
                    },
                ),
                backgroundColor = Color.Transparent
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Form content
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else -> {
                        // Create Form based on user type
                        if (userType == UserType.CLIENT) {
                            ClientForm(
                                state = state,
                                onAction = viewModel::handleAction,
                                focusRequester = focusRequester
                            )
                        } else if (userType == UserType.AGENT) {
                            AgentForm(
                                state = state,
                                onAction = viewModel::handleAction,
                                focusRequester = focusRequester
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit button
                        Button(
                            onClick = {
                                viewModel.handleAction(CreateUserContract.Action.SubmitForm)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isSubmitting
                        ) {
                            if (state.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Text(
                                text = stringResource(
                                    if (state.isSubmitting)
                                        R.string.creating
                                    else
                                        R.string.create_user
                                ),
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Reset button
                        TextButton(
                            onClick = {
                                viewModel.handleAction(CreateUserContract.Action.ResetForm)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isSubmitting
                        ) {
                            Text(stringResource(R.string.reset_form))
                        }

                        // Add extra padding at the bottom for better scrolling
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            // Show submitting overlay when needed
            if (state.isSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * Form for creating a new client
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientForm(
    state: CreateUserContract.State,
    onAction: (CreateUserContract.Action) -> Unit,
    focusRequester: FocusRequester
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section: Basic Information
        TextTitleMedium(
            text = stringResource(R.string.basic_information),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Name
        OutlinedTextField(
            value = state.fullName,
            onValueChange = { onAction(CreateUserContract.Action.UpdateFullName(it)) },
            label = { Text(stringResource(R.string.full_name)) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .padding(bottom = 8.dp),
            isError = state.fullNameError != null,
            supportingText = {
                if (state.fullNameError != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = state.fullNameError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )

        // Email
        OutlinedTextField(
            value = state.email,
            onValueChange = { onAction(CreateUserContract.Action.UpdateEmail(it)) },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            isError = state.emailError != null,
            supportingText = {
                if (state.emailError != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = state.emailError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )

        // Phone
        OutlinedTextField(
            value = state.phoneNumber,
            onValueChange = { onAction(CreateUserContract.Action.UpdatePhoneNumber(it)) },
            label = { Text(stringResource(R.string.phone_number)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            isError = state.phoneNumberError != null,
            supportingText = {
                if (state.phoneNumberError != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = state.phoneNumberError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )

        // Status dropdown
        var expandedStatus by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedStatus,
            onExpandedChange = { expandedStatus = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            OutlinedTextField(
                value = state.status.name.replace("_", " ").capitalize(),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.status)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expandedStatus,
                onDismissRequest = { expandedStatus = false }
            ) {
                UserStatus.values().forEach { status ->
                    DropdownMenuItem(
                        text = {
                            Text(status.name.replace("_", " ").capitalize())
                        },
                        onClick = {
                            onAction(CreateUserContract.Action.UpdateStatus(status))
                            expandedStatus = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section: Client Information
        TextTitleMedium(
            text = stringResource(R.string.client_information),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Address
        OutlinedTextField(
            value = state.address,
            onValueChange = { onAction(CreateUserContract.Action.UpdateAddress(it)) },
            label = { Text(stringResource(R.string.address)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            isError = state.addressError != null,
            supportingText = {
                if (state.addressError != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = state.addressError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )

        // Area dropdown
        var expandedArea by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedArea,
            onExpandedChange = { expandedArea = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = state.area,
                onValueChange = { /* Handled by dropdown */ },
                readOnly = true,
                label = { Text(stringResource(R.string.area)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedArea) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = state.areaError != null,
                supportingText = {
                    if (state.areaError != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = state.areaError ?: "",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )

            ExposedDropdownMenu(
                expanded = expandedArea,
                onDismissRequest = { expandedArea = false }
            ) {
                state.availableAreas.forEach { area ->
                    DropdownMenuItem(
                        text = { Text(area) },
                        onClick = {
                            onAction(CreateUserContract.Action.UpdateArea(area))
                            expandedArea = false
                        }
                    )
                }
            }
        }

        // Meter Number
        OutlinedTextField(
            value = state.meterNumber,
            onValueChange = { onAction(CreateUserContract.Action.UpdateMeterNumber(it)) },
            label = { Text(stringResource(R.string.meter_number)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            isError = state.meterNumberError != null,
            supportingText = {
                if (state.meterNumberError != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = state.meterNumberError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )

        // Account Number
        OutlinedTextField(
            value = state.accountNumber,
            onValueChange = { onAction(CreateUserContract.Action.UpdateAccountNumber(it)) },
            label = { Text(stringResource(R.string.account_number)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            isError = state.accountNumberError != null,
            supportingText = {
                if (state.accountNumberError != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = state.accountNumberError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )
    }
}

/**
 * Extension function to capitalize the first letter of each word in a string
 */
fun String.capitalize(): String {
    return split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}