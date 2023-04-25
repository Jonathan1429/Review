package com.jonathanev.review.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Xml;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.jonathanev.review.Clases.ColoresPregunta;
import com.jonathanev.review.Fragments.Fragment_DialogColores_popup;
import com.jonathanev.review.R;
import com.jonathanev.review.databinding.ActivityCuestionarioBinding;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Activity_Cuestionario extends AppCompatActivity {

    private ActivityCuestionarioBinding binding;
    private String nombreArchivo;
    private int colorActual = 0;
    private int contColorPreg = 0;
    private int contColorResp = 0;
    private ArrayList<String> preguntas = new ArrayList<>();
    private ArrayList<String> respuestas = new ArrayList<>();
    private ArrayList<ColoresPregunta> preguntasColor = new ArrayList<>();
    private ArrayList<ColoresPregunta> respuestasColor = new ArrayList<>();
    SpannableStringBuilder builder;
    int contador = 0;
    private InterstitialAd mInterstitialAd;

    // Creamos la serialización y la clase para crear archivos de manera global.
    XmlSerializer serializer = Xml.newSerializer();
    FileOutputStream fos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCuestionarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Sección de anuncios
        /*MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();

        // Anuncio de prueba: ca-app-pub-3940256099942544/1033173712
        // Anuncio de GoogleAdmob: ca-app-pub-5116088101296740/2671658549
        InterstitialAd.load(this,"ca-app-pub-5116088101296740/2671658549", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });*/

        // Recibimos el nombre del archivo del popupFragment Nueva Guia.
        nombreArchivo = getIntent().getExtras().getString("nombre_archivo");

        colorActual = Color.BLACK;
        colorActual(colorActual);

        binding.barraSuperiorRegreso.imgvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelarArchivo();
            }
        });

        binding.imgvColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
                Fragment_DialogColores_popup dialogo = new Fragment_DialogColores_popup();
                dialogo.show(getSupportFragmentManager(), "FragmentColor");
            }
        });

        binding.btnAtrasPregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // El contador tiene que ser mayor a 0 sino significa que no hay preguntas anteriores.
                if (contador > 0){
                    // Se le quita 1 para hacer referencia al arreglo
                    // tamaño 3-1 = 2 [0,1,2].
                    int longi = preguntas.size()-1;

                    // Contador tendrá acceso a modificar lo que esté en el rango a excepción
                    // de lo que esté en la posición 0.
                    if (contador <= longi){
                        // Validamos campos vacios en la pregunta y respuesta.
                        if (binding.etPregunta.getText().toString().isEmpty()
                                || binding.etRespuesta.getText().toString().isEmpty()){
                            Toast.makeText(getApplicationContext(),
                                    "Asegurate de no dejar ningun campo vacio",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Si los campos están bien se sobre escribe.
                            String cadOriginal = binding.etPregunta.getText().toString();
                            StringBuilder sb = new StringBuilder(cadOriginal);
                            int desplazamiento = 0; // variable sumar el desplazamiento de palabras

                            // Si hay colores anteriormente asignados en las palabras entra aquí.
                            // Preguntas.
                            for (ColoresPregunta coloresPregunta: preguntasColor) {
                                String palabra = cadOriginal.substring(coloresPregunta.getInicioColor(), coloresPregunta.getFinColor());
                                int inicio = coloresPregunta.getInicioColor() + desplazamiento;
                                int fin = coloresPregunta.getFinColor() + desplazamiento;
                                int color = coloresPregunta.getColor();

                                sb.replace(inicio, fin, "«"+color+"»"+palabra+"«/"+color+"»");
                                String longColor = String.valueOf(color);
                                int caractFijos = 5; // «»«/»  "«"+color+"»"+palabra+"«/"+color+"»"
                                desplazamiento +=  (longColor.length()*2) + caractFijos;
                            }

                            preguntas.set(contador, sb.toString());

                            cadOriginal = binding.etRespuesta.getText().toString();
                            sb = new StringBuilder(cadOriginal);
                            desplazamiento = 0; // variable sumar el desplazamiento de palabras

                            // Si hay colores anteriormente asignados en las palabras entra aquí.
                            // Respuestas.
                            for (ColoresPregunta coloresRespuesta: respuestasColor) {
                                String palabra = cadOriginal.substring(coloresRespuesta.getInicioColor(), coloresRespuesta.getFinColor());
                                int inicio = coloresRespuesta.getInicioColor() + desplazamiento;
                                int fin = coloresRespuesta.getFinColor() + desplazamiento;
                                int color = coloresRespuesta.getColor();

                                sb.replace(inicio, fin, "«"+color+"»"+palabra+"«/"+color+"»");
                                String longColor = String.valueOf(color);
                                int caractFijos = 5; // «»«/»  "«"+color+"»"+palabra+"«/"+color+"»"
                                desplazamiento +=  (longColor.length()*2) + caractFijos;
                            }
                            respuestas.set(contador, sb.toString());
                            preguntasColor.clear();
                            respuestasColor.clear();
                            contColorPreg = 0;
                            contColorResp = 0;

                            // Se borran las etiquetas de colores.
                            String texto = "";
                            texto = preguntas.get(contador-1).replaceAll("«.*?»", "");
                            binding.etPregunta.setText(texto);
                            texto = respuestas.get(contador-1).replaceAll("«.*?»", "");
                            binding.etRespuesta.setText(texto);
                        }
                    } else {
                        // Si el contador es mayor a lo que hay guardado entonces únicamente
                        // escribirá en los textos y se borran las etiquetas de colores.
                        String texto = "";
                        texto = preguntas.get(contador-1).replaceAll("«.*?»", "");
                        binding.etPregunta.setText(texto);
                        texto = respuestas.get(contador-1).replaceAll("«.*?»", "");
                        binding.etRespuesta.setText(texto);
                    }
                    contador--;
                } else {
                    Toast.makeText(getApplicationContext(), "Ya no tienes preguntas anteriores"
                            , Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.btnSiguientePregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validamos campos vacios en la pregunta o respuesta.
                if (binding.etPregunta.getText().toString().isEmpty()
                        || binding.etRespuesta.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Asegurate de no dejar ningun campo vacio",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Se le quita 1 para hacer referencia al arreglo
                    // tamaño 3-1 = 2 [0,1,2].
                    int longi = preguntas.size()-1;

                    // Contador tendrá acceso a modificar lo que esté en el rango a excepción
                    // de lo que esté en la posición 0.
                    if (contador <= longi){
                        String cadOriginal = binding.etPregunta.getText().toString();
                        StringBuilder sb = new StringBuilder(cadOriginal);
                        int desplazamiento = 0; // variable sumar el desplazamiento de palabras

                        // Si hay colores anteriormente asignados en las palabras entra aquí.
                        // Preguntas.
                        for (ColoresPregunta coloresPregunta: preguntasColor) {
                            String palabra = cadOriginal.substring(coloresPregunta.getInicioColor(), coloresPregunta.getFinColor());
                            int inicio = coloresPregunta.getInicioColor() + desplazamiento;
                            int fin = coloresPregunta.getFinColor() + desplazamiento;
                            int color = coloresPregunta.getColor();

                            sb.replace(inicio, fin, "«"+color+"»"+palabra+"«/"+color+"»");
                            String longColor = String.valueOf(color);
                            int caractFijos = 5; // «»«/»  "«"+color+"»"+palabra+"«/"+color+"»"
                            desplazamiento +=  (longColor.length()*2) + caractFijos;
                        }

                        preguntas.set(contador, sb.toString());

                        cadOriginal = binding.etRespuesta.getText().toString();
                        sb = new StringBuilder(cadOriginal);
                        desplazamiento = 0; // variable sumar el desplazamiento de palabras

                        // Si hay colores anteriormente asignados en las palabras entra aquí.
                        // Respuestas.
                        for (ColoresPregunta coloresRespuesta: respuestasColor) {
                            String palabra = cadOriginal.substring(coloresRespuesta.getInicioColor(), coloresRespuesta.getFinColor());
                            int inicio = coloresRespuesta.getInicioColor() + desplazamiento;
                            int fin = coloresRespuesta.getFinColor() + desplazamiento;
                            int color = coloresRespuesta.getColor();

                            sb.replace(inicio, fin, "«"+color+"»"+palabra+"«/"+color+"»");
                            String longColor = String.valueOf(color);
                            int caractFijos = 5; // «»«/»  "«"+color+"»"+palabra+"«/"+color+"»"
                            desplazamiento +=  (longColor.length()*2) + caractFijos;
                        }
                        respuestas.set(contador, sb.toString());
                        preguntasColor.clear();
                        respuestasColor.clear();
                        contColorPreg = 0;
                        contColorResp = 0;

                        // Mientras el contador sea menor escribiremos la siguiente pregunta
                        // en los et y se borran las etiquetas de colores.
                        if (contador < longi){
                            // Borramos las etiquetas que se pondrán en los et.
                            String texto = "";
                            texto = preguntas.get(contador+1).replaceAll("«.*?»", "");
                            binding.etPregunta.setText(texto);
                            texto = respuestas.get(contador+1).replaceAll("«.*?»", "");
                            binding.etRespuesta.setText(texto);
                        } else {
                            // Si el contador es igual entonces solo escribiremos los campos vacios.
                            binding.etPregunta.setText("");
                            binding.etRespuesta.setText("");
                            binding.etPregunta.requestFocus();
                        }
                    } else {
                        // Si el contador es mayor entonces agregaremos la pregunta actual a los
                        // arreglos.«»
                        String cadOriginal = binding.etPregunta.getText().toString();
                        StringBuilder sb = new StringBuilder(cadOriginal);
                        int desplazamiento = 0; // variable sumar el desplazamiento de palabras

                        // Si hay colores anteriormente asignados en las palabras entra aquí.
                        // Preguntas.
                        for (ColoresPregunta coloresPregunta: preguntasColor) {
                            String palabra = cadOriginal.substring(coloresPregunta.getInicioColor(), coloresPregunta.getFinColor());
                            int inicio = coloresPregunta.getInicioColor() + desplazamiento;
                            int fin = coloresPregunta.getFinColor() + desplazamiento;
                            int color = coloresPregunta.getColor();

                            sb.replace(inicio, fin, "«"+color+"»"+palabra+"«/"+color+"»");
                            String longColor = String.valueOf(color);
                            int caractFijos = 5; // «»«/»  "«"+color+"»"+palabra+"«/"+color+"»"
                            desplazamiento +=  (longColor.length()*2) + caractFijos;
                        }

                        preguntas.add(contador, sb.toString());

                        cadOriginal = binding.etRespuesta.getText().toString();
                        sb = new StringBuilder(cadOriginal);
                        desplazamiento = 0; // variable sumar el desplazamiento de palabras

                        // Si hay colores anteriormente asignados en las palabras entra aquí.
                        // Respuestas.
                        for (ColoresPregunta coloresRespuesta: respuestasColor) {
                            String palabra = cadOriginal.substring(coloresRespuesta.getInicioColor(), coloresRespuesta.getFinColor());
                            int inicio = coloresRespuesta.getInicioColor() + desplazamiento;
                            int fin = coloresRespuesta.getFinColor() + desplazamiento;
                            int color = coloresRespuesta.getColor();

                            sb.replace(inicio, fin, "«"+color+"»"+palabra+"«/"+color+"»");
                            String longColor = String.valueOf(color);
                            int caractFijos = 5; // «»«/»  "«"+color+"»"+palabra+"«/"+color+"»"
                            desplazamiento +=  (longColor.length()*2) + caractFijos;
                        }
                        respuestas.add(contador, sb.toString());
                        preguntasColor.clear();
                        respuestasColor.clear();
                        contColorPreg = 0;
                        contColorResp = 0;

                        binding.etPregunta.setText("");
                        binding.etRespuesta.setText("");
                        binding.etPregunta.requestFocus();
                    }
                    contador++;
                }
            }
        });

        binding.btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Activity_Cuestionario.this)
                        .setTitle("¡Atención!")
                        .setMessage("¿Quieres eliminar la pregunta?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Se le quita 1 para hacer referencia al arreglo
                                // tamaño 3-1 = 2 [0,1,2].
                                int longi = preguntas.size()-1;

                                // Contador tendrá acceso a modificar lo que esté en el rango a excepción
                                // de lo que esté en la posición 0.
                                if (contador <= longi){
                                    preguntas.remove(contador);
                                    respuestas.remove(contador);

                                    // Mientras el contador sea menor escribiremos la siguiente pregunta
                                    // en los et.
                                    if (contador < longi){
                                        binding.etPregunta.setText(preguntas.get(contador));
                                        binding.etRespuesta.setText(respuestas.get(contador));
                                    } else {
                                        // Si el contador es igual entonces solo escribiremos los campos vacios.
                                        binding.etPregunta.setText("");
                                        binding.etRespuesta.setText("");
                                        binding.etPregunta.requestFocus();
                                    }
                                } else {
                                    // Si el contador es mayor entonces únicamente limpiamos los campos.
                                    binding.etPregunta.setText("");
                                    binding.etRespuesta.setText("");
                                    binding.etPregunta.requestFocus();
                                }
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        binding.btnGuardarGuia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Se le quita 1 para hacer referencia al arreglo
                // tamaño 3-1 = 2 [0,1,2].
                int longi = preguntas.size()-1;

                // Si alguno de los dos campos está vacio entra aquí.
                if (!binding.etPregunta.getText().toString().isEmpty() &&
                    binding.etRespuesta.getText().toString().isEmpty() ||
                    binding.etPregunta.getText().toString().isEmpty() &&
                    !binding.etRespuesta.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Asegurate de no dejar ningún campo vacio",
                            Toast.LENGTH_SHORT).show();
                } else if (binding.etPregunta.getText().toString().isEmpty()
                        && binding.etRespuesta.getText().toString().isEmpty()){
                    // Si los dos campos están vacios entra aquí.

                    // Si queremos guardar con campos vacios y no hay preguntas anteriores guardadas
                    // entra aquí.
                    if (contador == 0){
                        Toast.makeText(getApplicationContext(),
                                "¡No puedes guardar una guía sin datos!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Si queremos guardar con campos vacios y hay preguntas anteriores
                        // guardadas entra aquí.
                        crearArchivo(nombreArchivo);
                    }
                } else if (contador > longi){
                    // Si el contador es mayor a lo guardado entonces agregamos la pregunta
                    // anteriormente ya validamos campos vacios.
                    String cadOriginal = binding.etPregunta.getText().toString();
                    StringBuilder sb = new StringBuilder(cadOriginal);
                    int desplazamiento = 0; // variable sumar el desplazamiento de palabras

                    // Si hay colores anteriormente asignados en las palabras entra aquí.
                    // Preguntas.
                    for (ColoresPregunta coloresPregunta: preguntasColor) {
                        String palabra = cadOriginal.substring(coloresPregunta.getInicioColor(), coloresPregunta.getFinColor());
                        int inicio = coloresPregunta.getInicioColor() + desplazamiento;
                        int fin = coloresPregunta.getFinColor() + desplazamiento;
                        int color = coloresPregunta.getColor();

                        sb.replace(inicio, fin, "«"+color+"»"+palabra+"«/"+color+"»");
                        String longColor = String.valueOf(color);
                        int caractFijos = 5; // «»«/»  "«"+color+"»"+palabra+"«/"+color+"»"
                        desplazamiento +=  (longColor.length()*2) + caractFijos;
                    }

                    preguntas.add(contador, sb.toString());

                    cadOriginal = binding.etRespuesta.getText().toString();
                    sb = new StringBuilder(cadOriginal);
                    desplazamiento = 0; // variable sumar el desplazamiento de palabras

                    // Si hay colores anteriormente asignados en las palabras entra aquí.
                    // Respuestas.
                    for (ColoresPregunta coloresRespuesta: respuestasColor) {
                        String palabra = cadOriginal.substring(coloresRespuesta.getInicioColor(), coloresRespuesta.getFinColor());
                        int inicio = coloresRespuesta.getInicioColor() + desplazamiento;
                        int fin = coloresRespuesta.getFinColor() + desplazamiento;
                        int color = coloresRespuesta.getColor();

                        sb.replace(inicio, fin, "«"+color+"»"+palabra+"«/"+color+"»");
                        String longColor = String.valueOf(color);
                        int caractFijos = 5; // «»«/»  "«"+color+"»"+palabra+"«/"+color+"»"
                        desplazamiento +=  (longColor.length()*2) + caractFijos;
                    }
                    respuestas.add(contador, sb.toString());
                    preguntasColor.clear();
                    respuestasColor.clear();
                    contColorPreg = 0;
                    contColorResp = 0;
                    crearArchivo(nombreArchivo);
                } else {
                    // Si el contador no es mayor a lo guardado entonces modificamos lo actual en
                    // el arreglo, además anteriormente ya validamos campos vacios.
                    String cadOriginal = binding.etPregunta.getText().toString();
                    StringBuilder sb = new StringBuilder(cadOriginal);
                    int desplazamiento = 0; // variable sumar el desplazamiento de palabras

                    // Si hay colores anteriormente asignados en las palabras entra aquí.
                    // Preguntas.
                    for (ColoresPregunta coloresPregunta: preguntasColor) {
                        String palabra = cadOriginal.substring(coloresPregunta.getInicioColor(), coloresPregunta.getFinColor());
                        int inicio = coloresPregunta.getInicioColor() + desplazamiento;
                        int fin = coloresPregunta.getFinColor() + desplazamiento;
                        int color = coloresPregunta.getColor();

                        sb.replace(inicio, fin, "«"+color+"»"+palabra+"«/"+color+"»");
                        String longColor = String.valueOf(color);
                        int caractFijos = 5; // «»«/»  "«"+color+"»"+palabra+"«/"+color+"»"
                        desplazamiento +=  (longColor.length()*2) + caractFijos;
                    }

                    preguntas.set(contador, sb.toString());

                    cadOriginal = binding.etRespuesta.getText().toString();
                    sb = new StringBuilder(cadOriginal);
                    desplazamiento = 0; // variable sumar el desplazamiento de palabras

                    // Si hay colores anteriormente asignados en las palabras entra aquí.
                    // Respuestas.
                    for (ColoresPregunta coloresRespuesta: respuestasColor) {
                        String palabra = cadOriginal.substring(coloresRespuesta.getInicioColor(), coloresRespuesta.getFinColor());
                        int inicio = coloresRespuesta.getInicioColor() + desplazamiento;
                        int fin = coloresRespuesta.getFinColor() + desplazamiento;
                        int color = coloresRespuesta.getColor();

                        sb.replace(inicio, fin, "«"+color+"»"+palabra+"«/"+color+"»");
                        String longColor = String.valueOf(color);
                        int caractFijos = 5; // «»«/»  "«"+color+"»"+palabra+"«/"+color+"»"
                        desplazamiento +=  (longColor.length()*2) + caractFijos;
                    }
                    respuestas.set(contador, sb.toString());
                    preguntasColor.clear();
                    respuestasColor.clear();
                    contColorPreg = 0;
                    contColorResp = 0;
                    crearArchivo(nombreArchivo);
                }
            }
        });

        binding.etPregunta.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                // Inflar el menú personalizado
                actionMode.getMenuInflater().inflate(R.menu.menu_personalizado, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.color:
                        // Poner un color al rango marcado
                        int start = binding.etPregunta.getSelectionStart();
                        int end = binding.etPregunta.getSelectionEnd();
                        Editable text = binding.etPregunta.getText();

                        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
                        spannableStringBuilder.setSpan(new ForegroundColorSpan(colorActual), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        binding.etPregunta.setText(spannableStringBuilder);

                        ColoresPregunta coloresPregunta = new ColoresPregunta(start, end, colorActual);
                        preguntasColor.add(contColorPreg, coloresPregunta);
                        contColorPreg++;

                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });

        binding.etRespuesta.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                // Inflar el menú personalizado
                actionMode.getMenuInflater().inflate(R.menu.menu_personalizado, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.color:
                        // Poner un color al rango marcado
                        int start = binding.etRespuesta.getSelectionStart();
                        int end = binding.etRespuesta.getSelectionEnd();
                        Editable text = binding.etRespuesta.getText();

                        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
                        spannableStringBuilder.setSpan(new ForegroundColorSpan(colorActual), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        binding.etRespuesta.setText(spannableStringBuilder);

                        ColoresPregunta coloresPregunta = new ColoresPregunta(start, end, colorActual);
                        respuestasColor.add(contColorResp, coloresPregunta);
                        contColorResp++;

                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });
    }

    // Método que se ejecuta cuando el back del telefono es presionado.
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            cancelarArchivo();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // Mostrará un mensaje diciendo que el archivo se eliminará ya que no se terminó de crear.
    private void cancelarArchivo(){
        // Se ejecuta cuando se regresa sin guardar.
        new AlertDialog.Builder(Activity_Cuestionario.this)
                .setTitle("¡Atención!")
                .setMessage("Aún no terminas de crear la guia, se borrará el " +
                        "archivo creado, ¿seguro deseas continuar?")
                .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Si el archivo se creó y existe, se elimina y te informa en consola
                        @SuppressLint("SdCardPath") File file = new File("/data/data/com.jonathanev.repasar/files/");
                        if (file.exists()){
                            new File(file, nombreArchivo+".xml").delete();
                            Log.d("ArchivoEliminado", "Archivo eliminado");
                        } else {
                            Log.d("ArchivoEliminado", "Archivo no eliminado");
                        }
                        onBackPressed();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    // Creamos el archivo en el dispositivo e inicializamos algunas etiquetas.
    private void crearArchivo(String nombreArchivo) {
        try {
            fos = openFileOutput(nombreArchivo+".xml", Context.MODE_PRIVATE);
            serializer.setOutput(fos, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag("", "GuiaEstudio");
                serializer.attribute("", "version", "1.0");

                serializer.startTag("", "Cuestionario");
                    serializer.attribute("", "nombreGuia", nombreArchivo);
                    // Creo la etiqueta interrogante con su respectiva pregunta
                    for (int i = 0; i < preguntas.size(); i++) {
                        serializer.startTag("", "Interrogante");
                        serializer.attribute("", "pregunta", preguntas.get(i));
                        serializer.attribute("", "respuesta", respuestas.get(i));
                        serializer.endTag("", "Interrogante");
                    }
                // Si los campos estan vacios simplemente cierro las etiquetas y directamente
                // guardo el documento en el teléfono.
                serializer.endTag("", "Cuestionario");
            serializer.endTag("", "GuiaEstudio");
            serializer.endDocument();
            serializer.flush();
            fos.close();
            Toast.makeText(getApplicationContext(), "Guia de estudio creada exitosamente",
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getApplicationContext(), Activity_RepasarGuia.class);
            intent.putExtra("nombre_archivo", nombreArchivo);
            startActivity(intent);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void colorActual(int colorActual){
        @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = getResources().getDrawable(R.drawable.boton_redondo);
        drawable.setColorFilter(colorActual, PorterDuff.Mode.SRC_ATOP);

        binding.btnColorActual.setBackground(drawable);

        // Recibimos el nombre del archivo del popupFragment Nueva Guia.
        this.colorActual = colorActual;
    }
}