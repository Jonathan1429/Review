package com.jonathanev.review.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.jonathanev.review.presentation.folders.model.FolderUiModel

class FolderUIDiffCallback : DiffUtil.ItemCallback<FolderUiModel>() {
    override fun areItemsTheSame(oldItem: FolderUiModel, newItem: FolderUiModel): Boolean =
        oldItem.name == newItem.name

    override fun areContentsTheSame(oldItem: FolderUiModel, newItem: FolderUiModel): Boolean =
        oldItem == newItem
}