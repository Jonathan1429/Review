package com.jonathanev.review.Fragments.ViewHolders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.databinding.ListarGuiasPersonalizadoBinding

class ListarGuiasViewHolder(
    view: View,
) : RecyclerView.ViewHolder(view) {

    private val binding = ListarGuiasPersonalizadoBinding.bind(view)

    fun bind(guia: GuiaModel, posClicked: (Int) -> Unit) {
        Glide.with(binding.ivGuiaEstudio.context)
            .load(guia.imgGuia)
            .override(80, 80) // Tamaño de tu ImageView
            .centerCrop()
            // format: Usa menos memoria hasta un 50% con una ligera perdida de color y nitidez pero imperceptible en imagenes pequeñas
            .format(DecodeFormat.PREFER_RGB_565)
            .into(binding.ivGuiaEstudio)

        binding.tvTituloGuia.text = guia.nombreGuia

        itemView.setOnClickListener {
            posClicked(layoutPosition)
        }
    }
}