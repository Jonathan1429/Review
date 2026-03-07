package com.jonathanev.review.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jonathanev.review.databinding.ListPintarImagenesRepasarBinding
import com.jonathanev.review.presentation.model.QuestionContentUi

class ListItemPintarImagenesAdapter(
    private val posClicked: (Int) -> Unit
): ListAdapter<QuestionContentUi.Image, ListItemPintarImagenesViewHolder>(
    QuestionImageDiffCallback()
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListItemPintarImagenesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListPintarImagenesRepasarBinding.inflate(inflater, parent, false)
        return ListItemPintarImagenesViewHolder(binding, posClicked)
    }

    override fun onBindViewHolder(holder: ListItemPintarImagenesViewHolder, position: Int) {
        val image = getItem(position)
        val position = (position + 1).toString()
        holder.bind(image, position)
    }
}