package com.jonathanev.repasar.Fragments.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jonathanev.repasar.Clases.Guias;
import com.jonathanev.repasar.R;

import java.util.List;

public class AdaptadorPersonalizadoListarGuias extends BaseAdapter {

    private Context context;
    private List<Guias> listaGuias;

    public AdaptadorPersonalizadoListarGuias(Context context, List<Guias> guias) {
        this.context = context;
        this.listaGuias = guias;
    }

    @Override
    public int getCount() {
        return listaGuias.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imgGuiaEstudio;
        TextView nombreArchivo;

        Guias guias = listaGuias.get(position);

        // Solo si la vista es nula la vamos a cargar.
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.listar_guias_personalizado, null);
        }

        imgGuiaEstudio = convertView.findViewById(R.id.ivGuiaEstudio);
        nombreArchivo = convertView.findViewById(R.id.tvTituloGuia);

        // Le ponemos los valores correspondientes a cada uno
        imgGuiaEstudio.setImageResource(guias.getImgGuia());
        nombreArchivo.setText(guias.getNombreGuia());
        return convertView;
    }
}
