package com.jonathanev.review.Data.Model

import androidx.recyclerview.widget.DiffUtil
import com.jonathanev.review.Data.Model.prueba.QuestionContent

class QuestionImageDiffCallback : DiffUtil.ItemCallback<QuestionContent.Image>() {
    override fun areItemsTheSame(
        oldItem: QuestionContent.Image,
        newItem: QuestionContent.Image
    ) = oldItem.decodedPath == newItem.decodedPath && oldItem.encodedPath == newItem.encodedPath

    override fun areContentsTheSame(
        oldItem: QuestionContent.Image,
        newItem: QuestionContent.Image
    ) = oldItem == newItem
}