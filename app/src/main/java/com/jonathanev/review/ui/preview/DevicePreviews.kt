package com.jonathanev.review.ui.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Light Mode - Phone",
    group = "UI Devices",
    device = Devices.PIXEL_4,
    showSystemUi = true,
    showBackground = true
)
@Preview(
    name = "Dark Mode - Phone",
    group = "UI Devices",
    device = Devices.PIXEL_4,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
//@Preview(name = "Idioma Español", locale = "es")
annotation class DevicePreviews

@Preview(
    name = "Light Mode",
    group = "UI Devices",
    showBackground = true
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
annotation class ComponentsPreviews