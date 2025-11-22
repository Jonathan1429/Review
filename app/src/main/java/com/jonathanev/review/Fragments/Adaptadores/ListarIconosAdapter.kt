package com.jonathanev.review.Fragments.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.Data.Model.IconDiffCallback
import com.jonathanev.review.Fragments.ViewHolders.ListarIconosViewHolder
import com.jonathanev.review.R

class ListarIconosAdapter(
    private val posClicked: (Int) -> Unit
) : ListAdapter<Int, ListarIconosViewHolder>(IconDiffCallback()) {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListarIconosViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_icons_custom, parent, false)
        return ListarIconosViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListarIconosViewHolder, position: Int) {
        val icono = currentList[position]
        val isSelected = selectedPosition == position

        holder.bind(icono, isSelected) {

            val realPos = holder.adapterPosition
            if (realPos == RecyclerView.NO_POSITION) return@bind

            val oldPos = selectedPosition
            selectedPosition = realPos

            // Actualiza solo los ítems necesarios
            if (oldPos != RecyclerView.NO_POSITION)
                notifyItemChanged(oldPos)

            notifyItemChanged(realPos)

            posClicked(realPos)
        }
    }

    fun handleItemClick(position: Int) {
        if (position == RecyclerView.NO_POSITION || position >= itemCount) return

        val oldPos = selectedPosition
        selectedPosition = position

        if (oldPos != RecyclerView.NO_POSITION)
            notifyItemChanged(oldPos)

        notifyItemChanged(position)
    }
}