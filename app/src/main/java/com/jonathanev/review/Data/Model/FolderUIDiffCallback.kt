package com.jonathanev.review.Data.Model

import androidx.recyclerview.widget.DiffUtil
import com.jonathanev.review.Data.Model.prueba.FolderUI

class FolderUIDiffCallback : DiffUtil.ItemCallback<FolderUI>() {
    override fun areItemsTheSame(oldItem: FolderUI, newItem: FolderUI): Boolean =
        oldItem.folderModel.name == newItem.folderModel.name

    override fun areContentsTheSame(oldItem: FolderUI, newItem: FolderUI): Boolean =
        oldItem == newItem
}