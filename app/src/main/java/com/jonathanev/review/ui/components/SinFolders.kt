package com.jonathanev.review.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.theme.Inter
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.getCardContainerColor
import com.jonathanev.review.ui.theme.getColorSubtitle

@ComponentsPreviews
@Composable
fun PreviewSinFolders() {
    ReviewTheme {
        SinFolders()
    }
}

@Composable
fun SinFolders() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(getCardContainerColor())
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_folder_off),
                contentDescription = "imagen folders",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Text(
            modifier = Modifier.padding(top = 10.dp),
            text = stringResource(R.string.lblWithoutFolders),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = Inter,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            modifier = Modifier.padding(top = 10.dp),
            text = stringResource(R.string.lblWithoutFoldersDes),
            color = getColorSubtitle(),
            textAlign = TextAlign.Center
        )
    }
}