package com.jonathanev.review.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jonathanev.review.ui.components.CustomBoxCreateText
import com.jonathanev.review.ui.components.OptionsCreateText
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.degradientColor

@Preview(showBackground = true)
@Composable
fun PreviewCreateTextScreen() {
    ReviewTheme {
        CreateTextScreen()
    }
}

@Composable
fun CreateTextScreen(
    modifier: Modifier = Modifier,
    onSelectColorClick: () -> Unit = {},
    onClearColorClick: () -> Unit = {}
) {
    var textValue by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(42.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = degradientColor
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                OptionsCreateText(textValue)
                CustomBoxCreateText(
                    textValue = textValue,
                    onTextValueChange = { actualText -> textValue = actualText }
                )
            }
        }
    }
}