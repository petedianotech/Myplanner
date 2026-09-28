package com.myplanner.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape system — 16–20dp for primary cards/surfaces.
 * Smaller radii for compact controls; pills only for chips.
 */
val MyPlannerShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

object AppShapes {
    val card = RoundedCornerShape(16.dp)
    val cardLarge = RoundedCornerShape(20.dp)
    val button = RoundedCornerShape(12.dp)
    val textField = RoundedCornerShape(12.dp)
    val chip = RoundedCornerShape(50)
    val dialog = RoundedCornerShape(20.dp)
    val bottomSheet = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val listItem = RoundedCornerShape(12.dp)
    val avatar = RoundedCornerShape(50)
    val badge = RoundedCornerShape(50)
}
