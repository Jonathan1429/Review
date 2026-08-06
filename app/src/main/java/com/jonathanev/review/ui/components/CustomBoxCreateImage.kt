package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import java.io.File

@Composable
fun CustomBoxCreateImage(
    modifier: Modifier = Modifier,
    uriImage: String
) {
    val context = LocalContext.current

    val imageModel: Any = remember(uriImage) {
        if (uriImage.startsWith("/")) File(uriImage) else uriImage
    }

    key(uriImage) {
        ZoomableAsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageModel)
                .crossfade(true)
                .build(),
            contentDescription = "Vista previa de la imagen",
            modifier = modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
    }
}