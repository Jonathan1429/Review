package com.jonathanev.review.Fragments.ViewHolders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.databinding.ListarGuiasPersonalizadoBinding

class ListarGuiasViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private var binding: ListarGuiasPersonalizadoBinding = ListarGuiasPersonalizadoBinding.bind(view)

    fun render(guias: GuiaModel, positionClicked: (Int) -> Unit) {
        binding.ivGuiaEstudio.setImageResource(guias.imgGuia)
        binding.tvTituloGuia.text = guias.nombreGuia

        itemView.setOnClickListener {
            positionClicked(layoutPosition)
        }
    }
}