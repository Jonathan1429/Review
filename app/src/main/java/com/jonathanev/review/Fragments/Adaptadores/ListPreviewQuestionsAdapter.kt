package com.jonathanev.review.Fragments.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jonathanev.review.Data.Model.PreviewQuestion
import com.jonathanev.review.Data.Model.PreviewQuestionDiffCallback
import com.jonathanev.review.Data.Model.StateUIPreviewQuestion
import com.jonathanev.review.Fragments.ViewHolders.ListPreviewQuestionsViewHolder
import com.jonathanev.review.databinding.ListPreviewQuestionsBinding

class ListPreviewQuestionsAdapter(
    private val posClicked: (Int) -> Unit
): ListAdapter<PreviewQuestion, ListPreviewQuestionsViewHolder>(PreviewQuestionDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListPreviewQuestionsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListPreviewQuestionsBinding.inflate(inflater, parent, false)
        return ListPreviewQuestionsViewHolder(binding, posClicked)
    }

    override fun onBindViewHolder(holder: ListPreviewQuestionsViewHolder, position: Int) {
        val guia = getItem(position)
        holder.bind(guia)
    }
}