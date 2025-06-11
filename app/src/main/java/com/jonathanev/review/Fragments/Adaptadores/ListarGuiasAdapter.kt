package com.jonathanev.review.Fragments.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Fragments.ViewHolders.ListarGuiasViewHolder
import com.jonathanev.review.R

class ListarGuiasAdapter(
    private var guias: List<GuiaModel> = listOf(),
    private val posClicked: (Int) -> Unit
) : RecyclerView.Adapter<ListarGuiasViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListarGuiasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.listar_guias_personalizado, parent, false)
        return ListarGuiasViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListarGuiasViewHolder, position: Int) {
        holder.bind(guias[position], posClicked)
    }

    override fun getItemCount() = guias.size
}
