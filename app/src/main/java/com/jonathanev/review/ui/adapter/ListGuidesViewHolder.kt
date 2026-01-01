package com.jonathanev.review.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.databinding.ListCustomGuideBinding
import com.jonathanev.review.presentation.files.model.GuideUiModel
import javax.inject.Inject

class ListGuidesViewHolder @Inject constructor(
    private val binding: ListCustomGuideBinding,
    private val posClicked: (Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {
    fun bind(guia: GuideUiModel) {
        val nameGuide = "${guia.nameGuide}.review"

        binding.tvGuideTitle.text = nameGuide
        if (guia.description.isNotEmpty()){
            binding.tvGuideDescription.text = guia.description
        }

        itemView.setOnClickListener{
            posClicked(layoutPosition)
        }
    }
}