package com.jonathanev.review.data.Model

import androidx.recyclerview.widget.DiffUtil
import com.jonathanev.review.data.Model.prueba.QuestionContent

class QuestionTextDiffCallback: DiffUtil.ItemCallback<QuestionContent.Text>() {
    override fun areItemsTheSame(
        oldItem: QuestionContent.Text,
        newItem: QuestionContent.Text
    ): Boolean {
        return oldItem.text ==  newItem.text
    }

    override fun areContentsTheSame(
        oldItem: QuestionContent.Text,
        newItem: QuestionContent.Text
    ): Boolean {
        return oldItem == newItem
    }

}