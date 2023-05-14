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

import com.jonathanev.review.Clases.ColoresPregunta;
import com.jonathanev.review.Fragments.Fragment_DialogColoresMod_popup;
import com.jonathanev.review.MainActivity;
import com.jonathanev.review.R;
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
    private int colorActual = 0;
    private ArrayList<String> preguntas = new ArrayList<>();
    private ArrayList<String> respuestas = new ArrayList<>();
    private ArrayList<ColoresPregunta> preguntasColor = new ArrayList<>();
    private ArrayList<ColoresPregunta> respuestasColor = new ArrayList<>();
    SpannableStringBuilder builder;
    private int contadorPregunta= 0;
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

        colorActual = Color.BLACK;
        colorActual(colorActual);

        binding.imgvColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
                Fragment_DialogColoresMod_popup dialogo = new Fragment_DialogColoresMod_popup();
                dialogo.show(getSupportFragmentManager(), "FragmentColor");
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

        // Pintamos el texto en la pregunta actual
        pintarTexto(contadorPregunta);

        binding.btnAtrasPregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contadorPregunta > 0){
                    // Se le quita 1 para hacer referencia al arreglo
                    // tamaño 3-1 = 2 [0,1,2].
                    int longi = preguntas.size()-1;

                    // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                    // de lo que esté en la posición 0.
                    if (contadorPregunta <= longi){
                        // Validamos campos vacios en la pregunta y respuesta.
                        if (binding.etPregunta.getText().toString().isEmpty()
                                || binding.etRespuesta.getText().toString().isEmpty()){
                            Toast.makeText(getApplicationContext(),
                                    "Asegurate de no dejar ningun campo vacio",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Si los campos están bien se sobre escribe.
                            Editable editable = Editable.Factory.getInstance().newEditable(binding.etPregunta.getText());
                            ForegroundColorSpan[] colorSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable);

                            preguntas.set(contadorPregunta, editable.toString());

                            editable = Editable.Factory.getInstance().newEditable(binding.etRespuesta.getText());
                            colorSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable);

                            respuestas.set(contadorPregunta, editable.toString());

                            // Pintamos el texto en la pregunta actual
                            pintarTexto(contadorPregunta-1);
                        }
                    } else {
                        // Si el contadorPregunta es mayor a lo que hay guardado entonces
                        // Pintamos el texto
                        pintarTexto(contadorPregunta-1);
                    }
                    contadorPregunta--;
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

                    // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                    // de lo que esté en la posición 0.
                    if (contadorPregunta <= longi){
                        Editable editable = Editable.Factory.getInstance().newEditable(binding.etPregunta.getText());
                        ForegroundColorSpan[] colorSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable);

                        preguntas.set(contadorPregunta, editable.toString());

                        editable = Editable.Factory.getInstance().newEditable(binding.etRespuesta.getText());
                        colorSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable);

                        respuestas.set(contadorPregunta, editable.toString());

                        // Mientras el contadorPregunta sea menor escribiremos la siguiente pregunta
                        // en los et y se borran las etiquetas de colores.
                        if (contadorPregunta < longi){
                            // Pintamos el texto en la pregunta actual
                            pintarTexto(contadorPregunta+1);
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
                                            contadorPregunta++;

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
                            contadorPregunta--;
                        } else {
                            // Si el contadorPregunta es igual entonces solo escribiremos los campos vacios.
                            binding.etPregunta.setText("");
                            binding.etRespuesta.setText("");
                            binding.etPregunta.requestFocus();                        }
                    } else {
                        // Si el contadorPregunta es mayor entonces agregaremos la pregunta actual a los
                        // arreglos.«»
                        Editable editable = Editable.Factory.getInstance().newEditable(binding.etPregunta.getText());
                        ForegroundColorSpan[] colorSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable);

                        preguntas.add(contadorPregunta, editable.toString());

                        editable = Editable.Factory.getInstance().newEditable(binding.etRespuesta.getText());
                        colorSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable);

                        respuestas.add(contadorPregunta, editable.toString());

                        binding.etPregunta.setText("");
                        binding.etRespuesta.setText("");
                        binding.etPregunta.requestFocus();
                    }
                    contadorPregunta++;
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

                                // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                                // de lo que esté en la posición 0.
                                if (contadorPregunta <= longi){
                                    preguntas.remove(contadorPregunta);
                                    respuestas.remove(contadorPregunta);

                                    // Mientras el contadorPregunta sea menor escribiremos la siguiente pregunta
                                    // en los et.
                                    if (contadorPregunta < longi){
                                        // Pintamos el texto en la pregunta actual
                                        pintarTexto(contadorPregunta);
                                    } else {
                                        // Si el contadorPregunta es igual entonces solo escribiremos los campos vacios.
                                        binding.etPregunta.setText("");
                                        binding.etRespuesta.setText("");
                                        binding.etPregunta.requestFocus();
                                    }
                                } else {
                                    // Si el contadorPregunta es mayor entonces únicamente limpiamos los campos.
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
                    if (contadorPregunta == 0){
                        Toast.makeText(getApplicationContext(),
                                "¡No puedes guardar una guía sin datos!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Si queremos guardar con campos vacios y hay preguntas anteriores
                        // guardadas entra aquí.
                        borrarCrearXML(nombreArchivo);
                    }
                } else if (contadorPregunta > longi){
                    // Si el contadorPregunta es mayor a lo guardado entonces agregamos la pregunta
                    // anteriormente ya validamos campos vacios.
                    Editable editable = Editable.Factory.getInstance().newEditable(binding.etPregunta.getText());
                    ForegroundColorSpan[] colorSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable);

                    preguntas.add(contadorPregunta, editable.toString());

                    editable = Editable.Factory.getInstance().newEditable(binding.etRespuesta.getText());
                    colorSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable);

                    respuestas.add(contadorPregunta, editable.toString());
                    borrarCrearXML(nombreArchivo);
                } else {
                    // Si el contadorPregunta no es mayor a lo guardado entonces modificamos lo actual en
                    // el arreglo, además anteriormente ya validamos campos vacios.
                    Editable editable = Editable.Factory.getInstance().newEditable(binding.etPregunta.getText());
                    ForegroundColorSpan[] colorSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable);

                    preguntas.set(contadorPregunta, editable.toString());

                    editable = Editable.Factory.getInstance().newEditable(binding.etRespuesta.getText());
                    colorSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable);

                    respuestas.set(contadorPregunta, editable.toString());
                    borrarCrearXML(nombreArchivo);
                }
            }
        });

        binding.etPregunta.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                // Inflar el menú personalizado de color.
                actionMode.getMenuInflater().inflate(R.menu.menu_color, menu);

                // Inflar el menú personalizado sin color.
                actionMode.getMenuInflater().inflate(R.menu.munu_sin_color, menu);
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
                        // Acción para traducir la palabra seleccionada
                        int start = binding.etPregunta.getSelectionStart();
                        int end = binding.etPregunta.getSelectionEnd();
                        Editable text = binding.etPregunta.getText();

                        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
                        spannableStringBuilder.setSpan(new ForegroundColorSpan(colorActual), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        binding.etPregunta.setText(spannableStringBuilder);
                        binding.etPregunta.setSelection(end);
                        return true;
                    case R.id.sin_color:
                        // Poner un color al rango marcado
                        start = binding.etPregunta.getSelectionStart();
                        end = binding.etPregunta.getSelectionEnd();
                        text = binding.etPregunta.getText();

                        spannableStringBuilder = new SpannableStringBuilder(text);

                        // Obtener los spans de color que se superponen con la selección
                        ForegroundColorSpan[] colorSpans = spannableStringBuilder.getSpans(start, end, ForegroundColorSpan.class);

                        // Eliminar cada span de color que se superponga con la selección
                        for (ForegroundColorSpan colorSpan : colorSpans) {
                            spannableStringBuilder.removeSpan(colorSpan);
                        }

                        binding.etPregunta.setText(spannableStringBuilder);

                        binding.etPregunta.setSelection(end);
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
                // Inflar el menú personalizado de color.
                actionMode.getMenuInflater().inflate(R.menu.menu_color, menu);

                // Inflar el menú personalizado sin color.
                actionMode.getMenuInflater().inflate(R.menu.munu_sin_color, menu);
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
                        // Acción para traducir la palabra seleccionada
                        int start = binding.etRespuesta.getSelectionStart();
                        int end = binding.etRespuesta.getSelectionEnd();
                        Editable text = binding.etRespuesta.getText();

                        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
                        spannableStringBuilder.setSpan(new ForegroundColorSpan(colorActual), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        binding.etRespuesta.setText(spannableStringBuilder);
                        binding.etPregunta.setSelection(end);
                        return true;
                    case R.id.sin_color:
                        // Poner un color al rango marcado
                        start = binding.etPregunta.getSelectionStart();
                        end = binding.etPregunta.getSelectionEnd();
                        text = binding.etPregunta.getText();

                        spannableStringBuilder = new SpannableStringBuilder(text);

                        // Obtener los spans de color que se superponen con la selección
                        ForegroundColorSpan[] colorSpans = spannableStringBuilder.getSpans(start, end, ForegroundColorSpan.class);

                        // Eliminar cada span de color que se superponga con la selección
                        for (ForegroundColorSpan colorSpan : colorSpans) {
                            spannableStringBuilder.removeSpan(colorSpan);
                        }

                        binding.etPregunta.setText(spannableStringBuilder);

                        binding.etPregunta.setSelection(end);
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });

        binding.btnQuitarColores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.etPregunta.setText(binding.etPregunta.getText().toString());
                binding.etRespuesta.setText(binding.etRespuesta.getText().toString());
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

    public void colocarEtiquetas(ForegroundColorSpan[] colorSpans, Editable editable ){
        for (ForegroundColorSpan colorSpan : colorSpans) {
            int start = editable.getSpanStart(colorSpan);
            int end = editable.getSpanEnd(colorSpan);
            int color = colorSpan.getForegroundColor();

            // Agregar la etiqueta de inicio al texto
            String etiqIni = "«"+color+"»";
            String etiqFin = "«/"+color+"»";
            editable.replace(start, start, etiqIni);
            // Actualizar la posición de inicio del span
            // colorSpan = new ForegroundColorSpan(colorSpan.getForegroundColor());
            // editable.setSpan(colorSpan, start + etiqIni.length(), end + etiqIni.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Agregar la etiqueta de cierre al texto
            editable.replace(end + etiqIni.length(), end + etiqIni.length(), etiqFin);
            // Actualizar la posición de finalización del span
            // editable.setSpan(colorSpan, start + etiqIni.length(), end + etiqIni.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void pintarTexto(int contadorPregunta) {
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

        binding.etRespuesta.setText(builder);
        preguntasColor.clear();
        respuestasColor.clear();
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

    public void colorActual(int colorActual){
        @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = getResources().getDrawable(R.drawable.boton_redondo);
        drawable.setColorFilter(colorActual, PorterDuff.Mode.SRC_ATOP);

        binding.btnColorActual.setBackground(drawable);

        // Recibimos el nombre del archivo del popupFragment Nueva Guia.
        this.colorActual = colorActual;
    }
}