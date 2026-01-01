package com.jonathanev.review.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jonathanev.review.databinding.ListPintarTextosRepasarBinding
import com.jonathanev.review.presentation.model.QuestionContentUi

class ListItemPintarTextosAdapter(
    private val posClicked: (Int) -> Unit
): ListAdapter<QuestionContentUi.Text, ListItemPintarTextosViewHolder>(QuestionTextDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemPintarTextosViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListPintarTextosRepasarBinding.inflate(inflater, parent, false)
        return ListItemPintarTextosViewHolder(binding, posClicked)
    }

    override fun onBindViewHolder(holder: ListItemPintarTextosViewHolder, position: Int) {
        val text = getItem(position)
        holder.bind(text)
    }
}