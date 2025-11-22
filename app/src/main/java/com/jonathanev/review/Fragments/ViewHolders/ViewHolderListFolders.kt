package com.jonathanev.review.Fragments.ViewHolders

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.R
import com.jonathanev.review.databinding.ListItemFolderBinding
import javax.inject.Inject

class ViewHolderListFolders @Inject constructor(
    private val binding: ListItemFolderBinding,
    private val posClicked: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(guia: GuiaModel) {
        Glide.with(binding.itemCarpeta.ivCarpeta.context)
            .load(guia.imgGuia)
            .override(80, 80)
            .centerCrop()
            .format(DecodeFormat.PREFER_RGB_565)
            .into(binding.itemCarpeta.ivCarpeta)


        binding.lblTitle.text = guia.nombreGuia

        val background = binding.itemCarpeta.bgCarpeta.background as GradientDrawable
        binding.itemCarpeta.ivCarpeta.imageTintMode = PorterDuff.Mode.SRC_ATOP
        val color50 = ColorUtils.setAlphaComponent(guia.color, 50)
        background.setColor(color50)

        binding.itemCarpeta.ivCarpeta.imageTintList = ColorStateList.valueOf(guia.color)
        val lblNumGuides = "${guia.num} ${ContextCompat.getString(binding.noFoldersDescription.context, R.string.lblGuides)}"
        binding.noFoldersDescription.text = lblNumGuides

        itemView.setOnClickListener {
            posClicked(layoutPosition)
        }
    }
}