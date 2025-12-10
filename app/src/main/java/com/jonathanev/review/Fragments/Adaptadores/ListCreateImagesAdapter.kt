package com.jonathanev.review.Fragments.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jonathanev.review.Data.Model.QuestionImageDiffCallback
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Fragments.ViewHolders.ListCreateImagesViewHolder
import com.jonathanev.review.databinding.ListCreateImagesBinding

class ListCreateImagesAdapter(
    private val posClicked:(Int) -> Unit
): ListAdapter<QuestionContent.Image, ListCreateImagesViewHolder>(QuestionImageDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListCreateImagesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListCreateImagesBinding.inflate(inflater, parent, false)
        return ListCreateImagesViewHolder(binding, posClicked)
    }

    override fun onBindViewHolder(holder: ListCreateImagesViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}