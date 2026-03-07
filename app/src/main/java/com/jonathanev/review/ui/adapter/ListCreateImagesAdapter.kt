package com.jonathanev.review.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jonathanev.review.databinding.ListCreateImagesBinding
import com.jonathanev.review.presentation.model.QuestionContentUi

class ListCreateImagesAdapter(
    private val onEditClicked:(Int) -> Unit,
    private val onDeleteClicked: (Int) -> Unit
): ListAdapter<QuestionContentUi.Image, ListCreateImagesViewHolder>(QuestionImageDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListCreateImagesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListCreateImagesBinding.inflate(inflater, parent, false)
        return ListCreateImagesViewHolder(binding, onEditClicked, onDeleteClicked)
    }

    override fun onBindViewHolder(holder: ListCreateImagesViewHolder, position: Int) {
        val item = getItem(position)
        val itemPosition = (position + 1).toString()
        holder.bind(item, itemPosition)
    }
}