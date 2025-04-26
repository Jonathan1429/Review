package com.jonathanev.review.Fragments.ViewHolders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.databinding.ListarGuiasPersonalizadoBinding

class ListarGuiasViewHolder(
    view: View,
    click: (Int) -> Unit
) : RecyclerView.ViewHolder(view) {

    private val binding = ListarGuiasPersonalizadoBinding.bind(view)

    init {
        binding.root.setOnClickListener {
            // adapterPosition es lo mismo que lo que antes llamábamos layoutPosition
            // y funciona en todas las versiones
            val pos = adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                click(pos)
            }
        }
    }

    fun bind(guia: GuiaModel) {
        binding.ivGuiaEstudio.setImageResource(guia.imgGuia)
        binding.tvTituloGuia.text = guia.nombreGuia
    }
}