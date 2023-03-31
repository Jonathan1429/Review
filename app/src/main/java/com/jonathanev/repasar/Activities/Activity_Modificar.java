package com.jonathanev.repasar.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
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
    private boolean sinModificadas = true;
    private boolean agregarPreguntas = false;

    // Creamos la serialización y la clase para crear archivos de manera global.
    XmlSerializer serializer = Xml.newSerializer();
    FileOutputStream fos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityModificarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Se cambia el nombre del titulo del toolbar
        binding.barraSuperiorRegreso.tvTituloToolbar.setText("Modificación de guía");

        binding.barraSuperiorRegreso.imgvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "No se hicieron cambios en el archivo",
                        Toast.LENGTH_SHORT).show();

                onBackPressed();
            }
        });

        // Guardo el nombre del archivo enviado desde el popupFragmentListarGuias.
        nombreArchivo = getIntent().getExtras().getString("nombre_archivo");

        // Aquí simplemente nos aseguramos que tenga el xml, si lo tiene no entramos.
        // En teoria ya todos los archivos no tienen el .xml porque lo recupero del ListarGuias
        if (!nombreArchivo.contains(".xml")){
            nombreArchivo = getIntent().getExtras().getString("nombre_archivo")+".xml";
        }

        // Obtenemos los datos del XML y los guardamos en su respectivo ArrayList.
        obtenerDatosXML();

        // Pintamos el primer valor de la pregunta.
        binding.etPregunta.setText(preguntas.get(contadorPregunta));
        binding.etRespuesta.setText(respuestas.get(contadorPregunta));

        binding.btnAtrasPregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contadorPregunta > 0){
                    contadorPregunta--;
                    binding.etPregunta.setText("");
                    binding.etRespuesta.setText("");
                    binding.etPregunta.setText(preguntasModificadas.get(contadorPregunta));
                    binding.etRespuesta.setText(respuestasModificadas.get(contadorPregunta));
                    sinModificadas = false;
                } else {
                    // Si el contador
                    Toast.makeText(getApplicationContext(), "Ya no tienes preguntas anteriores"
                            , Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.btnSiguientePregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contadorPregunta++;
                int preguntasTotales = preguntas.size();
                // Validamos que no haya campos vacios en la pregunta o respuesta.
                if (!binding.etPregunta.getText().toString().isEmpty()
                        && !binding.etRespuesta.getText().toString().isEmpty()){
                    // Si siempre se le da siguiente no entrará al if, unicamente entra si se le
                    // da click al botón de atras.
                    if (contadorPregunta <= preguntasModificadas.size()){
                        preguntasModificadas.set((contadorPregunta-1), binding.etPregunta.getText().toString());
                        respuestasModificadas.set((contadorPregunta-1), binding.etRespuesta.getText().toString());
                        sinModificadas = false;

                        if (contadorPregunta == preguntasModificadas.size()){
                            sinModificadas = true;
                        }
                    } else if ((contadorPregunta)<=preguntasTotales){
                        binding.btnGuardarGuiaInhabilitado.setVisibility(View.INVISIBLE);
                        binding.btnGuardarGuia.setVisibility(View.VISIBLE);
                        preguntasModificadas.add((contadorPregunta-1), binding.etPregunta.getText().toString());
                        respuestasModificadas.add((contadorPregunta-1), binding.etRespuesta.getText().toString());

                        if (contadorPregunta != preguntasTotales){
                            binding.etPregunta.setText(preguntas.get(contadorPregunta));
                            binding.etRespuesta.setText(respuestas.get(contadorPregunta));
                        } else {
                            // noHayMasPreguntas = false; esta tendría que ponerla en el boton de atras.
                            noHayMasPreguntas = true;

                            // ¿Quieres agregar más preguntas?
                            new AlertDialog.Builder(Activity_Modificar.this)
                                    .setTitle("¡Atención!")
                                    .setMessage("Se acabaron las preguntas, ¿Quieres agregar mas preguntas?")
                                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            // Cambiaremos el toolbar y cambiaremos de botón siguiente
                                            binding.barraSuperiorRegreso.tvTituloToolbar.setText("Agrega más preguntas a la guía");
                                            binding.btnEliminar.setVisibility(View.INVISIBLE);
                                            binding.etPregunta.setText("");
                                            binding.etRespuesta.setText("");
                                            Toast.makeText(getApplicationContext(), "Ya puedes agregar " +
                                                    "mas preguntas", Toast.LENGTH_LONG).show();

                                            agregarPreguntas = true;
                                            binding.btnAtrasPregunta.setVisibility(View.INVISIBLE);
                                        }
                                    })
                                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {
                                            // binding.btnAtrasPregunta.performClick();
                                            // noHayMasPreguntas = false;
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                        }
                        binding.etPregunta.requestFocus();
                    } else if (noHayMasPreguntas){
                        // Cuando el contador supera las preguntas que ya están modificadas
                        // entra aquí y agrega las nuevas a las modificadas.
                        preguntasModificadas.add((contadorPregunta-1), binding.etPregunta.getText().toString());
                        respuestasModificadas.add((contadorPregunta-1), binding.etRespuesta.getText().toString());
                        binding.etPregunta.setText("");
                        binding.etRespuesta.setText("");
                        binding.etPregunta.requestFocus();
                    }
                } else if(binding.etPregunta.getText().toString().isEmpty()
                        && binding.etRespuesta.getText().toString().isEmpty() &&
                        contadorPregunta<=preguntasTotales){
                    // En dado caso de que haya algun espacio vacio no se tomará en cuenta
                    Toast.makeText(getApplicationContext(), "Borraste la pregunta anterior" +
                            " de tu guía de estudio", Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int preguntasTotales = preguntas.size();

                // ¿Quieres agregar más preguntas?
                new AlertDialog.Builder(Activity_Modificar.this)
                        .setTitle("¡Atención!")
                        .setMessage("¿Quieres eliminar la pregunta?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Cambiaremos el toolbar y cambiaremos de botón siguiente
                                binding.etPregunta.setText("");
                                binding.etRespuesta.setText("");
                                contadorPregunta++;

                                // Validamos que haya mas preguntas, si las hay entra al método sino al else.
                                if ((contadorPregunta+1)<=preguntasTotales){
                                    binding.etPregunta.setText(preguntas.get(contadorPregunta));
                                    binding.etRespuesta.setText(respuestas.get(contadorPregunta));
                                } else {
                                    noHayMasPreguntas = true;
                                    binding.btnEliminar.setVisibility(View.INVISIBLE);

                                    // ¿Quieres agregar más preguntas?
                                    new AlertDialog.Builder(Activity_Modificar.this)
                                            .setTitle("¡Atención!")
                                            .setMessage("Se acabaron las preguntas, ¿Quieres agregar mas preguntas?")
                                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    // Cambiaremos el toolbar y cambiaremos de botón siguiente
                                                    binding.barraSuperiorRegreso.tvTituloToolbar.setText("Agrega más preguntas a la guía");
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
                int preguntasTotales = preguntas.size();

                // Valido que ambos campos esten vacios o llenos sino envío un mensaje.
                if (!binding.etPregunta.getText().toString().isEmpty() &&
                    binding.etRespuesta.getText().toString().isEmpty() ||
                    binding.etPregunta.getText().toString().isEmpty() &&
                    !binding.etRespuesta.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(),
                            "Asegurate de no dejar ningún campo vacio",
                            Toast.LENGTH_SHORT).show();
                } else if (binding.etPregunta.getText().toString().isEmpty()
                        && binding.etRespuesta.getText().toString().isEmpty()){
                    borrarCrearXML();
                } else {
                    binding.btnGuardarGuiaInhabilitado.setVisibility(View.INVISIBLE);
                    binding.btnGuardarGuia.setVisibility(View.VISIBLE);
                    // Si modificadas es falso entonces si hay en el arreglo de preguntasModificadas
                    // Por lo cual entraría al if (darle en el botón atras y guardar ahí)
                    // Si aun no son nuevas preguntas entonces agregarPreguntas es falso.
                        if (!sinModificadas){
                            preguntasModificadas.set((contadorPregunta), binding.etPregunta.getText().toString());
                            respuestasModificadas.set((contadorPregunta), binding.etRespuesta.getText().toString());
                            binding.etPregunta.setText("");
                            binding.etRespuesta.setText("");

                            // Se suman 2 porque la posición +1 ya la guardamos en las dos líneas de
                            // arriba, así que para comenzar en la siguiente pregunta se le suma otro 1.
                            contadorPregunta += 2;
                            for (int i = contadorPregunta; i <= preguntasModificadas.size(); i++) {
                                preguntasModificadas.add((i-1), preguntasModificadas.get(i-1));
                                respuestasModificadas.add((i-1), respuestasModificadas.get(i-1));
                                contadorPregunta += i;
                            }

                            for (int i = contadorPregunta; i <= preguntasTotales; i++) {
                                preguntasModificadas.add((i-1), preguntas.get(i-1));
                                respuestasModificadas.add((i-1), respuestas.get(i-1));
                            }

                            borrarCrearXML();

                        } else if ((contadorPregunta)<=preguntasTotales){
                            int contMasPreg = 0;

                            preguntasModificadas.add(contadorPregunta, binding.etPregunta.getText().toString());
                            respuestasModificadas.add(contadorPregunta, binding.etRespuesta.getText().toString());
                            binding.etPregunta.setText("");
                            binding.etRespuesta.setText("");

                            // Se suman 2 porque la posición +1 ya la guardamos en las dos líneas de
                            // arriba, así que para comenzar en la siguiente pregunta se le suma otro 1.
                            for (int i = contadorPregunta+2; i <= preguntasTotales; i++) {
                                preguntasModificadas.add((i-1), preguntas.get(i-1));
                                respuestasModificadas.add((i-1), respuestas.get(i-1));
                            }
                            borrarCrearXML();
                        } else if (agregarPreguntas){
                            preguntasModificadas.add((contadorPregunta-1), binding.etPregunta.getText().toString());
                            respuestasModificadas.add((contadorPregunta-1), binding.etRespuesta.getText().toString());
                            binding.etPregunta.setText("");
                            binding.etRespuesta.setText("");
                            borrarCrearXML();
                        }
                }
            }
        });
    }

    // Método que se ejecuta cuando el back del telefono es presionado.
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            Toast.makeText(getApplicationContext(),
                    "No se hicieron cambios en el archivo",
                    Toast.LENGTH_SHORT).show();

            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void borrarCrearXML(){
        // Eliminamos el archivo anteriormente creado
        @SuppressLint("SdCardPath") File file = new File("/data/data/com.jonathanev.repasar/files/");
        if (file.exists()){
            new File(file, nombreArchivo).delete();
            Log.d("ArchivoEliminado", "Archivo eliminado");
        } else {
            Log.d("ArchivoEliminado", "Archivo no eliminado");
        }

        //Vamos a crear el archivo que acabamos de eliminar pero con el nuevo cuestionario
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
            for (int j = 0; j < preguntasModificadas.size(); j++) {
                serializer.startTag("", "Interrogante");
                serializer.attribute("", "pregunta", preguntasModificadas.get(j));
                serializer.attribute("", "respuesta", respuestasModificadas.get(j));
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

            // Una vez actualizado regresaremos a la pantalla principal.
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("nombre_archivo", nombreArchivo);
            startActivity(intent);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
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