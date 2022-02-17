package com.example.repasar.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.repasar.Activities.Activity_Cuestionario;
import com.example.repasar.databinding.FragmentNuevoArchivoBinding;

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
            @Override
            public void onClick(View view) {
                // Verificamos que el campo no se encuentre vacio.
                if (binding.etNombreArchivo.getText().toString().isEmpty()){
                    Toast.makeText(getContext(), "El campo no puede estar vacio",
                            Toast.LENGTH_SHORT).show();
                } else { // Si hay un valor dentro del campo enviamos el nombre del archivo a
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