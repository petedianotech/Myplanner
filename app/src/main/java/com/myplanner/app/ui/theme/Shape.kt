package com.myplanner.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val MyPlannerShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

object AppShapes {
    val card = RoundedCornerShape(24.dp)
    val cardLarge = RoundedCornerShape(28.dp)
    val cardXLarge = RoundedCornerShape(32.dp)
    val button = RoundedCornerShape(50)
    val pill = RoundedCornerShape(50)
    val textField = RoundedCornerShape(28.dp)
    val chip = RoundedCornerShape(50)
    val dialog = RoundedCornerShape(28.dp)
    val bottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val listItem = RoundedCornerShape(20.dp)
    val avatar = RoundedCornerShape(50)
    val badge = RoundedCornerShape(50)
    val glass = RoundedCornerShape(24.dp)
    val orb = RoundedCornerShape(50)
}
