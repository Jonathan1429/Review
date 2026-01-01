package com.jonathanev.review.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.jonathanev.review.databinding.ItemIconsCustomBinding

class ListarIconosViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemIconsCustomBinding.bind(view)

    fun bind(
        icono: Int,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        Glide.with(binding.ivIcon.context)
            .load(icono)
            .override(50, 50)
            .centerCrop()
            .format(DecodeFormat.PREFER_RGB_565)
            .into(binding.ivIcon)

        itemView.isSelected = isSelected

        itemView.setOnClickListener { onClick() }
    }
}