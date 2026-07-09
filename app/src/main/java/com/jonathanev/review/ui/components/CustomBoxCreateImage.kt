package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.jonathanev.review.R
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage

@Preview(showBackground = true)
@Composable
fun TestCoil() {
    AsyncImage(
        model = R.drawable.no_files,
        contentDescription = null
    )
}

@Composable
fun CustomBoxCreateImage(
    uriImage: String
) {
    ZoomableAsyncImage(
        model = uriImage.toUri(),
        contentDescription = "Vista previa de la imagen",
        modifier = Modifier.fillMaxSize()
    )
}