package com.jonathanev.review.Fragments.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jonathanev.review.Data.Model.FoldersUiState
import com.jonathanev.review.Data.Model.GuiaDiffCallback
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Fragments.ViewHolders.ViewHolderListFolders
import com.jonathanev.review.databinding.ListItemFolderBinding
import com.jonathanev.review.databinding.ListarGuiasPersonalizadoBinding

class AdapterListFolders(
    private val posClicked: (Int) -> Unit
) : ListAdapter<GuiaModel, ViewHolderListFolders>(GuiaDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderListFolders {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemFolderBinding.inflate(inflater, parent, false)
        return ViewHolderListFolders(binding, posClicked)
    }

    override fun onBindViewHolder(holder: ViewHolderListFolders, position: Int) {
        val guia = getItem(position)
        holder.bind(guia)
    }
}
