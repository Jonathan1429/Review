package com.jonathanev.review.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jonathanev.review.databinding.ListCreateTextsBinding
import com.jonathanev.review.presentation.model.QuestionContentUi

class ListCreateTextsAdapter(
    private val onEditClicked:(Int) -> Unit,
    private val onDeleteClicked: (Int) -> Unit
): ListAdapter<QuestionContentUi.Text, ListCreateTextsViewHolder>(QuestionTextDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListCreateTextsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListCreateTextsBinding.inflate(inflater, parent, false)
        return ListCreateTextsViewHolder(binding, onEditClicked, onDeleteClicked)
    }

    override fun onBindViewHolder(holder: ListCreateTextsViewHolder, position: Int) {
        val text = getItem(position)
        holder.bind(text)
    }
}