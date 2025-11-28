package com.jonathanev.review.Fragments.ViewHolders

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.jonathanev.review.Data.Model.prueba.FolderUI
import com.jonathanev.review.R
import com.jonathanev.review.databinding.ListItemFolderBinding
import javax.inject.Inject

class ListFoldersViewHolder @Inject constructor(
    private val binding: ListItemFolderBinding,
    private val posClicked: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(folder: FolderUI) {
        Glide.with(binding.itemCarpeta.ivCarpeta.context)
            .load(folder.folderModel.imgFolder)
            .override(80, 80)
            .centerCrop()
            .format(DecodeFormat.PREFER_RGB_565)
            .into(binding.itemCarpeta.ivCarpeta)


        binding.lblTitle.text = folder.folderModel.nameFolder

        val background = binding.itemCarpeta.bgCarpeta.background as GradientDrawable
        binding.itemCarpeta.ivCarpeta.imageTintMode = PorterDuff.Mode.SRC_ATOP
        val color50 = ColorUtils.setAlphaComponent(folder.folderModel.color, 50)
        background.setColor(color50)

        binding.itemCarpeta.ivCarpeta.imageTintList = ColorStateList.valueOf(folder.folderModel.color)
        val lblNumGuides = "${folder.numGuides} ${ContextCompat.getString(binding.noFoldersDescription.context, R.string.lblGuides)}"
        binding.noFoldersDescription.text = lblNumGuides

        itemView.setOnClickListener {
            posClicked(layoutPosition)
        }
    }
}