package com.jonathanev.repasar.Activities;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.jonathanev.repasar.databinding.ActivityCuestionarioBinding;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Activity_Cuestionario extends AppCompatActivity {

    private ActivityCuestionarioBinding binding;
    private String nombreArchivo;
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
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
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
                });

        // Recibimos el nombre del archivo del popupFragment Nueva Guia.
        nombreArchivo = getIntent().getExtras().getString("nombre_archivo");

        // Creamos el archivo una vez que entra al Activity de cuestionario.
        crearArchivo(nombreArchivo);

        binding.barraSuperiorRegreso.imgvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    // Guardamos la pregunta y respuesta en una variable
                    String pregunta = binding.etPregunta.getText().toString();
                    String respuesta = binding.etRespuesta.getText().toString();

                    // Enviamos los valores al método para escribir en el XML.
                    escribirPreguntaXml(pregunta, respuesta);
                    binding.btnGuardarGuiaInhabilitado.setVisibility(View.INVISIBLE);
                    binding.btnGuardarGuia.setVisibility(View.VISIBLE);
                    binding.etPregunta.setText("");
                    binding.etRespuesta.setText("");
                    binding.etPregunta.requestFocus();
                }
            }
        });

        binding.btnGuardarGuia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Guardamos lo que se escriba en la pregunta y respuesta.
                String pregunta = binding.etPregunta.getText().toString();
                String respuesta = binding.etRespuesta.getText().toString();

                // Dentro del método valido si los campos son vacios.
                guardarXml(pregunta, respuesta);
            }
        });
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
                    // serializer.text("Hola");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void escribirPreguntaXml(String pregunta, String respuesta) {
        try {
                // Creo la etiqueta interrogante con su respectiva pregunta
                serializer.startTag("", "Interrogante");
                    serializer.attribute("", "pregunta", pregunta);
                    serializer.attribute("", "respuesta", respuesta);
                serializer.endTag("", "Interrogante");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void guardarXml(String pregunta, String respuesta){
        try {
            // Valido que ambos campos esten vacios o llenos sino envío un mensaje.
            if (pregunta.isEmpty() && !respuesta.isEmpty() ||
            respuesta.isEmpty() && !pregunta.isEmpty()){
                Toast.makeText(getApplicationContext(),
                        "Llena o vacía ambos campos para poder continuar",
                        Toast.LENGTH_SHORT).show();
            } else{
                // Aquí verifico que cuando le de a guardar los campos no se encuentren vacios,
                // si ambos campos están con valores lo guardo sino se ignora.
                if (!pregunta.isEmpty() && !respuesta.isEmpty()){
                    serializer.startTag("", "Interrogante");
                    serializer.attribute("", "pregunta", pregunta);
                    serializer.attribute("", "respuesta", respuesta);
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}