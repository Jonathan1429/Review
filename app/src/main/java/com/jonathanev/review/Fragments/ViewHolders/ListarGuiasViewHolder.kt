package com.jonathanev.review.Fragments.ViewHolders

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.Clases.Guia
import com.jonathanev.review.databinding.ListarGuiasPersonalizadoBinding

class ListarGuiasViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private var binding: ListarGuiasPersonalizadoBinding = ListarGuiasPersonalizadoBinding.bind(view)

    fun render(guias: Guia, positionClicked: (Int) -> Unit) {
        Log.d("ListarGuiasViewHolder", "Render llamado para ${guias.nombreGuia}")
        binding.ivGuiaEstudio.setImageResource(guias.imgGuia)
        binding.tvTituloGuia.text = guias.nombreGuia

        itemView.setOnClickListener {
            positionClicked(layoutPosition)
        }
    }
}