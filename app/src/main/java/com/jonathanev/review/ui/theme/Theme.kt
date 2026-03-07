package com.jonathanev.review.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = LightPrimary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightOnPrimary
)

val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkOnPrimary
)

val cardStepBackground: Color
    @Composable
    get() = if (isSystemInDarkTheme())
        ContentsDark
    else
        ContentsLight

val textColor: Color
    @Composable
    get() = if (isSystemInDarkTheme())
        White
    else
        Black