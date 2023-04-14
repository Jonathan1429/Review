package com.jonathanev.review.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jonathanev.review.databinding.ActivityRepasarGuiaBinding;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Activity_RepasarGuia extends AppCompatActivity {

    private ActivityRepasarGuiaBinding binding;
    private String nombreArchivo;
    private ArrayList<String> preguntas = new ArrayList<>();
    private ArrayList<String> respuestas = new ArrayList<>();
    private List<String> palabras =
            Arrays.asList("REPLACE", "WITH", "INTO", "TO", "ADD", "SUBTRACT","TYPE", "DATA", "WRITE",
                    "FROM", "MULTIPLY", "BY", "CLEAR", "RESPECTING BLANKS", "INTO", "SPACE", "TRANSLATE",
                    "CASE", "UPPER", "LOWER", "IF", "ELSEIF", "ENDIF", "CONCATENATE", "SEPARATED", "BY",
                    "SPACE", "SPLIT", "TABLE", "SELECT", "WHERE", "EQ", "NE", "FORM", "ENDFORM", "CLASS",
                    "ENDCLASS", "INSERT", "SINGLE", "ELSE");

    private int contadorPregunta = 0;
    SpannableStringBuilder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRepasarGuiaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        // Pintamos el primer valor de la pregunta.
        binding.etPregunta.setText(preguntas.get(contadorPregunta));

        // Mientras noHayMasPreguntas sea falso entrará al método.
        binding.btnMostrarRespuesta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pintarPalabras();
                binding.etRespuesta.setText(builder);
            }
        });

        binding.btnAtrasPregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(contadorPregunta == 0){
                    Toast.makeText(getApplicationContext(), "No tienes preguntas anteriores",
                            Toast.LENGTH_SHORT).show();
                } else {
                    contadorPregunta--;
                    binding.etPregunta.setText("");
                    binding.etRespuesta.setText("");
                    // Pintamos el primer valor de la pregunta.
                    binding.etPregunta.setText(preguntas.get(contadorPregunta));
                }
            }
        });

        binding.btnSiguientePregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contadorPregunta++;
                int preguntasTotales = preguntas.size();

                // Validamos que haya mas preguntas, si las hay entra al método sino al else.
                if ((contadorPregunta+1)<=preguntasTotales){
                    binding.etPregunta.setText(preguntas.get(contadorPregunta));
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
                                    // Pintamos el primer valor de la pregunta.
                                    binding.etPregunta.setText(preguntas.get(contadorPregunta));
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

    private void pintarPalabras() {
         builder = new SpannableStringBuilder(respuestas.get(contadorPregunta).toUpperCase());

        /*for (String palabra : palabras) {
            palabra = palabra.toUpperCase();
            String text = builder.toString();
            int startIndex = text.indexOf(palabra);

            while (startIndex != -1) {
                int endIndex = startIndex + palabra.length();

                ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#1E61E8"));
                builder.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                startIndex = text.indexOf(palabra, endIndex);
            }
        }*/

        String textoCompletoAzul = builder.toString();
        String regex = "\\b(" + TextUtils.join("|", palabras) + ")\\b";

        Pattern patternAzul = Pattern.compile(regex);
        Matcher matcherAzul = patternAzul.matcher(textoCompletoAzul );
        while (matcherAzul.find()) {
            int startIndex = matcherAzul.start();
            int endIndex = matcherAzul.end();

            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#1E61E8"));
            builder.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.etRespuesta.setText(builder);

        String textoCompletoVerde = builder.toString();
        Pattern patternVerde = Pattern.compile("'([^']+)'");
        Matcher matcherVerde = patternVerde.matcher(textoCompletoVerde);
        while (matcherVerde.find()) {
            int startIndex = matcherVerde.start();
            int endIndex = matcherVerde.end();

            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#3FEA2D"));
            builder.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
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
                // Obtienes el nodo actual y lo guardamos en info.
                // Este no lo utilizamos ya que arriba ya accedimos al ultimo Nodo
                // Node info = cuestionario.item(i);

                // Accedes a los elmentos de dicho nodo
                Element e = (Element) cuestionario.item(i);

                // Guardo cada uno de los valores en su respectivo arreglo.
                preguntas.add(e.getAttribute("pregunta"));
                respuestas.add(e.getAttribute("respuesta"));
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}