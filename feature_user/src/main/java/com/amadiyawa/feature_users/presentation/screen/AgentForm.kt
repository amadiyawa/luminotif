package com.amadiyawa.feature_users.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.amadiyawa.feature_base.presentation.compose.composable.TextTitleMedium
import com.amadiyawa.feature_user.R
import com.amadiyawa.feature_users.presentation.contract.CreateUserContract

/**
 * Form for creating a new agent
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AgentForm(
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

        // Employee ID
        OutlinedTextField(
            value = state.employeeId,
            onValueChange = { onAction(CreateUserContract.Action.UpdateEmployeeId(it)) },
            label = { Text(stringResource(R.string.employee_id)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            isError = state.employeeIdError != null,
            supportingText = {
                if (state.employeeIdError != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = state.employeeIdError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )

        // Territories Section
        TextTitleMedium(
            text = stringResource(R.string.assigned_territories),
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        // Selected territories display
        if (state.territories.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.territories.forEach { territory ->
                            FilterChip(
                                selected = true,
                                onClick = {
                                    onAction(CreateUserContract.Action.RemoveTerritory(territory))
                                },
                                label = { Text(territory) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.remove)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Error for territories
        if (state.territoriesError != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = state.territoriesError ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Button to select territories
        Button(
            onClick = { onAction(CreateUserContract.Action.ShowTerritorySelectionDialog) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.select_territories))
        }
    }
}

/**
 * Dialog for selecting territories for an agent
 */
@Composable
fun TerritorySelectionDialog(
    selectedTerritories: List<String>,
    availableTerritories: List<String>,
    onTerritoriesSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val tempSelectedTerritories = remember { mutableStateOf(selectedTerritories.toMutableList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_territories_title)) },
        text = {
            Column {
                if (availableTerritories.isEmpty()) {
                    Text(
                        stringResource(R.string.no_territories_available),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(availableTerritories) { territory ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val currentList = tempSelectedTerritories.value
                                        if (currentList.contains(territory)) {
                                            currentList.remove(territory)
                                        } else {
                                            currentList.add(territory)
                                        }
                                        tempSelectedTerritories.value = currentList
                                    }
                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = tempSelectedTerritories.value.contains(territory),
                                    onCheckedChange = { checked ->
                                        val currentList = tempSelectedTerritories.value
                                        if (checked) {
                                            if (!currentList.contains(territory)) {
                                                currentList.add(territory)
                                            }
                                        } else {
                                            currentList.remove(territory)
                                        }
                                        tempSelectedTerritories.value = currentList
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = territory,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onTerritoriesSelected(tempSelectedTerritories.value)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}