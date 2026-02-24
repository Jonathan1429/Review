package com.jonathanev.review.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.databinding.ListPintarTextosRepasarBinding
import com.jonathanev.review.presentation.model.QuestionContentUi
import javax.inject.Inject

class ListItemPintarTextosViewHolder @Inject constructor(
    private val binding: ListPintarTextosRepasarBinding,
    private val posClicked: (Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {
    fun bind(text: QuestionContentUi.Text, positionItem: String) {
        binding.lblText.text = text.text
        binding.noPosition.text = positionItem

        binding.titleVisor.setOnClickListener {
            posClicked(layoutPosition)
        }
    }
}