package com.quotecards.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Theme Colors
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Dark Theme Colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

private val LightCardColors = listOf(
    Color(0xFFFFF3E0), // Warm cream
    Color(0xFFE3F2FD), // Light blue
    Color(0xFFF3E5F5), // Light purple
    Color(0xFFE8F5E9), // Light green
    Color(0xFFFCE4EC), // Light pink
    Color(0xFFFFFDE7), // Light yellow
    Color(0xFFE0F7FA), // Light cyan
    Color(0xFFFBE9E7), // Light orange
)

private val DarkCardColors = listOf(
    Color(0xFF3B3329), // Muted warm brown
    Color(0xFF293640), // Muted slate blue
    Color(0xFF3A3042), // Muted plum
    Color(0xFF283B2D), // Muted forest
    Color(0xFF422E36), // Muted rose
    Color(0xFF403D28), // Muted amber
    Color(0xFF263B3D), // Muted teal
    Color(0xFF423129), // Muted orange brown
)

@Composable
fun appCardColors(): List<Color> {
    return if (isSystemInDarkTheme()) DarkCardColors else LightCardColors
}
