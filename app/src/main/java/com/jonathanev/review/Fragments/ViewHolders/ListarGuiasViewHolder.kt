package com.jonathanev.review.Fragments.ViewHolders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.databinding.ListarGuiasPersonalizadoBinding

/*class ListarGuiasViewHolder(
    view: View,
    private val click: (Int) -> Unit
) : RecyclerView.ViewHolder(view) {

    private val binding = ListarGuiasPersonalizadoBinding.bind(view)

    init {
        binding.root.setOnClickListener {
            val pos = adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                click(pos)
            }
        }
    }

    fun bind(guia: GuiaModel) {
        Glide.with(binding.ivGuiaEstudio.context)
            .load(guia.imgGuia)
            .override(80, 80) // Tamaño de tu ImageView
            .centerCrop()
            .into(binding.ivGuiaEstudio)

        binding.tvTituloGuia.text = guia.nombreGuia
    }
}*/

class ListarGuiasViewHolder(
    view: View,
) : RecyclerView.ViewHolder(view) {

    private val binding = ListarGuiasPersonalizadoBinding.bind(view)

    fun bind(guia: GuiaModel, posClicked: (Int) -> Unit) {
        Glide.with(binding.ivGuiaEstudio.context)
            .load(guia.imgGuia)
            .override(80, 80) // Tamaño de tu ImageView
            .centerCrop()
            .into(binding.ivGuiaEstudio)

        binding.tvTituloGuia.text = guia.nombreGuia

        itemView.setOnClickListener {
            posClicked(layoutPosition)
        }
    }
}