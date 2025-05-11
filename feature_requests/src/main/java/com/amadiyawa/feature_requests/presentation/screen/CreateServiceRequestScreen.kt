package com.amadiyawa.feature_requests.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amadiyawa.feature_requests.presentation.viewmodel.CreateServiceRequestViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.amadiyawa.feature_base.presentation.compose.composable.Toolbar
import com.amadiyawa.feature_base.presentation.compose.composable.ToolbarParams
import com.amadiyawa.feature_requests.R
import com.amadiyawa.feature_requests.domain.model.RequestCategory
import com.amadiyawa.feature_requests.domain.model.RequestPriority
import com.amadiyawa.feature_requests.presentation.state.CreateServiceRequestAction
import com.amadiyawa.feature_requests.presentation.state.CreateServiceRequestEvent
import com.amadiyawa.feature_requests.presentation.state.CreateServiceRequestUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceRequestScreen(
    onBackClick: () -> Unit,
    onRequestCreated: (String) -> Unit,
    viewModel: CreateServiceRequestViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateServiceRequestEvent.NavigateBack -> onBackClick()
                is CreateServiceRequestEvent.NavigateToDetail -> onRequestCreated(event.requestId)
                is CreateServiceRequestEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Toolbar(
                params = ToolbarParams(
                    title = stringResource(R.string.create_request),
                    showBackButton = true,
                    onBackPressed = onBackClick
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = formState.title,
                    onValueChange = {
                        viewModel.onAction(CreateServiceRequestAction.UpdateTitle(it))
                    },
                    label = { Text(stringResource(R.string.title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = formState.description,
                    onValueChange = {
                        viewModel.onAction(CreateServiceRequestAction.UpdateDescription(it))
                    },
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )

                CategoryDropdown(
                    selectedCategory = formState.category,
                    onCategorySelected = { category ->
                        viewModel.onAction(CreateServiceRequestAction.UpdateCategory(category))
                    }
                )

                PrioritySelector(
                    selectedPriority = formState.priority,
                    onPrioritySelected = { priority ->
                        viewModel.onAction(CreateServiceRequestAction.UpdatePriority(priority))
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.onAction(CreateServiceRequestAction.SubmitRequest)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = formState.isValid && uiState !is CreateServiceRequestUiState.Submitting
                ) {
                    if (uiState is CreateServiceRequestUiState.Submitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.submit_request))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedCategory: RequestCategory?,
    onCategorySelected: (RequestCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCategory?.let { getCategoryString(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            RequestCategory.values().forEach { category ->
                DropdownMenuItem(
                    text = { Text(getCategoryString(category)) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun getCategoryString(category: RequestCategory): String {
    return when (category) {
        RequestCategory.CONNECTION_ISSUE -> stringResource(R.string.connection_issue)
        RequestCategory.BILLING_QUERY -> stringResource(R.string.billing_query)
        RequestCategory.METER_PROBLEM -> stringResource(R.string.meter_problem)
        RequestCategory.POWER_OUTAGE -> stringResource(R.string.power_outage)
        RequestCategory.OTHER -> stringResource(R.string.other)
    }
}

@Composable
fun PrioritySelector(
    selectedPriority: RequestPriority,
    onPrioritySelected: (RequestPriority) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.priority),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RequestPriority.values().forEach { priority ->
                FilterChip(
                    selected = selectedPriority == priority,
                    onClick = { onPrioritySelected(priority) },
                    label = { Text(getPriorityString(priority)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (priority) {
                            RequestPriority.LOW -> MaterialTheme.colorScheme.secondaryContainer
                            RequestPriority.MEDIUM -> MaterialTheme.colorScheme.primaryContainer
                            RequestPriority.HIGH -> MaterialTheme.colorScheme.tertiaryContainer
                            RequestPriority.URGENT -> MaterialTheme.colorScheme.errorContainer
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun getPriorityString(priority: RequestPriority): String {
    return when (priority) {
        RequestPriority.LOW -> stringResource(R.string.low)
        RequestPriority.MEDIUM -> stringResource(R.string.medium)
        RequestPriority.HIGH -> stringResource(R.string.high)
        RequestPriority.URGENT -> stringResource(R.string.urgent)
    }
}