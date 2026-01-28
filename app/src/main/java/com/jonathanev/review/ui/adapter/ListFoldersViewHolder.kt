package com.jonathanev.review.ui.adapter

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.jonathanev.review.R
import com.jonathanev.review.databinding.ListItemFolderBinding
import com.jonathanev.review.presentation.folders.model.FolderUiModel
import com.jonathanev.review.ui.mapper.toColorRes
import com.jonathanev.review.ui.mapper.toDrawableRes
import javax.inject.Inject

class ListFoldersViewHolder @Inject constructor(
    private val binding: ListItemFolderBinding,
    private val posClicked: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(folder: FolderUiModel) {
        Glide.with(binding.itemCarpeta.ivCarpeta.context)
            .load(folder.folder.imgFolder.toDrawableRes())
            .override(80, 80)
            .centerCrop()
            .format(DecodeFormat.PREFER_RGB_565)
            .into(binding.itemCarpeta.ivCarpeta)


        binding.lblTitle.text = folder.folder.name

        val background = binding.itemCarpeta.bgCarpeta.background as GradientDrawable
        binding.itemCarpeta.ivCarpeta.imageTintMode = PorterDuff.Mode.SRC_ATOP
        val colorRes = folder.folder.color.toColorRes()
        val color = ContextCompat.getColor(binding.itemCarpeta.ivCarpeta.context, colorRes)
        val color50 = ColorUtils.setAlphaComponent(color, 50)
        background.setColor(color50)

        binding.itemCarpeta.ivCarpeta.imageTintList = ColorStateList.valueOf(color)
        val lblNumGuides = "${folder.numGuides} ${ContextCompat.getString(binding.noFoldersDescription.context, R.string.lblGuides)}"
        binding.noFoldersDescription.text = lblNumGuides

        itemView.setOnClickListener {
            posClicked(layoutPosition)
        }
    }
}