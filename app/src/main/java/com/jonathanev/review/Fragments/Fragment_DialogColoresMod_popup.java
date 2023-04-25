package com.jonathanev.review.Fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.jonathanev.review.Activities.Activity_Cuestionario;
import com.jonathanev.review.Activities.Activity_Modificar;
import com.jonathanev.review.databinding.FragmentColoresBinding;
import com.skydoves.colorpickerview.flag.BubbleFlag;
import com.skydoves.colorpickerview.flag.FlagMode;
import com.skydoves.colorpickerview.listeners.ColorListener;

public class Fragment_DialogColoresMod_popup extends DialogFragment {
    private FragmentColoresBinding binding;
    private int colorActual = 0;

    public Fragment_DialogColoresMod_popup() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentColoresBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BubbleFlag bubbleFlag = new BubbleFlag(getContext());
        bubbleFlag.setFlagMode(FlagMode.FADE);
        binding.colorPickerView.setFlagView(bubbleFlag);

        binding.colorPickerView.setColorListener(new ColorListener() {
            @Override
            public void onColorSelected(int color, boolean fromUser) {
                setLayoutColor(color);
                colorActual = color;

                Activity_Modificar activityModificar = (Activity_Modificar) getActivity();
                activityModificar.colorActual(colorActual);
            }
        });

        final Fragment fragment = this;
        binding.btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        });

        binding.btnDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                Intent intent = new Intent(getActivity(), Activity_Modificar.class);
                colorActual = Color.BLACK;

                Activity_Modificar activityModificar = (Activity_Modificar) getActivity();
                activityModificar.colorActual(colorActual);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setLayoutColor(int color) {
        binding.colorHexadecimal.setText("#"+ binding.colorPickerView.getColorEnvelope().getHexCode());
        binding.btnContinuar.setBackgroundColor(color);
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
            dialog.getWindow().setLayout(700, 1000);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
