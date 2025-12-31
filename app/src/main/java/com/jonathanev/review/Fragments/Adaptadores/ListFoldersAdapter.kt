package com.jonathanev.review.Fragments.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jonathanev.review.presentation.ui.adapter.FolderUIDiffCallback
import com.jonathanev.review.presentation.model.FolderUI
import com.jonathanev.review.Fragments.ViewHolders.ListFoldersViewHolder
import com.jonathanev.review.databinding.ListItemFolderBinding

class ListFoldersAdapter(
    private val posClicked: (Int) -> Unit
) : ListAdapter<FolderUI, ListFoldersViewHolder>(FolderUIDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListFoldersViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemFolderBinding.inflate(inflater, parent, false)
        return ListFoldersViewHolder(binding, posClicked)
    }

    override fun onBindViewHolder(holder: ListFoldersViewHolder, position: Int) {
        val guia = getItem(position)
        holder.bind(guia)
    }
}
