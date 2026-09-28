package com.myplanner.app.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.myplanner.app.R
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.AppTextButton
import com.myplanner.app.ui.theme.AppDimens
import com.myplanner.app.ui.theme.MyPlannerTheme
import com.myplanner.app.ui.theme.Spacing

@Composable
fun FirstReminderPromptScreen(
    onCreateReminder: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.xxxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.huge))
            Icon(
                imageVector = Icons.Outlined.Alarm,
                contentDescription = null,
                modifier = Modifier.size(AppDimens.iconXl * 1.25f),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(Spacing.xxl))
            Text(
                text = stringResource(R.string.first_reminder_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = stringResource(R.string.first_reminder_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spacing.sm)
            )
            Spacer(modifier = Modifier.weight(1f, fill = true))
            Spacer(modifier = Modifier.height(Spacing.section))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppPrimaryButton(
                    text = stringResource(R.string.first_reminder_create),
                    onClick = onCreateReminder,
                    modifier = Modifier.fillMaxWidth()
                )
                AppTextButton(
                    text = stringResource(R.string.first_reminder_skip),
                    onClick = onSkip
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FirstReminderPromptPreview() {
    MyPlannerTheme {
        FirstReminderPromptScreen(onCreateReminder = {}, onSkip = {})
    }
}
