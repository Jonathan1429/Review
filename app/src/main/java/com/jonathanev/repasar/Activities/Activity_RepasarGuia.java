package com.jonathanev.repasar.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jonathanev.repasar.databinding.ActivityRepasarGuiaBinding;

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
    private int contadorPregunta = 0;
    private boolean noHayMasPreguntas = false;

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
                if (!noHayMasPreguntas){
                    binding.etRespuesta.setText(respuestas.get(contadorPregunta));
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
                    noHayMasPreguntas = true;
                    Toast.makeText(getApplicationContext(), "Se acabaron las preguntas",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
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