package com.jonathanev.review.presentation.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.jonathanev.review.presentation.model.FolderUI

class FolderUIDiffCallback : DiffUtil.ItemCallback<FolderUI>() {
    override fun areItemsTheSame(oldItem: FolderUI, newItem: FolderUI): Boolean =
        oldItem.folderUiModel.name == newItem.folderUiModel.name

    override fun areContentsTheSame(oldItem: FolderUI, newItem: FolderUI): Boolean =
        oldItem == newItem
}