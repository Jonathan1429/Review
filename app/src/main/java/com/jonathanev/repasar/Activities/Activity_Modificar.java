package com.jonathanev.repasar.Activities;

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

import androidx.appcompat.app.AppCompatActivity;

import com.jonathanev.repasar.MainActivity;
import com.jonathanev.repasar.R;
import com.jonathanev.repasar.databinding.ActivityModificarBinding;
import com.jonathanev.repasar.databinding.ActivityRepasarGuiaBinding;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Activity_Modificar extends AppCompatActivity {

    private ActivityModificarBinding binding;
    private String nombreArchivo;
    private ArrayList<String> preguntas = new ArrayList<>();
    private ArrayList<String> respuestas = new ArrayList<>();
    private ArrayList<String> preguntasModificadas = new ArrayList<>();
    private ArrayList<String> respuestasModificadas = new ArrayList<>();
    private int contadorPregunta = 0;
    private boolean noHayMasPreguntas = false;

    // Creamos la serialización y la clase para crear archivos de manera global.
    XmlSerializer serializer = Xml.newSerializer();
    FileOutputStream fos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityModificarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.barraSuperiorRegreso.tvTituloToolbar.setText("Modificación de guía");

        binding.barraSuperiorRegreso.imgvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Se ejecuta cuando se regresa sin guardar.
                new AlertDialog.Builder(Activity_Modificar.this)
                        .setTitle("¡Atención!")
                        .setMessage("Aún no terminas de modificar la guia, no se hará ningún" +
                                " cambio en el archivo, ¿seguro deseas continuar?")
                        .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
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
        binding.etRespuesta.setText(respuestas.get(contadorPregunta));

        binding.btnSiguientePregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validamos campos vacios en la pregunta o respuesta.
                if (!binding.etPregunta.getText().toString().isEmpty()
                        || !binding.etRespuesta.getText().toString().isEmpty()){
                    preguntasModificadas.add(binding.etPregunta.getText().toString());
                    respuestasModificadas.add(binding.etPregunta.getText().toString());

                    binding.btnGuardarGuiaInhabilitado.setVisibility(View.INVISIBLE);
                    binding.btnGuardarGuia.setVisibility(View.VISIBLE);
                    binding.etPregunta.setText("");
                    binding.etRespuesta.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), "Borraste la pregunta anterior" +
                            " de tu guía de estudio", Toast.LENGTH_LONG).show();
                }

                contadorPregunta++;
                int preguntasTotales = preguntas.size();

                // Validamos que haya mas preguntas, si las hay entra al método sino al else.
                if ((contadorPregunta+1)<=preguntasTotales){
                    binding.etPregunta.setText(preguntas.get(contadorPregunta));
                    binding.etRespuesta.setText(respuestas.get(contadorPregunta));
                } else {
                    noHayMasPreguntas = true;

                    // ¿Quieres agregar más preguntas?
                    new AlertDialog.Builder(Activity_Modificar.this)
                            .setTitle("¡Atención!")
                            .setMessage("Se acabaron las preguntas, ¿Quieres agregar mas preguntas?")
                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    binding.barraSuperiorRegreso.tvTituloToolbar.setText("Agrega más preguntas a la guía");
                                    binding.btnSiguientePregunta.setVisibility(View.INVISIBLE);
                                    binding.btnMasPreguntas.setVisibility(View.VISIBLE);
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

        binding.btnMasPreguntas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validamos campos vacios en la pregunta o respuesta.
                if (binding.etPregunta.getText().toString().isEmpty()
                        || binding.etRespuesta.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Asegurate de no dejar ningun campo vacio",
                            Toast.LENGTH_SHORT).show();
                } else {
                    preguntasModificadas.add(binding.etPregunta.getText().toString());
                    respuestasModificadas.add(binding.etPregunta.getText().toString());
                    binding.etPregunta.setText("");
                    binding.etRespuesta.setText("");
                    binding.etPregunta.requestFocus();
                }
            }
        });

        binding.btnGuardarGuia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validamos campos vacios en la pregunta o respuesta.
                if (!binding.etPregunta.getText().toString().isEmpty()
                        || !binding.etRespuesta.getText().toString().isEmpty()){
                    // Guardamos lo que se escriba en la pregunta y respuesta
                    preguntasModificadas.add(binding.etPregunta.getText().toString());
                    respuestasModificadas.add(binding.etRespuesta.getText().toString());
                }

                @SuppressLint("SdCardPath") File file = new File("/data/data/com.jonathanev.repasar/files/");
                if (file.exists()){
                    new File(file, nombreArchivo).delete();
                    Log.d("ArchivoEliminado", "Archivo eliminado");
                } else {
                    Log.d("ArchivoEliminado", "Archivo no eliminado");
                }

                //Vamos a crear el archivo aquí de manera de prueba
                try {
                    fos = openFileOutput(nombreArchivo, Context.MODE_PRIVATE);
                    serializer.setOutput(fos, "UTF-8");
                    serializer.startDocument(null, Boolean.valueOf(true));
                    serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                    serializer.startTag("", "GuiaEstudio");
                    serializer.attribute("", "version", "1.0");

                    serializer.startTag("", "Cuestionario");
                    serializer.attribute("", "nombreGuia", nombreArchivo);

                    // Creo la etiqueta interrogante con su respectiva pregunta
                    for (int i = 0; i < preguntasModificadas.size(); i++) {
                        serializer.startTag("", "Interrogante");
                            serializer.attribute("", "pregunta", preguntasModificadas.get(i));
                            serializer.attribute("", "respuesta", respuestasModificadas.get(i));
                        serializer.endTag("", "Interrogante");
                    }

                    // Si los campos estan vacios simplemente cierro las etiquetas y directamente
                    // guardo el documento en el teléfono.
                    serializer.endTag("", "Cuestionario");
                    serializer.endTag("", "GuiaEstudio");
                    serializer.endDocument();
                    serializer.flush();
                    fos.close();
                    Toast.makeText(getApplicationContext(), "Guia de estudio modificada exitosamente",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("nombre_archivo", nombreArchivo);
                    startActivity(intent);
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
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