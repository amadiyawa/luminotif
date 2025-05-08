package com.amadiyawa.feature_billing.presentation.screen.billingdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.amadiyawa.feature_base.presentation.compose.composable.TextTitleLarge
import com.amadiyawa.feature_base.presentation.compose.composable.Toolbar
import com.amadiyawa.feature_base.presentation.compose.composable.ToolbarParams
import com.amadiyawa.feature_billing.R

@Composable
fun InvoiceDetailScreen(
    onBackClick: () -> Unit,
    invoiceId: String,
) {
    Scaffold(
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = { Toolbar(params = ToolbarParams(title = "Users")) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
            TextTitleLarge(text = stringResource(R.string.bills) + " $invoiceId")
        }
    }
}