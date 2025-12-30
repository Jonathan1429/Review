package com.jonathanev.review.Fragments.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jonathanev.review.data.Model.QuestionImageDiffCallback
import com.jonathanev.review.data.Model.prueba.QuestionContent
import com.jonathanev.review.Fragments.ViewHolders.ListItemPintarImagenesViewHolder
import com.jonathanev.review.databinding.ListPintarImagenesRepasarBinding

class ListItemPintarImagenesAdapter(
    private val posClicked: (Int) -> Unit
): ListAdapter<QuestionContent.Image, ListItemPintarImagenesViewHolder>(QuestionImageDiffCallback()) {
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
        holder.bind(image)
    }
}