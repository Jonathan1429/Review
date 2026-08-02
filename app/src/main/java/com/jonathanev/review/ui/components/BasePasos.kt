package com.jonathanev.review.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.preview.providers.PasoPreviewData
import com.jonathanev.review.ui.preview.providers.PasosDataProvider
import com.jonathanev.review.ui.theme.Inter
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.getCardContainerColor
import com.jonathanev.review.ui.theme.getColorSubtitle

@ComponentsPreviews
@Composable
fun PreviewBasePasos(
    @PreviewParameter(PasosDataProvider::class) data: PasoPreviewData
) {
    ReviewTheme {
        BasePasos(
            image = data.image,
            title = data.title,
            description = data.description
        )
    }
}

@Composable
fun BasePasos(image: Int, title: Int, description: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                color = getCardContainerColor(),
                shape = RoundedCornerShape(10.dp)
            ) //Contents
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(image),
                contentDescription = "añadir carpeta",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(Modifier.size(10.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = stringResource(title),
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = Inter,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(description),
                color = getColorSubtitle(),
                fontSize = 15.sp
            )
        }
    }
}