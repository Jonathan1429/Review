package com.jonathanev.review.Fragments.ViewHolders

import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.presentation.model.QuestionContentDomain
import com.jonathanev.review.databinding.ListPintarTextosRepasarBinding
import com.jonathanev.review.presentation.model.QuestionContentUi
import javax.inject.Inject

class ListItemPintarTextosViewHolder @Inject constructor(
    private val binding: ListPintarTextosRepasarBinding,
    private val posClicked: (Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {
    fun bind(text: QuestionContentUi.Text) {
        binding.lblText.text = text.text

        binding.titleVisor.setOnClickListener {
            posClicked(layoutPosition)
        }
    }
}