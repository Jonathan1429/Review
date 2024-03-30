package com.jonathanev.review.Fragments.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Fragments.ViewHolders.ListarGuiasViewHolder
import com.jonathanev.review.R

class ListarGuiasAdapter(private var guiasEstudio: List<GuiaModel>, private val positionClicked: (Int) -> Unit): RecyclerView.Adapter<ListarGuiasViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListarGuiasViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.listar_guias_personalizado, parent, false)
        return ListarGuiasViewHolder(view)
    }

    override fun getItemCount() = guiasEstudio.size

    override fun onBindViewHolder(holder: ListarGuiasViewHolder, position: Int) {
        holder.render(guiasEstudio[position], positionClicked)
    }

    /*fun agregarGuias(nuevasGuias: List<GuiaModel>) {
        val listaMutable = guiasEstudio.toMutableList() // Copiar la lista existente a una lista mutable
        listaMutable.addAll(nuevasGuias) // Agregar las nuevas guías a la lista mutable
        guiasEstudio = listaMutable.toList() // Asignar la lista mutable actualizada a guiasEstudio
        notifyDataSetChanged()
    }*/
}