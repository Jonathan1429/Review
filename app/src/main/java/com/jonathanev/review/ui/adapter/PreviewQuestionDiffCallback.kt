package com.jonathanev.review.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.jonathanev.review.presentation.model.PreviewQuestionUi

class PreviewQuestionDiffCallback: DiffUtil.ItemCallback<PreviewQuestionUi>() {
    override fun areItemsTheSame(
        oldItem: PreviewQuestionUi,
        newItem: PreviewQuestionUi
    ): Boolean {
        return oldItem.question == newItem.question
    }

    override fun areContentsTheSame(
        oldItem: PreviewQuestionUi,
        newItem: PreviewQuestionUi
    ): Boolean {
        return oldItem == newItem
    }
}