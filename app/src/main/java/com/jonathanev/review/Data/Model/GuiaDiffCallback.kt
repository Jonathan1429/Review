package com.jonathanev.review.Data.Model

import androidx.recyclerview.widget.DiffUtil

class GuiaDiffCallback : DiffUtil.ItemCallback<GuiaModel>() {
    override fun areItemsTheSame(oldItem: GuiaModel, newItem: GuiaModel): Boolean =
        oldItem.nombreGuia == newItem.nombreGuia

    override fun areContentsTheSame(oldItem: GuiaModel, newItem: GuiaModel): Boolean =
        oldItem == newItem
}