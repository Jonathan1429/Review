package com.jonathanev.repasar.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jonathanev.repasar.Activities.Activity_Cuestionario;
import com.jonathanev.repasar.databinding.FragmentNuevoArchivoBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Fragment_DialogNuevoArchivo_popu extends DialogFragment{

    private FragmentNuevoArchivoBinding binding;

    public Fragment_DialogNuevoArchivo_popu() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNuevoArchivoBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnGuardarGuiaEstudio.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            @Override
            public void onClick(View view) {
                String item = "";
                // Defino la ruta donde busco los ficheros.
                @SuppressLint("SdCardPath") File file = new File("/data/data/com.jonathanev.repasar/files/");

                boolean archivoExiste = false;
                if (file.exists()){
                    // Creo el array de tipo File con el contenido de la carpeta.
                    File[] files = file.listFiles();
                    File archivo = null;
                    // Hacemos un ciclo por cada fichero para extraer el nombre uno a uno.
                    for (int i = 0; i < files.length; i++){
                        // Sacamos del array files el nombre recuperandolo por posición.
                        archivo = files[i];
                        item = archivo.getName().replaceAll(".xml", "");
                        // Comparamos el texto ingresado en la App con el recuperado.
                        if (binding.etNombreArchivo.getText().toString().equals(item)){
                            archivoExiste = true;
                            break;
                        }
                    }

                    // Si hay un archivo existente entra
                    if (archivoExiste){
                        // Se ejecuta cuando se regresa sin guardar.
                        new AlertDialog.Builder(getContext())
                                .setTitle("¡Atención!")
                                .setMessage("Ya tienes una guía con el mismo nombre, " +
                                        "si continuas se va a sobreescribir el archivo, " +
                                        "¿seguro deseas continuar?")
                                .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Si hay un valor dentro del campo enviamos el nombre del archivo a
                                        // Activity_Cuestionario.
                                        Intent intent = new Intent(getActivity(), Activity_Cuestionario.class);
                                        intent.putExtra("nombre_archivo", binding.etNombreArchivo.getText().toString());
                                        // Recuperamos el dialogo abierto actualmente
                                        // (Fragment_DialogNuevoArchivo.java) y lo cerramos.
                                        Dialog dialogActual =  getDialog();
                                        dialogActual.dismiss();
                                        binding.etNombreArchivo.setText("");
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        dialog.dismiss();
                                    }
                                }).create().show();
                    } else { // Sino hay un archivo existente entra aquí
                        // Si hay un valor dentro del campo enviamos el nombre del archivo a
                        // Activity_Cuestionario.
                        if (binding.etNombreArchivo.getText().toString().isEmpty()){
                            Toast.makeText(getContext(),
                                    "Tienes que colocar un nombre para el archivo",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(getActivity(), Activity_Cuestionario.class);
                            intent.putExtra("nombre_archivo", binding.etNombreArchivo.getText().toString());
                            // Recuperamos el dialogo abierto actualmente
                            // (Fragment_DialogNuevoArchivo.java) y lo cerramos.
                            Dialog dialogActual =  getDialog();
                            dialogActual.dismiss();
                            binding.etNombreArchivo.setText("");
                            startActivity(intent);
                        }
                    }
                }

            }
        });
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
            dialog.getWindow().setLayout(700, 700);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}