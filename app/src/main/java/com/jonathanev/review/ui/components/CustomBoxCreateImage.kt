package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage

@Composable
fun CustomBoxCreateImage(uriImage: String) {
    ZoomableAsyncImage(
        model = uriImage,
        contentDescription = "Vista previa de la imagen",
        modifier = Modifier.fillMaxSize()
    )
}