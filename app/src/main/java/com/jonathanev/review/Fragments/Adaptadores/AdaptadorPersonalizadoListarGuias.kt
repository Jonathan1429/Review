package com.jonathanev.review.Fragments.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jonathanev.review.Clases.Guia
import com.jonathanev.review.R

class AdaptadorPersonalizadoListarGuias(
    private val context: Context?,
    private val listaGuias: List<Guia>
) : BaseAdapter() {
    override fun getCount(): Int {
        return listaGuias.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        var convertView = convertView
        val imgGuiaEstudio: ImageView
        val nombreArchivo: TextView
        val guias = listaGuias[position]

        // Solo si la vista es nula la vamos a cargar.
        if (convertView == null) {
            convertView =
                LayoutInflater.from(context).inflate(R.layout.listar_guias_personalizado, null)
        }
        imgGuiaEstudio = convertView.findViewById(R.id.ivGuiaEstudio)
        nombreArchivo = convertView.findViewById(R.id.tvTituloGuia)

        // Le ponemos los valores correspondientes a cada uno
        imgGuiaEstudio.setImageResource(guias.imgGuia)
        nombreArchivo.text = guias.nombreGuia
        return convertView
    }
}