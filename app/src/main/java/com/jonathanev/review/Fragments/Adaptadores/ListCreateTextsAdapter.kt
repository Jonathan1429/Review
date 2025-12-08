package com.jonathanev.review.Fragments.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jonathanev.review.Data.Model.QuestionTextDiffCallback
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Fragments.ViewHolders.ListCreateTextsViewHolder
import com.jonathanev.review.databinding.ListCreateTextsBinding

class ListCreateTextsAdapter(
    private val posClicked: (Int) -> Unit
): ListAdapter<QuestionContent.Text, ListCreateTextsViewHolder>(QuestionTextDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListCreateTextsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListCreateTextsBinding.inflate(inflater, parent, false)
        return ListCreateTextsViewHolder(binding, posClicked)
    }

    override fun onBindViewHolder(holder: ListCreateTextsViewHolder, position: Int) {
        val text = getItem(position)
        holder.bind(text)
    }
}