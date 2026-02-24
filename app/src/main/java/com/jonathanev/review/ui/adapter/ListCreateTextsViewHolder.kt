package com.jonathanev.review.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.databinding.ListCreateTextsBinding
import com.jonathanev.review.presentation.model.QuestionContentUi

class ListCreateTextsViewHolder(
    private val binding: ListCreateTextsBinding,
    private val onEditClicked: (Int) -> Unit,
    private val onDeleteClicked: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(content: QuestionContentUi.Text, itemPosition: String) {
        binding.lblText.text = content.text
        binding.noPosition.text = itemPosition

        binding.btnEdit.setOnClickListener {
            onEditClicked(layoutPosition)
        }

        binding.btnCancel.setOnClickListener {
            onDeleteClicked(layoutPosition)
        }
    }
}