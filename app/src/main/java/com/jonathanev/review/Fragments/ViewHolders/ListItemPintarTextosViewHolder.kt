package com.jonathanev.review.Fragments.ViewHolders

import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.databinding.ListPintarTextosRepasarBinding
import javax.inject.Inject

class ListItemPintarTextosViewHolder @Inject constructor(
    private val binding: ListPintarTextosRepasarBinding,
    private val posClicked: (Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {
    fun bind(text: QuestionContent.Text) {
        binding.lblText.text = text.text

        binding.titleVisor.setOnClickListener {
            posClicked(layoutPosition)
        }
    }
}