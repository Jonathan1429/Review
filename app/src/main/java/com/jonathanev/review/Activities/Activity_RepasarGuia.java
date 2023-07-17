package com.jonathanev.review.Activities;

import static com.google.android.gms.ads.AdRequest.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.jonathanev.review.Clases.ColoresPregunta;
import com.jonathanev.review.databinding.ActivityRepasarGuiaBinding;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Activity_RepasarGuia extends AppCompatActivity {

    private ActivityRepasarGuiaBinding binding;
    private String nombreArchivo;

    private ArrayList<String> preguntas = new ArrayList<>();
    private ArrayList<String> respuestas = new ArrayList<>();
    private ArrayList<ColoresPregunta> preguntasColor = new ArrayList<>();
    private ArrayList<ColoresPregunta> respuestasColor = new ArrayList<>();

    private int contadorPregunta = 0;
    SpannableStringBuilder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRepasarGuiaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Anuncios publicitarios Banners.
        /*MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdRequest adRequest = new Builder().build();
        binding.adView.loadAd(adRequest);
        binding.adView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        });*/

        // Reutilizo el layout anteriormente creado y le asigno un texto el tvTituloToolbar
        binding.barraSuperiorRegreso.tvTituloToolbar.setText("Guia");

        binding.barraSuperiorRegreso.imgvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Guardo el nombre del archivo enviado desde el popupFragmentListarGuias.
        nombreArchivo = getIntent().getExtras().getString("nombre_archivo");

        // Aquí simplemente nos aseguramos que tenga el xml, si lo tiene no entramos, sino si.
        // En teoría todos los archivos lo van a tener.
        if (!nombreArchivo.contains(".xml")){
            nombreArchivo = getIntent().getExtras().getString("nombre_archivo")+".xml";
        }

        // Obtenemos los datos del XML y los guardamos en su respectivo ArrayList.
        obtenerDatosXML();

        // Pintamos el texto del contador actual.
        pintarTexto();

        // Mientras noHayMasPreguntas sea falso entrará al método.
        binding.btnMostrarRespuesta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.btnMostrarRespuesta.getText().toString().equals("Mostrar respuesta")){
                    binding.btnMostrarRespuesta.setText("Ocultar respuesta");
                    mostrarRespuesta(builder);
                } else {
                     binding.btnMostrarRespuesta.setText("Mostrar respuesta");
                     binding.etRespuesta.setText("");
                }
            }
        });

        binding.btnAtrasPregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnMostrarRespuesta.setText("Mostrar respuesta");
                if(contadorPregunta == 0){
                    Toast.makeText(getApplicationContext(), "No tienes preguntas anteriores",
                            Toast.LENGTH_SHORT).show();
                } else {
                    contadorPregunta--;
                    binding.etPregunta.setText("");
                    binding.etRespuesta.setText("");

                    // Pintamos el valor anterior de colores.
                    pintarTexto();
                }
            }
        });

        binding.btnSiguientePregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contadorPregunta++;
                binding.btnMostrarRespuesta.setText("Mostrar respuesta");
                int preguntasTotales = preguntas.size();

                // Validamos que haya mas preguntas, si las hay entra al método sino al else.
                if ((contadorPregunta+1)<=preguntasTotales){
                    // Pintamos el valor siguiente con colores.
                    pintarTexto();
                    binding.etRespuesta.setText("");
                } else {
                    contadorPregunta--;
                    // noHayMasPreguntas = true;
                    // Se ejecuta cuando se regresa sin guardar.
                    new AlertDialog.Builder(Activity_RepasarGuia.this)
                            .setTitle("¡Atención!")
                            .setMessage("Se acabaron las preguntas, ¿Quieres repetir la guia?")
                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    contadorPregunta = 0;
                                    binding.etPregunta.setText("");
                                    binding.etRespuesta.setText("");

                                    // Mostramos el primer valor de la pregunta pintado.
                                    pintarTexto();
                                    //binding.etPregunta.setText(preguntas.get(contadorPregunta));
                                }
                            })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }
            }
        });
    }

    private void pintarTexto() {
        int contColorPreg = 0;
        int contColorResp = 0;
        int inicio = 0;
        int fin = 0;
        ColoresPregunta coloresPregunta = null;

        String texto = preguntas.get(contadorPregunta);

        while (texto.contains("«")) {
            inicio = texto.indexOf("«") + 1;
            fin = texto.indexOf("»");
            String color = texto.substring(inicio, fin);
            int longColor = color.length();
            int colEntero = Integer.parseInt(color);
            inicio = fin + 1;
            fin = texto.indexOf("«", inicio);

            coloresPregunta = new ColoresPregunta((inicio-longColor-2), (fin-longColor-2), colEntero);
            preguntasColor.add(contColorPreg, coloresPregunta);
            // Eliminar la primera etiqueta y su contenido
            texto = texto.replaceFirst("«.*?»", "");

            // Eliminar la segunda etiqueta y su contenido
            texto = texto.replaceFirst("«.*?»", "");

            contColorPreg++;
        }

        builder = new SpannableStringBuilder(texto);
        for (ColoresPregunta coloresPreguntas : preguntasColor) {
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(coloresPreguntas.getColor());
            builder.setSpan(colorSpan, coloresPreguntas.getInicioColor(), coloresPreguntas.getFinColor(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.etPregunta.setText(builder);

        texto = respuestas.get(contadorPregunta);

        while (texto.contains("«")) {
            inicio = texto.indexOf("«") + 1;
            fin = texto.indexOf("»");
            String color = texto.substring(inicio, fin);
            int longColor = color.length();
            int colEntero = Integer.parseInt(color);
            inicio = fin + 1;
            fin = texto.indexOf("«", inicio);

            coloresPregunta = new ColoresPregunta((inicio-longColor-2), (fin-longColor-2), colEntero);
            respuestasColor.add(contColorResp, coloresPregunta);
            // Eliminar la primera etiqueta y su contenido
            texto = texto.replaceFirst("«.*?»", "");

            // Eliminar la segunda etiqueta y su contenido
            texto = texto.replaceFirst("«.*?»", "");

            contColorResp++;
        }

        builder = new SpannableStringBuilder(texto);
        for (ColoresPregunta coloresPreguntas : respuestasColor) {
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(coloresPreguntas.getColor());
            builder.setSpan(colorSpan, coloresPreguntas.getInicioColor(), coloresPreguntas.getFinColor(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        preguntasColor.clear();
        respuestasColor.clear();
    }

    private void mostrarRespuesta(SpannableStringBuilder builder){
        binding.etRespuesta.setText(builder);
    }

    private void obtenerDatosXML() {
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;

        try {
            db = dbf.newDocumentBuilder();
            FileInputStream fis = openFileInput(nombreArchivo);
            doc = db.parse(fis);

            // Buscamos los Nodos Interrogante y accedemos a lo que se encuentre dentro.
            NodeList cuestionario = doc.getElementsByTagName("Interrogante");

            for (int i = 0; i < cuestionario.getLength(); i++) {
                // Accedes a los elementos de dicho nodo
                Element e = (Element) cuestionario.item(i);

                preguntas.add(i, e.getAttribute("pregunta"));
                respuestas.add(i, e.getAttribute("respuesta"));
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}