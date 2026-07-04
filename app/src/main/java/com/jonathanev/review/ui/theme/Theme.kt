package com.jonathanev.review.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = ColorBotones,
    background = LightBackground,
    surface = LightSurface,
    onSurface = Black,
    onPrimary = LightOnPrimary
)

val DarkColors = darkColorScheme(
    primary = ColorBotones,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = White,
    onPrimary = LightOnPrimary
)

val cardStepBackground: Color
    @Composable
    get() = if (isSystemInDarkTheme())
        ContentsDark
    else
        ContentsLight

val iconBackground: Color
    @Composable
    get() = if (isSystemInDarkTheme())
        ContentInContent
    else
        ContentsLight

val cardListBackground: Color
    @Composable
    get() = if (isSystemInDarkTheme())
        ContentsDark
    else
        ContentsLight


val baseColor: Color
    @Composable
    get() = if (isSystemInDarkTheme())
        White
    else
        Black