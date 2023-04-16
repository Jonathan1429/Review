package com.jonathanev.review.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.jonathanev.review.Activities.Activity_Modificar;
import com.jonathanev.review.Activities.Activity_RepasarGuia;
import com.jonathanev.review.Clases.Guias;
import com.jonathanev.review.Fragments.Adaptadores.AdaptadorPersonalizadoListarGuias;
import com.jonathanev.review.R;
import com.jonathanev.review.databinding.FragmentListarGuiasBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recibimos las preferencias y las guardamos nuevamente en un set.
        SharedPreferences preferences = getActivity().getSharedPreferences("nombres_guias", Context.MODE_PRIVATE);
        Set<String> set = preferences.getStringSet("guias_estudio", null);

        // Metemos la collection directamente en el arreglo y se ordena automáticamente.
        item.addAll(set);

        // Ordena las guías con el método sort
        item.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        // Sino tengo ninguna guía de estudio aparece el texto que no tengo guías
        if (item.isEmpty()){
            binding.tvSinGuias.setVisibility(View.VISIBLE);
        } else {
            // Si es lo contrario no aparece el texto
            binding.tvSinGuias.setVisibility(View.INVISIBLE);
        }

        // Cargamos la lista de los items
        cargarListaGuiasEstudio(item);

        binding.lvGuiasEstudio.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int posicion, long id) {
                Guias guias = lista.get(posicion);

                // Creo una alerta donde me saldrán una lista de items
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setIcon(R.drawable.ic_advertencia);
                builder.setTitle("¿Qué acción deseas realizar?");
                builder.setItems(new CharSequence[]
                                {"Abrir Guia", "Modificar Guia", "Eliminar Guia", "Cancelar"},
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        // Si entra al primer item se abre la guía a review
                                        Intent intentAbrirGuia = new Intent(getActivity(), Activity_RepasarGuia.class);
                                        intentAbrirGuia.putExtra("nombre_archivo", guias.getNombreGuia());
                                        startActivity(intentAbrirGuia);
                                        // Recuperamos el dialogo abierto actualmente
                                        // (Fragment_DialogListarGuias.java) y lo cerramos.
                                        Dialog dialogoAbrirGuia =  getDialog();
                                        dialogoAbrirGuia.dismiss();
                                        break;
                                    case 1:
                                        // Si entra al segundo es para modificar la guía de estudio
                                        Intent intentModificarGuia = new Intent(getActivity(), Activity_Modificar.class);
                                        intentModificarGuia.putExtra("nombre_archivo", guias.getNombreGuia());
                                        startActivity(intentModificarGuia);
                                        // Recuperamos el dialogo abierto actualmente
                                        // (Fragment_DialogListarGuias.java) y lo cerramos.
                                        Dialog dialogoModificarGuia =  getDialog();
                                        dialogoModificarGuia.dismiss();
                                        break;
                                    case 2:
                                        // Se ejecuta cuando se regresa sin guardar.
                                        new AlertDialog.Builder(getContext())
                                                .setTitle("¡Atención!")
                                                .setMessage("¿Estás seguro que deseas eliminar la" +
                                                        " guia?")
                                                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        // Si entra al tercero es para eliminar la guia exitosamente
                                                        @SuppressLint("SdCardPath") File file = new File("/data/data/com.jonathanev.review/files/");
                                                        if (file.exists()){
                                                            new File(file, guias.getNombreGuia()+".xml").delete();
                                                            Toast.makeText(getContext(),
                                                                    "¡Archivo eliminado exitosamente!",
                                                                    Toast.LENGTH_SHORT).show();

                                                            // Recuperamos el dialogo abierto actualmente
                                                            // (Fragment_DialogListarGuias_popup.java)
                                                            // y lo cerramos.
                                                            Dialog dialogoEliminarGuia =  getDialog();
                                                            dialogoEliminarGuia.dismiss();
                                                        } else {
                                                            Toast.makeText(getContext(), "La ruta para eliminar el " +
                                                                            "archivo actualmente no existe.",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                })
                                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {
                                                        dialog.dismiss();
                                                    }
                                                }).create().show();
                                        break;
                                    case 3:
                                        // Si entra al 5 se cancela cualquier operación
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), "Cancelaste la acción", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        });
                builder.create().show();
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

        // La imagen guardada en numeroRandom se pondrá en el objeto guias
        // Después se irá poniendo el nombre de cada una de las guias en el objeto
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