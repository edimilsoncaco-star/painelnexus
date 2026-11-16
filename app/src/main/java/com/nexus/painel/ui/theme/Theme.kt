package com.nexus.painel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NexusBlack = Color(0xFF000000)
val NexusSurface = Color(0xFF0B0B0B)
val NexusSurfaceHigh = Color(0xFF151515)
val NeonRed = Color(0xFFFF1A1A)
val NeonRedDark = Color(0xFFB30000)
val NeonRedSoft = Color(0xFFFF5252)
val NexusWhite = Color(0xFFF5F5F5)
val NexusGray = Color(0xFF9A9A9A)
val NexusGreen = Color(0xFF2BFF88)

private val NexusColorScheme = darkColorScheme(
    primary = NeonRed,
    onPrimary = NexusWhite,
    secondary = NeonRedDark,
    onSecondary = NexusWhite,
    background = NexusBlack,
    onBackground = NexusWhite,
    surface = NexusSurface,
    onSurface = NexusWhite,
    error = NeonRedSoft,
    onError = NexusBlack
)

@Composable
fun NexusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NexusColorScheme,
        content = content
    )
}
