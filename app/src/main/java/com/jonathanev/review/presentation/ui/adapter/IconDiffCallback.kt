package com.jonathanev.review.presentation.ui.adapter

import androidx.recyclerview.widget.DiffUtil

class IconDiffCallback : DiffUtil.ItemCallback<Int>() {
    override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean =
        oldItem == newItem
}