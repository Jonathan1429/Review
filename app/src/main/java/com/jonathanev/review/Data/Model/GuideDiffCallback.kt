package com.jonathanev.review.Data.Model

import androidx.recyclerview.widget.DiffUtil

class GuideDiffCallback: DiffUtil.ItemCallback<GuideModel>() {
    override fun areItemsTheSame(oldItem: GuideModel, newItem: GuideModel): Boolean {
        return oldItem.nameGuide == newItem.nameGuide
    }

    override fun areContentsTheSame(oldItem: GuideModel, newItem: GuideModel): Boolean {
        return oldItem == newItem
    }
}