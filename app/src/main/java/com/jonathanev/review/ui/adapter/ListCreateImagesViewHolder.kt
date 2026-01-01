package com.jonathanev.review.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.jonathanev.review.databinding.ListCreateImagesBinding
import com.jonathanev.review.presentation.model.QuestionContentUi

class ListCreateImagesViewHolder(
    private val binding: ListCreateImagesBinding,
    private val onEditClicked: (Int) -> Unit,
    private val onDeleteClicked: (Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {
    fun bind(image: QuestionContentUi.Image) {
        Glide.with(binding.ivImagen.context)
            .load(image.uri)
            .centerCrop()
            .format(DecodeFormat.PREFER_RGB_565)
            .into(binding.ivImagen)

        binding.btnEdit.setOnClickListener {
            onEditClicked(layoutPosition)
        }

        binding.btnCancel.setOnClickListener {
            onDeleteClicked(layoutPosition)
        }
    }
}