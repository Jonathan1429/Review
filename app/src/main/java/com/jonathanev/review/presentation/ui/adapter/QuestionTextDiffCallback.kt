package com.jonathanev.review.presentation.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.jonathanev.review.presentation.model.QuestionContentUi

class QuestionTextDiffCallback: DiffUtil.ItemCallback<QuestionContentUi.Text>() {
    override fun areItemsTheSame(
        oldItem: QuestionContentUi.Text,
        newItem: QuestionContentUi.Text
    ): Boolean {
        return oldItem.text ==  newItem.text
    }

    override fun areContentsTheSame(
        oldItem: QuestionContentUi.Text,
        newItem: QuestionContentUi.Text
    ): Boolean {
        return oldItem == newItem
    }

}