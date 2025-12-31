package com.jonathanev.review.Fragments.ViewHolders

import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.presentation.model.QuestionContentDomain
import com.jonathanev.review.databinding.ListCreateTextsBinding
import com.jonathanev.review.presentation.model.QuestionContentUi

class ListCreateTextsViewHolder(
    private val binding: ListCreateTextsBinding,
    private val onEditClicked: (Int) -> Unit,
    private val onDeleteClicked: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(content: QuestionContentUi.Text) {
        binding.lblText.text = content.text

        binding.btnEdit.setOnClickListener {
            onEditClicked(layoutPosition)
        }

        binding.btnCancel.setOnClickListener {
            onDeleteClicked(layoutPosition)
        }
    }
}