package com.jonathanev.review.Fragments.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jonathanev.review.presentation.ui.adapter.GuideDiffCallback
import com.jonathanev.review.Fragments.ViewHolders.ListGuidesViewHolder
import com.jonathanev.review.databinding.ListCustomGuideBinding
import com.jonathanev.review.presentation.model.GuideUiModel

class ListGuidesAdapter(
    private val posClicked: (Int) -> Unit
): ListAdapter<GuideUiModel, ListGuidesViewHolder>(GuideDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListGuidesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListCustomGuideBinding.inflate(inflater, parent, false)
        return ListGuidesViewHolder(binding, posClicked)
    }

    override fun onBindViewHolder(holder: ListGuidesViewHolder, position: Int) {
        val guia = getItem(position)
        holder.bind(guia)
    }
}