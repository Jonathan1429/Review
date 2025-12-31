package com.jonathanev.review.presentation.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.jonathanev.review.presentation.model.QuestionContentDomain
import com.jonathanev.review.presentation.model.QuestionContentUi

class QuestionImageDiffCallback : DiffUtil.ItemCallback<QuestionContentUi.Image>() {
    override fun areItemsTheSame(
        oldItem: QuestionContentUi.Image,
        newItem: QuestionContentUi.Image
    ) = oldItem.uri == newItem.uri

    override fun areContentsTheSame(
        oldItem: QuestionContentUi.Image,
        newItem: QuestionContentUi.Image
    ) = oldItem == newItem
}