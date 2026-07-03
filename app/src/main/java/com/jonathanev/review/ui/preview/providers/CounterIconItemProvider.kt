package com.jonathanev.review.ui.preview.providers

import androidx.annotation.DrawableRes
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.R

data class CounterItem(
    @param:DrawableRes val icon: Int,
    val count: Int
)

class CounterIconItemProvider(): PreviewParameterProvider<CounterItem> {
    override val values: Sequence<CounterItem>
        get() = sequenceOf(
            CounterItem(
                icon = R.drawable.ic_file,
                count = 2
            ),
            CounterItem(
                icon = R.drawable.ic_image,
                count = 2
            )
        )
}