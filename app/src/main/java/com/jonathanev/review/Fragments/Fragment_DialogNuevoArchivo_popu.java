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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.jonathanev.review.Activities.Activity_Cuestionario;
import com.jonathanev.review.R;
import com.jonathanev.review.databinding.FragmentNuevoArchivoBinding;

import java.io.File;

public class Fragment_DialogNuevoArchivo_popu extends DialogFragment{

    private FragmentNuevoArchivoBinding binding;

    public Fragment_DialogNuevoArchivo_popu() {
        // Required empty public constructor
    }

    public interface DialogListener {
        void onDialogClosed();
    }

    private DialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DialogListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getParentFragment().getClass() + " debe implementar la interfaz DialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    // En algún lugar del código donde cierres el dialogo, notifica al fragmento padre
    private void cerrarDialogo() {
        if (listener != null) {
            listener.onDialogClosed();
        }
        dismiss();
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

        SharedPreferences preferencias = getActivity().getSharedPreferences("cambiar_nombre", Context.MODE_PRIVATE);

        if (preferencias.getString("cambiar_nombre", "no existe").equals("sin nombre")){
            binding.btnGuardarGuiaEstudio.setText("Cambiar nombre");

            binding.btnGuardarGuiaEstudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!binding.etNombreArchivo.getText().toString().isEmpty()){
                        // Ruta + nombre del archivo.

                        SharedPreferences preferencias = getActivity().getSharedPreferences("nombre_archivo", Context.MODE_PRIVATE);

                        @SuppressLint("SdCardPath")
                        File archivo = new File("/data/data/com.jonathanev.review/files/"
                                + preferencias.getString("nombre_archivo", "no existe")
                                + ".xml");

                        if (archivo.exists()){
                            String item = "";
                            // Defino la ruta donde busco los ficheros.
                            @SuppressLint("SdCardPath") File file = new File("/data/data/com.jonathanev.review/files/");

                            boolean archivoExiste = false;
                            // Creo el array de tipo File con el contenido de la carpeta.
                            File[] files = file.listFiles();
                            File archivos = null;
                            // Hacemos un ciclo por cada fichero para extraer el nombre uno a uno.
                            for (int i = 0; i < files.length; i++){
                                // Sacamos del array files el nombre recuperandolo por posición.
                                archivos = files[i];
                                item = archivos.getName().replaceAll(".xml", "");
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
                                                // Si el usuario quiere continuar reemplazamos el archivo.
                                                File nuevoArchivo = new File(archivo.getParentFile(), binding.etNombreArchivo.getText().toString() + ".xml"); // Creamos el nuevo objeto File

                                                if (archivo.renameTo(nuevoArchivo)){
                                                    Toast.makeText(getContext(),"Archivo renombrado exitosamente",
                                                        Toast.LENGTH_SHORT).show();

                                                    cerrarDialogo();
                                                } else {
                                                    Toast.makeText(getContext(),"No se pudo renombrar el archivo",
                                                        Toast.LENGTH_SHORT).show();

                                                    cerrarDialogo();
                                                }
                                            }
                                        })
                                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int i) {
                                                dialog.dismiss();
                                            }
                                        }).create().show();
                            } else {
                                // Si el archivo no existe directamente pasamos a cambiar el nombre.
                                File nuevoArchivo = new File(archivo.getParentFile(), binding.etNombreArchivo.getText().toString() + ".xml"); // Creamos el nuevo objeto File

                                if (archivo.renameTo(nuevoArchivo)){
                                    Toast.makeText(getContext(),"Archivo renombrado exitosamente",
                                        Toast.LENGTH_SHORT).show();

                                    cerrarDialogo();
                                } else {
                                    Toast.makeText(getContext(),"No se pudo renombrar el archivo",
                                        Toast.LENGTH_SHORT).show();

                                    cerrarDialogo();
                                }
                            }
                        }
                    } else {
                        Toast.makeText(getContext(),"Ingresa un nuevo nombre",
                            Toast.LENGTH_SHORT).show();

                    }
                }
            });
        } else {
             binding.btnGuardarGuiaEstudio.setText("Guardar guía");

             binding.btnGuardarGuiaEstudio.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SdCardPath")
                @Override
                public void onClick(View view) {
                    String item = "";
                    // Defino la ruta donde busco los ficheros.
                    @SuppressLint("SdCardPath") File file = new File("/data/data/com.jonathanev.review/files/");

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