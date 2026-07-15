package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage

@Composable
fun CustomBoxCreateImage(
    modifier: Modifier = Modifier,
    uriImage: String
) {
    ZoomableAsyncImage(
        model = uriImage.toUri(),
        contentDescription = "Vista previa de la imagen",
        modifier = modifier.fillMaxSize()
    )
}