package com.jonathanev.review.Fragments.ViewHolders

import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.databinding.ListCreateTextsBinding

class ListCreateTextsViewHolder(
    private val binding: ListCreateTextsBinding,
    private val onEditClicked: (Int) -> Unit,
    private val onDeleteClicked: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(content: QuestionContent.Text) {
        binding.lblText.text = content.text

        binding.btnEdit.setOnClickListener {
            onEditClicked(layoutPosition)
        }

        binding.btnCancel.setOnClickListener {
            onDeleteClicked(layoutPosition)
        }
    }
}