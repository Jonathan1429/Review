package com.jonathanev.review.Data.Model

import androidx.recyclerview.widget.DiffUtil
import com.jonathanev.review.Data.Model.prueba.QuestionContent

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