package com.jonathanev.review.data.Model

import androidx.recyclerview.widget.DiffUtil
import com.jonathanev.review.data.Model.prueba.FolderUI

class FolderUIDiffCallback : DiffUtil.ItemCallback<FolderUI>() {
    override fun areItemsTheSame(oldItem: FolderUI, newItem: FolderUI): Boolean =
        oldItem.folderModel.name == newItem.folderModel.name

    override fun areContentsTheSame(oldItem: FolderUI, newItem: FolderUI): Boolean =
        oldItem == newItem
}