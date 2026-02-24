package com.jonathanev.review.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.jonathanev.review.databinding.ListPintarImagenesRepasarBinding
import com.jonathanev.review.presentation.model.QuestionContentUi

class ListItemPintarImagenesViewHolder(
    private val binding: ListPintarImagenesRepasarBinding,
    private val posClicked: (Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {
    fun bind(image: QuestionContentUi.Image, positionItem: String){
        binding.noPosition.text = positionItem

        Glide.with(binding.ivImagen.context)
            .load(image.uri)
            .centerCrop()
            .format(DecodeFormat.PREFER_RGB_565)
            .into(binding.ivImagen)

        binding.titleVisor.setOnClickListener {
            posClicked(layoutPosition)
        }
    }
}