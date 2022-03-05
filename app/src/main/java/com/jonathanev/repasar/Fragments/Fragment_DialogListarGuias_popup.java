package com.jonathanev.repasar.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.Collator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jonathanev.repasar.Activities.Activity_RepasarGuia;
import com.jonathanev.repasar.Clases.Guias;
import com.jonathanev.repasar.Fragments.Adaptadores.AdaptadorPersonalizadoListarGuias;
import com.jonathanev.repasar.R;
import com.jonathanev.repasar.databinding.FragmentListarGuiasBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class Fragment_DialogListarGuias_popup extends DialogFragment {

    private FragmentListarGuiasBinding binding;

    // Guardar la collection del set en este Arreglo
    private ArrayList<String> item = new ArrayList<String>();

    // Cargar el ListView
    private ArrayList<Guias> lista = new ArrayList<>();

    public Fragment_DialogListarGuias_popup() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentListarGuiasBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recibimos las preferencias y las guardamos nuevamente en un set.
        SharedPreferences preferences = getActivity().getSharedPreferences("nombres_guias", Context.MODE_PRIVATE);
        Set<String> set = preferences.getStringSet("guias_estudio", null);

        // Metemos la collection directamente en el arreglo y se ordena automáticamente.
        item.addAll(set);

        // Cargamos la lista de los items
        cargarListaGuiasEstudio(item);

        binding.lvGuiasEstudio.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int posicion, long id) {
                Guias guias = lista.get(posicion);

                // Le mostramos un dialogo con multiples opciones.
                new AlertDialog.Builder(getActivity())
                        .setIcon(R.drawable.ic_advertencia)
                        .setTitle("¿Atención!")
                        .setMessage("¿Cuál es la acción que deseas realizar?")
                        // Le da la opción de abrir la guia seleccionada.
                        .setPositiveButton("Abrir Guia", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(getActivity(), Activity_RepasarGuia.class);
                                intent.putExtra("nombre_archivo", guias.getNombreGuia());
                                startActivity(intent);
                                // Recuperamos el dialogo abierto actualmente
                                // (Fragment_DialogListarGuias.java) y lo cerramos.
                                Dialog dialogActual =  getDialog();
                                dialogActual.dismiss();
                            }
                        })
                        // Le da la opción de eliminar la guia seleccionada.
                        .setNegativeButton("Eliminar Guia", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                @SuppressLint("SdCardPath") File file = new File("/data/data/com.example.repasar/files/");
                                if (file.exists()){
                                    new File(file, guias.getNombreGuia()+".xml").delete();
                                    Toast.makeText(getContext(),
                                            "¡Archivo eliminado exitosamente!",
                                            Toast.LENGTH_SHORT).show();

                                    // Recuperamos el dialogo abierto actualmente
                                    // (Fragment_DialogListarGuias_popup.java)
                                    // y lo cerramos.
                                    Dialog dialogActual =  getDialog();
                                    dialogActual.dismiss();
                                } else {
                                    Toast.makeText(getContext(), "La ruta para eliminar el " +
                                                    "archivo actualmente no existe.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        // Le da la opción de cancelar.
                        .setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });
    }

    private void cargarListaGuiasEstudio(ArrayList<String> item) {
        // Se crea un número random para saber cual imagen aparecerá en el listado.
        int numeroRandom = (int) (Math.random() * 6) + 1;

        switch (numeroRandom){
            case 1:
                numeroRandom = R.drawable.cerebro;
                break;
            case 2:
                numeroRandom = R.drawable.cuaderno;
                break;
            case 3:
                numeroRandom = R.drawable.estudiar;
                break;
            case 4:
                numeroRandom = R.drawable.libro_dual;
                break;
            case 5:
                numeroRandom = R.drawable.libro_triple;
                break;
            case 6:
                numeroRandom = R.drawable.libros;
                break;
        }

        AdaptadorPersonalizadoListarGuias adaptador = null;
        Guias guias;

        for (int i = 0; i < item.size(); i++) {
            guias = new Guias();
            guias.setImgGuia(numeroRandom);
            guias.setNombreGuia(item.get(i));
            lista.add(guias);
        }

        adaptador = new AdaptadorPersonalizadoListarGuias(getContext(), lista);
        binding.lvGuiasEstudio.setAdapter(adaptador);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null){
            dialog.getWindow().setLayout(900, 1200);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}