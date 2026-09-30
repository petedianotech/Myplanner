package com.myplanner.app.ui.foundation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.myplanner.app.R
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.theme.Spacing

@Composable
fun FoundationScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = Spacing.screenHorizontal)
        ) {
            PageHeader(
                title = stringResource(R.string.foundation_title),
                subtitle = stringResource(R.string.foundation_subtitle)
            )
            Text(
                text = "Design system gallery (dev)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.lg)
            )
        }
    }
}

@Composable
fun DesignSystemScreen() {
    FoundationScreen()
}
