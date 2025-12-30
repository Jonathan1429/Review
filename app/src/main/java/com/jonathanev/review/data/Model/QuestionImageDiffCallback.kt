package com.jonathanev.review.data.Model

import androidx.recyclerview.widget.DiffUtil
import com.jonathanev.review.data.Model.prueba.QuestionContent

class QuestionImageDiffCallback : DiffUtil.ItemCallback<QuestionContent.Image>() {
    override fun areItemsTheSame(
        oldItem: QuestionContent.Image,
        newItem: QuestionContent.Image
    ) = oldItem.uri == newItem.uri

    override fun areContentsTheSame(
        oldItem: QuestionContent.Image,
        newItem: QuestionContent.Image
    ) = oldItem == newItem
}