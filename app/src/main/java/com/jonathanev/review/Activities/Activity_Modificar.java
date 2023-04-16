package com.jonathanev.review.Activities;

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

import com.jonathanev.review.MainActivity;
import com.jonathanev.review.databinding.ActivityModificarBinding;

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
    private int contador= 0;
    private boolean noHayMasPreguntas = false;
    private boolean sinModificadas = true;
    private boolean dialMasPreg = false;

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
        binding.etPregunta.setText(preguntas.get(contador));
        binding.etRespuesta.setText(respuestas.get(contador));

        binding.btnAtrasPregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                            preguntas.set(contador, binding.etPregunta.getText().toString());
                            respuestas.set(contador, binding.etRespuesta.getText().toString());

                            binding.etPregunta.setText(preguntas.get(contador-1));
                            binding.etRespuesta.setText(respuestas.get(contador-1));
                        }
                    } else {
                        // Si el contador es mayor a lo que hay guardado entonces únicamente
                        // escribirá en los textos.
                        binding.etPregunta.setText(preguntas.get(contador-1));
                        binding.etRespuesta.setText(respuestas.get(contador-1));
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
                        preguntas.set(contador, binding.etPregunta.getText().toString());
                        respuestas.set(contador, binding.etRespuesta.getText().toString());

                        // Mientras el contador sea menor escribiremos la siguiente pregunta
                        // en los et.
                        if (contador < longi){
                            binding.etPregunta.setText(preguntas.get(contador+1));
                            binding.etRespuesta.setText(respuestas.get(contador+1));
                        } else if (!dialMasPreg){
                            // ¿Quieres agregar más preguntas?
                            new AlertDialog.Builder(Activity_Modificar.this)
                                    .setTitle("¡Atención!")
                                    .setMessage("Se acabaron las preguntas, ¿Quieres agregar más?")
                                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            // Cambiaremos el texto del toolbar.
                                            binding.barraSuperiorRegreso.tvTituloToolbar.setText("Agrega más preguntas a la guía");
                                            binding.etPregunta.setText("");
                                            binding.etRespuesta.setText("");
                                            binding.etPregunta.requestFocus();
                                            contador++;

                                            dialMasPreg = true;

                                            Toast.makeText(getApplicationContext(), "Ya puedes agregar " +
                                                    "mas preguntas", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                            contador--;
                        } else {
                            // Si el contador es igual entonces solo escribiremos los campos vacios.
                            binding.etPregunta.setText("");
                            binding.etRespuesta.setText("");
                            binding.etPregunta.requestFocus();                        }
                    } else {
                        // Si el contador es mayor entonces agregaremos la pregunta actual a los
                        // arreglos.
                        preguntas.add(contador, binding.etPregunta.getText().toString());
                        respuestas.add(contador, binding.etRespuesta.getText().toString());

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
            public void onClick(View view) {
                new AlertDialog.Builder(Activity_Modificar.this)
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
                        borrarCrearXML(nombreArchivo);
                    }
                } else if (contador > longi){
                    // Si el contador es mayor a lo guardado entonces agregamos la pregunta
                    // anteriormente ya validamos campos vacios.
                    preguntas.add(contador, binding.etPregunta.getText().toString());
                    respuestas.add(contador, binding.etRespuesta.getText().toString());
                    borrarCrearXML(nombreArchivo);
                } else {
                    // Si el contador no es mayor a lo guardado entonces modificamos lo actual en
                    // el arreglo, además anteriormente ya validamos campos vacios.
                    preguntas.set(contador, binding.etPregunta.getText().toString());
                    respuestas.set(contador, binding.etRespuesta.getText().toString());
                    borrarCrearXML(nombreArchivo);
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

    private void borrarCrearXML(String nombreArchivo){
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