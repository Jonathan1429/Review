package com.jonathanev.review.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import android.graphics.Color as AndroidColor

val LightColors = lightColorScheme(
    primary = HardColorButton,
    background = LightBackground,
    surface = LightSurface,
    onSurface = Black,
    onPrimary = LightOnPrimary
)

val DarkColors = darkColorScheme(
    primary = HardColorButton,
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
        CardBackgroundColor
    else
        CardBackgroundColor.lighten(0.9f)

val dialogBackground: Color
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

@Composable
fun getButtonBackgroundBrush(): Brush {
    return Brush.linearGradient(
        colors = listOf(LightColorButton, HardColorButton)
    )
}

@Composable
fun getCardContainerColor(): Color {
    return if (isSystemInDarkTheme()) {
        ContainerCard
    } else {
        ContainerCard.lighten(0.8f)
    }
}

@Composable
fun getColorSubtitle(): Color {
    return if (isSystemInDarkTheme()) {
        ColorDarkSubtitle
    } else {
        ColorLightSubtitle
    }
}

@Composable
fun getColorTitleCard(): Color {
    return if (isSystemInDarkTheme()) {
        ColorTitleCard
    } else {
        ColorTitleCard.darken(0.6f)
    }
}

fun Color.darken(factor: Float = 0.3f): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(this.toArgb(), hsv)

    hsv[2] = (hsv[2] * (1f - factor)).coerceIn(0f, 1f)

    return Color(AndroidColor.HSVToColor(hsv))
}

fun Color.lighten(factor: Float = 0.3f): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(this.toArgb(), hsv)

    val validFactor = factor.coerceIn(0f, 1f)

    hsv[2] = (hsv[2] + (1f - hsv[2]) * validFactor).coerceIn(0f, 1f)

    hsv[1] = (hsv[1] * (1f - validFactor)).coerceIn(0f, 1f)

    return Color(AndroidColor.HSVToColor(hsv))
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