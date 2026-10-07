package com.myplanner.app.ui.pete

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            Text(dateLine, color = PeteColors.OnDarkMuted, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                clock,
                color = PeteColors.OnDark,
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text("Hey boss", color = PeteColors.Cyan, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(36.dp))
            PeteGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Up next", color = PeteColors.OnDarkMuted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text(nextTitle, color = PeteColors.OnDark, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.weight(1f))
            PetePillButton(text = "Hey Pete", onClick = onHeyPete, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(14.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (onOpenBrief != null) {
                    PeteGlassPill(text = "Daily brief", onClick = onOpenBrief)
                }
                if (onOpenLegacyHome != null) {
                    PeteGlassPill(text = "Classic planner", onClick = onOpenLegacyHome)
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}
