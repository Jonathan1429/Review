package com.jonathanev.review.Fragments.ViewHolders

import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.data.Model.GuideModel
import com.jonathanev.review.databinding.ListCustomGuideBinding
import javax.inject.Inject

class ListGuidesViewHolder @Inject constructor(
    private val binding: ListCustomGuideBinding,
    private val posClicked: (Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {
    fun bind(guia: GuideModel) {
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