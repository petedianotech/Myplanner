package com.myplanner.app.ui.pete

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.ui.components.PeteAmbientBackground
import com.myplanner.app.ui.components.PeteGlassCard
import com.myplanner.app.ui.components.PeteGlassPill
import com.myplanner.app.ui.components.PetePillButton
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.home.PlanItem
import com.myplanner.app.ui.theme.PeteColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AmbientHomeScreen(
    onHeyPete: () -> Unit,
    onOpenItem: (PlanItem) -> Unit = {},
    onOpenBrief: (() -> Unit)? = null,
    onOpenLegacyHome: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    onOpenSearch: (() -> Unit)? = null,
    onOpenPlans: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clock = remember {
        LocalTime.now().format(DateTimeFormatter.ofPattern("H:mm", Locale.getDefault()))
    }
    val dateLine = remember {
        val day = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault()))
        val mood = when (LocalTime.now().hour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            in 18..21 -> "Quiet evening"
            else -> "Late night"
        }
        "$day · $mood"
    }
    val nextTitle = state.todayItems.firstOrNull { !it.completed }?.title
        ?: state.upcomingItems.firstOrNull { !it.completed }?.title
        ?: "Nothing scheduled — tell Pete what to do"

    Box(modifier = modifier.fillMaxSize()) {
        PeteAmbientBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(36.dp))
            Text("petediano", color = PeteColors.Cyan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(dateLine, color = PeteColors.OnDarkMuted, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                clock,
                color = PeteColors.OnDark,
                fontSize = 68.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text("Hey boss", color = PeteColors.OnDarkSoft, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(28.dp))
            PeteGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Up next", color = PeteColors.OnDarkMuted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text(nextTitle, color = PeteColors.OnDark, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
            ) {
                if (onOpenPlans != null) {
                    PeteGlassPill(text = "Plans", onClick = onOpenPlans)
                }
                if (onOpenSearch != null) {
                    PeteGlassPill(text = "Search", onClick = onOpenSearch)
                }
                if (onOpenSettings != null) {
                    PeteGlassPill(text = "Settings", onClick = onOpenSettings)
                }
            }
            Spacer(Modifier.weight(1f))
            PetePillButton(text = "Hey Pete", onClick = onHeyPete, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            if (onOpenBrief != null) {
                PeteGlassPill(text = "Live call", onClick = onOpenBrief)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
