package com.jonathanev.review.Fragments.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jonathanev.review.Data.Model.QuestionTextDiffCallback
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Fragments.ViewHolders.ListItemPintarTextosViewHolder
import com.jonathanev.review.databinding.ListPintarTextosRepasarBinding

class ListItemPintarTextosAdapter(
    private val posClicked: (Int) -> Unit
): ListAdapter<QuestionContent.Text, ListItemPintarTextosViewHolder>(QuestionTextDiffCallback()) {
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