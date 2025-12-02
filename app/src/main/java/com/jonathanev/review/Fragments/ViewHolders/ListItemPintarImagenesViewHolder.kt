package com.jonathanev.review.Fragments.ViewHolders

import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.databinding.ListPintarImagenesRepasarBinding

class ListItemPintarImagenesViewHolder(
    private val binding: ListPintarImagenesRepasarBinding,
    private val posClicked: (Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {
    fun bind(image: QuestionContent.Image){
        binding.ivImagen.setImage(ImageSource.uri(image.decodedPath))

        binding.titleVisor.setOnClickListener {
            posClicked(layoutPosition)
        }
    }
}