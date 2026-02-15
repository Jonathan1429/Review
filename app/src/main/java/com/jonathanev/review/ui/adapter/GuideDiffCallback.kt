package com.jonathanev.review.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.jonathanev.review.presentation.model.GuideUiModel

class GuideDiffCallback: DiffUtil.ItemCallback<GuideUiModel>() {
    override fun areItemsTheSame(oldItem: GuideUiModel, newItem: GuideUiModel): Boolean {
        return oldItem.nameGuide == newItem.nameGuide
    }

    override fun areContentsTheSame(oldItem: GuideUiModel, newItem: GuideUiModel): Boolean {
        return oldItem == newItem
    }
}