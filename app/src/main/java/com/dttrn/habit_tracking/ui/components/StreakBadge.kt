package com.dttrn.habit_tracking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StreakBadge(
    streak: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    if (streak <= 0) return

    val bgColor = when {
        streak >= 30 -> Color(0xFFFF6B00)
        streak >= 7 -> Color(0xFFFF9500)
        else -> Color(0xFFFFAD33)
    }

    Row(
        modifier = modifier
            .background(
                color = bgColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = if (compact) 2.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🔥",
            fontSize = if (compact) 12.sp else 14.sp
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = if (compact) "$streak" else "$streak ngày",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = bgColor,
            fontSize = if (compact) 11.sp else 12.sp
        )
    }
}
