package com.jonathanev.review.Fragments.ViewHolders

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.jonathanev.review.data.Model.prueba.QuestionContent
import com.jonathanev.review.databinding.ListPintarImagenesRepasarBinding

class ListItemPintarImagenesViewHolder(
    private val binding: ListPintarImagenesRepasarBinding,
    private val posClicked: (Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {
    fun bind(image: QuestionContent.Image){
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