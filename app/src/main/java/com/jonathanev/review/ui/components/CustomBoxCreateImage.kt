package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import com.jonathanev.review.R
import com.jonathanev.review.domain.constants.Constants
import me.saket.telephoto.zoomable.ZoomableImageState
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import java.io.File

@Composable
fun CustomBoxCreateImage(
    modifier: Modifier = Modifier,
    uriImage: String,
    contentScale: ContentScale = ContentScale.Fit,
    state: ZoomableImageState = rememberZoomableImageState(
        rememberZoomableState()
    )
) {
    val context = LocalContext.current

    val imageModel: Any = remember(uriImage) {
        if (uriImage == Constants.IMAGE_CORRUPT) {
            R.drawable.archivo_corrupto
        } else if (uriImage.startsWith("/")) {
            File(uriImage)
        } else {
            uriImage
        }
    }

    ZoomableAsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageModel)
            .crossfade(true)
            .build(),
        contentDescription = "Vista previa de la imagen",
        modifier = modifier.fillMaxSize(),
        state = state,
        contentScale = contentScale
    )
}