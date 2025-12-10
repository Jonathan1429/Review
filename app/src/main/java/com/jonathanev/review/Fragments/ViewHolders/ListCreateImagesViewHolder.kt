package com.jonathanev.review.Fragments.ViewHolders

import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.databinding.ListCreateImagesBinding

class ListCreateImagesViewHolder(
    private val binding: ListCreateImagesBinding,
    private val posClicked: (Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {
    fun bind(image: QuestionContent.Image) {
        binding.ivImagen.setImage(ImageSource.uri(image.decodedPath))

        binding.btnEdit.setOnClickListener {
            posClicked(layoutPosition)
        }
    }
}