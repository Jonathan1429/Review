package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage

@Composable
fun CustomBoxCreateImage(
    modifier: Modifier = Modifier,
    uriImage: String
) {
    val context = LocalContext.current

    key(uriImage) {
        ZoomableAsyncImage(
            model = ImageRequest.Builder(context)
                .data(uriImage)
                .crossfade(true)
                .build(),
            contentDescription = "Vista previa de la imagen",
            modifier = modifier.fillMaxSize()
        )
    }
}