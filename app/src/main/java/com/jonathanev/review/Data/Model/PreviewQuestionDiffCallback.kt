package com.jonathanev.review.Data.Model

import androidx.recyclerview.widget.DiffUtil

class PreviewQuestionDiffCallback: DiffUtil.ItemCallback<PreviewQuestion>() {
    override fun areItemsTheSame(
        oldItem: PreviewQuestion,
        newItem: PreviewQuestion
    ): Boolean {
        return oldItem.question == newItem.question
    }

    override fun areContentsTheSame(
        oldItem: PreviewQuestion,
        newItem: PreviewQuestion
    ): Boolean {
        return oldItem == newItem
    }
}