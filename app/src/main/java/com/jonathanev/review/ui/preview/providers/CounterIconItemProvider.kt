package com.jonathanev.review.ui.preview.providers

import androidx.annotation.DrawableRes
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.R

data class CounterItem(
    @param:DrawableRes val icon: Int,
    val count: String
)

class CounterIconItemProvider(): PreviewParameterProvider<CounterItem> {
    override val values: Sequence<CounterItem>
        get() = sequenceOf(
            CounterItem(
                icon = R.drawable.ic_file,
                count = "3"
            ),
            CounterItem(
                icon = R.drawable.ic_image,
                count = "2"
            )
        )

    override fun getDisplayName(index: Int): String? {
        return when(index){
            0 -> "ic_file"
            1 -> "ic_image"
            else -> super.getDisplayName(index)
        }
    }
}