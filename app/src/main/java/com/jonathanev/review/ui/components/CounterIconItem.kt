package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.CounterIconItemProvider
import com.jonathanev.review.ui.preview.providers.CounterItem
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.TextColorSecondary

@DevicePreviews
@Composable
fun PreviewCounterIconItem(
    @PreviewParameter(CounterIconItemProvider ::class) data: CounterItem
){
    ReviewTheme {
        CounterIconItem(data.icon, data.count)
    }
}

@Composable
fun CounterIconItem(icon: Int, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = TextColorSecondary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = count.toString(),
            color = TextColorSecondary,
            fontSize = 13.sp
        )
    }
}