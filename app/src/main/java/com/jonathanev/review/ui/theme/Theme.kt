package com.jonathanev.review.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
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

val degradientColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) {
        Black
    } else {
        White
    }

object ComponentTheme {
    // Tu color primario base
    val PrimaryTeal = Color(0xFF019486)

    // Colores base Modo Oscuro
    private val ContentsDark = Color(0xFF1F2937)
    private val BorderSelectedDark = Color(0xFFC3F5FF)

    // Nuevos Colores base Modo Claro (Inyectados con tu primario)
    private val TealBgLightNormal = Color(0xFFF0FDFA)  // Blanco-Teal vivo
    private val TealBorderLightNormal = Color(0xFFCCFBF1) // Borde suave vivo

    @Composable
    fun getSelectedBorderBrush(): Brush {
        return if (isSystemInDarkTheme()) {
            Brush.verticalGradient(listOf(Color.Transparent, BorderSelectedDark))
        } else {
            // Modo Claro: De transparente a tu verde primario real
            Brush.verticalGradient(listOf(Color.Transparent, PrimaryTeal))
        }
    }

    @Composable
    fun getSelectedBackgroundBrush(): Brush {
        return if (isSystemInDarkTheme()) {
            Brush.linearGradient(listOf(ContentsDark, Color(0xFF1E293B)))
        } else {
            // Modo Claro Seleccionado: Degradado sutil entre tonos de tu marca
            Brush.linearGradient(listOf(TealBgLightNormal, Color(0xFFE6F4F1)))
        }
    }

    @Composable
    fun getUnselectedBorderColor(): Color {
        return if (isSystemInDarkTheme()) Color(0xFF374151) else TealBorderLightNormal
    }

    @Composable
    fun getUnselectedBackgroundColor(): Color {
        return if (isSystemInDarkTheme()) ContentsDark else TealBgLightNormal
    }
}