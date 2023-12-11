package com.jonathanev.review.Activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Xml
import android.view.ActionMode
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.jonathanev.review.Clases.ColoresPregunta
import com.jonathanev.review.Fragments.Fragment_DialogColoresMod_popup
import com.jonathanev.review.R
import com.jonathanev.review.databinding.ActivityModificarBinding
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class Activity_Modificar constructor() : AppCompatActivity() {
    private var binding: ActivityModificarBinding? = null
    private var nombreArchivo: String? = null
    private var colorActual: Int = 0
    private val preguntas: ArrayList<String> = ArrayList()
    private val respuestas: ArrayList<String> = ArrayList()
    private val preguntasColor: ArrayList<ColoresPregunta> = ArrayList()
    private val respuestasColor: ArrayList<ColoresPregunta> = ArrayList()
    var builder: SpannableStringBuilder? = null
    private var contadorPregunta: Int = 0
    private var dialMasPreg: Boolean = false

    // Creamos la serialización y la clase para crear archivos de manera global.
    private var serializer: XmlSerializer = Xml.newSerializer()
    private var fos: FileOutputStream? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModificarBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Guardo el nombre del archivo enviado desde el popupFragmentListarGuias.
        nombreArchivo = intent.extras!!.getString("nombre_archivo")

        // Se cambia el nombre del titulo del toolbar
        binding!!.barraSuperiorRegreso.tvTituloToolbar.text = "Modificando: $nombreArchivo"
        binding!!.barraSuperiorRegreso.imgvBack.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                Toast.makeText(
                    applicationContext,
                    "No se hicieron cambios en el archivo",
                    Toast.LENGTH_SHORT
                ).show()
                onBackPressed()
            }
        })
        colorActual = Color.BLACK
        colorActual(colorActual)
        binding!!.imgvColor.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
                val dialogo: Fragment_DialogColoresMod_popup = Fragment_DialogColoresMod_popup()
                //=====================================================================================================================
                dialogo.show(supportFragmentManager, "FragmentColor")
            }
        })

        // Aquí simplemente nos aseguramos que tenga el xml, si lo tiene no entramos.
        // En teoria ya todos los archivos no tienen el .xml porque lo recupero del ListarGuias
        if (!nombreArchivo!!.contains(".xml")) {
            nombreArchivo = intent.extras!!.getString("nombre_archivo") + ".xml"
        }

        // Obtenemos los datos del XML y los guardamos en su respectivo ArrayList.
        obtenerDatosXML()

        // Pintamos el texto en la pregunta actual
        pintarTexto(contadorPregunta)
        binding!!.btnAtrasPregunta.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                // El contadorPregunta tiene que ser mayor a 0 sino significa que no hay preguntas anteriores.
                if (contadorPregunta > 0) {
                    // Se le quita 1 para hacer referencia al arreglo
                    // tamaño 3-1 = 2 [0,1,2].
                    val longi: Int = preguntas.size - 1

                    // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                    // de lo que esté en la posición 0.
                    if (contadorPregunta <= longi) {
                        // Validamos campos vacios en la pregunta y respuesta.
                        if ((binding!!.etPregunta.text.toString().isEmpty()
                                    || binding!!.etRespuesta.text.toString().isEmpty())
                        ) {
                            Toast.makeText(
                                applicationContext,
                                "Asegurate de no dejar ningun campo vacio",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Se resta uno al final y así se queda neutral.
                            contadorPregunta++
                        } else {
                            // Si los campos están bien se sobre escribe.
                            var editable: Editable = Editable.Factory.getInstance().newEditable(
                                binding!!.etPregunta.text
                            )
                            var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                                0,
                                editable.length,
                                ForegroundColorSpan::class.java
                            )

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable)
                            preguntas[contadorPregunta] = editable.toString()
                            editable = Editable.Factory.getInstance()
                                .newEditable(binding!!.etRespuesta.text)
                            colorSpans = editable.getSpans(
                                0,
                                editable.length,
                                ForegroundColorSpan::class.java
                            )

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable)
                            respuestas[contadorPregunta] = editable.toString()

                            // Pintamos el texto en la pregunta actual
                            pintarTexto(contadorPregunta - 1)
                        }
                    } else {
                        if ((binding!!.etPregunta.text.toString().isEmpty()
                                    || binding!!.etRespuesta.text.toString().isEmpty())
                        ) {
                            Toast.makeText(
                                applicationContext,
                                "Asegurate de no dejar ningun campo vacio",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Se resta uno al final y así se queda neutral.
                            contadorPregunta++
                        } else {
                            // Si el contadorPregunta es mayor entonces agregaremos la pregunta actual a los
                            // arreglos.«»
                            var editable: Editable = Editable.Factory.getInstance().newEditable(
                                binding!!.etPregunta.text
                            )
                            var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                                0,
                                editable.length,
                                ForegroundColorSpan::class.java
                            )

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable)
                            preguntas.add(contadorPregunta, editable.toString())
                            editable = Editable.Factory.getInstance()
                                .newEditable(binding!!.etRespuesta.text)
                            colorSpans = editable.getSpans(
                                0,
                                editable.length,
                                ForegroundColorSpan::class.java
                            )

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable)
                            respuestas.add(contadorPregunta, editable.toString())

                            // Si el contadorPregunta es mayor a lo que hay guardado entonces
                            // pintamos el texto anterior.
                            pintarTexto(contadorPregunta - 1)
                        }
                    }
                    contadorPregunta--
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Ya no tienes preguntas anteriores",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
        binding!!.btnSiguientePregunta.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                // Validamos campos vacios en la pregunta o respuesta.
                if ((binding!!.etPregunta.text.toString().isEmpty()
                            || binding!!.etRespuesta.text.toString().isEmpty())
                ) {
                    Toast.makeText(
                        getApplicationContext(),
                        "Asegurate de no dejar ningun campo vacio",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Se le quita 1 para hacer referencia al arreglo
                    // tamaño 3-1 = 2 [0,1,2].
                    val longi: Int = preguntas.size - 1

                    // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                    // de lo que esté en la posición 0.
                    if (contadorPregunta <= longi) {
                        var editable: Editable = Editable.Factory.getInstance().newEditable(
                            binding!!.etPregunta.text
                        )
                        var colorSpans: Array<ForegroundColorSpan> =
                            editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)
                        preguntas[contadorPregunta] = editable.toString()
                        editable = Editable.Factory.getInstance()
                            .newEditable(binding!!.etRespuesta.text)
                        colorSpans =
                            editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)
                        respuestas[contadorPregunta] = editable.toString()

                        // Mientras el contadorPregunta sea menor escribiremos la siguiente pregunta
                        // en los et y se borran las etiquetas de colores.
                        if (contadorPregunta < longi) {
                            // Pintamos el texto en la pregunta actual
                            pintarTexto(contadorPregunta + 1)
                        } else if (!dialMasPreg) {
                            // ¿Quieres agregar más preguntas?
                            AlertDialog.Builder(this@Activity_Modificar)
                                .setTitle("¡Atención!")
                                .setMessage("Se acabaron las preguntas, ¿Quieres agregar más?")
                                .setPositiveButton("Si", object : DialogInterface.OnClickListener {
                                    public override fun onClick(
                                        dialogInterface: DialogInterface,
                                        i: Int
                                    ) {
                                        // Cambiaremos el texto del toolbar.
                                        binding!!.barraSuperiorRegreso.tvTituloToolbar.text = "Agrega más preguntas a la guía"
                                        binding!!.etPregunta.setText("")
                                        binding!!.etRespuesta.setText("")
                                        binding!!.etPregunta.requestFocus()
                                        contadorPregunta++
                                        dialMasPreg = true
                                        Toast.makeText(
                                            applicationContext, "Ya puedes agregar " +
                                                    "mas preguntas", Toast.LENGTH_LONG
                                        ).show()
                                    }
                                })
                                .setNegativeButton(
                                    "Cancelar",
                                    object : DialogInterface.OnClickListener {
                                        public override fun onClick(
                                            dialog: DialogInterface,
                                            i: Int
                                        ) {
                                            dialog.dismiss()
                                        }
                                    }).create().show()
                            contadorPregunta--
                        } else {
                            // Si el contadorPregunta es igual entonces solo escribiremos los campos vacios.
                            binding!!.etPregunta.setText("")
                            binding!!.etRespuesta.setText("")
                            binding!!.etPregunta.requestFocus()
                        }
                    } else {
                        // Si el contadorPregunta es mayor entonces agregaremos la pregunta actual a los
                        // arreglos.«»
                        var editable: Editable = Editable.Factory.getInstance()
                            .newEditable(binding!!.etPregunta.text)
                        var colorSpans: Array<ForegroundColorSpan> =
                            editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)
                        preguntas.add(contadorPregunta, editable.toString())
                        editable = Editable.Factory.getInstance()
                            .newEditable(binding!!.etRespuesta.text)
                        colorSpans =
                            editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)
                        respuestas.add(contadorPregunta, editable.toString())
                        binding!!.etPregunta.setText("")
                        binding!!.etRespuesta.setText("")
                        binding!!.etPregunta.requestFocus()
                    }
                    contadorPregunta++
                }
            }
        })
        binding!!.btnEliminar.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                AlertDialog.Builder(this@Activity_Modificar)
                    .setTitle("¡Atención!")
                    .setMessage("¿Quieres eliminar la pregunta?")
                    .setPositiveButton("Si", object : DialogInterface.OnClickListener {
                        public override fun onClick(dialogInterface: DialogInterface, i: Int) {
                            // Se le quita 1 para hacer referencia al arreglo
                            // tamaño 3-1 = 2 [0,1,2].
                            val longi: Int = preguntas.size - 1

                            // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                            // de lo que esté en la posición 0.
                            if (contadorPregunta <= longi) {
                                preguntas.removeAt(contadorPregunta)
                                respuestas.removeAt(contadorPregunta)

                                // Mientras el contadorPregunta sea menor escribiremos la siguiente pregunta
                                // en los et.
                                if (contadorPregunta < longi) {
                                    // Pintamos el texto en la pregunta actual
                                    pintarTexto(contadorPregunta)
                                } else {
                                    // Si el contadorPregunta es igual entonces solo escribiremos los campos vacios.
                                    binding!!.etPregunta.setText("")
                                    binding!!.etRespuesta.setText("")
                                    binding!!.etPregunta.requestFocus()
                                }
                            } else {
                                // Si el contadorPregunta es mayor entonces únicamente limpiamos los campos.
                                binding!!.etPregunta.setText("")
                                binding!!.etRespuesta.setText("")
                                binding!!.etPregunta.requestFocus()
                            }
                        }
                    })
                    .setNegativeButton("Cancelar", object : DialogInterface.OnClickListener {
                        public override fun onClick(dialog: DialogInterface, i: Int) {
                            dialog.dismiss()
                        }
                    }).create().show()
            }
        })
        binding!!.btnGuardarGuia.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                // Se le quita 1 para hacer referencia al arreglo
                // tamaño 3-1 = 2 [0,1,2].
                val longi: Int = preguntas.size - 1

                // Si alguno de los dos campos está vacio entra aquí.
                if (!binding!!.etPregunta.text.toString().isEmpty() &&
                    binding!!.etRespuesta.text.toString().isEmpty() ||
                    binding!!.etPregunta.text.toString().isEmpty() &&
                    !binding!!.etRespuesta.text.toString().isEmpty()
                ) {
                    Toast.makeText(
                        applicationContext,
                        "Asegurate de no dejar ningún campo vacio",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if ((binding!!.etPregunta.text.toString().isEmpty()
                            && binding!!.etRespuesta.text.toString().isEmpty())
                ) {
                    // Si los dos campos están vacios entra aquí.

                    // Si queremos guardar con campos vacios y no hay preguntas anteriores guardadas
                    // entra aquí.
                    if (contadorPregunta == 0) {
                        Toast.makeText(
                            applicationContext,
                            "¡No puedes guardar una guía sin datos!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Si queremos guardar con campos vacios y hay preguntas anteriores
                        // guardadas entra aquí.
                        borrarCrearXML(nombreArchivo)
                    }
                } else if (contadorPregunta <= longi) {
                    // Si el contadorPregunta no es mayor a lo guardado entonces modificamos lo actual en
                    // el arreglo, además anteriormente ya validamos campos vacios.
                    var editable: Editable =
                        Editable.Factory.getInstance().newEditable(binding!!.etPregunta.text)
                    var colorSpans: Array<ForegroundColorSpan> =
                        editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)
                    preguntas[contadorPregunta] = editable.toString()
                    editable =
                        Editable.Factory.getInstance().newEditable(binding!!.etRespuesta.text)
                    colorSpans =
                        editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)
                    respuestas[contadorPregunta] = editable.toString()
                    borrarCrearXML(nombreArchivo)
                } else {
                    // Si el contadorPregunta es igual a lo guardado entonces agregamos la pregunta,
                    // anteriormente ya validamos campos vacios.
                    var editable: Editable =
                        Editable.Factory.getInstance().newEditable(binding!!.etPregunta.text)
                    var colorSpans: Array<ForegroundColorSpan> =
                        editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)
                    preguntas.add(contadorPregunta, editable.toString())
                    editable =
                        Editable.Factory.getInstance().newEditable(binding!!.etRespuesta.text)
                    colorSpans =
                        editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)
                    respuestas.add(contadorPregunta, editable.toString())
                    borrarCrearXML(nombreArchivo)
                }
            }
        })
        binding!!.etPregunta.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                // Inflar el menú personalizado de color.
                actionMode.menuInflater.inflate(R.menu.menu_color, menu)

                // Inflar el menú personalizado sin color.
                // actionMode.getMenuInflater().inflate(R.menu.munu_sin_color, menu);
                return true
            }

            public override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                return false
            }

            public override fun onActionItemClicked(
                actionMode: ActionMode,
                menuItem: MenuItem
            ): Boolean {
                val start: Int
                val end: Int
                val text: Editable?
                val spannableStringBuilder: SpannableStringBuilder
                when (menuItem.itemId) {
                    R.id.color -> {
                        // Acción para traducir la palabra seleccionada
                        start = binding!!.etPregunta.selectionStart
                        end = binding!!.etPregunta.selectionEnd
                        text = binding!!.etPregunta.text
                        spannableStringBuilder = SpannableStringBuilder(text)

                        // Obtén los spans aplicados
                        val spans: Array<ForegroundColorSpan> = spannableStringBuilder.getSpans(
                            0,
                            spannableStringBuilder.length,
                            ForegroundColorSpan::class.java
                        )

                        // Eliminar spans existentes que se superpongan con el nuevo rango
                        for (span: ForegroundColorSpan? in spans) {
                            val spanInicio: Int = spannableStringBuilder.getSpanStart(span)
                            val spanFin: Int = spannableStringBuilder.getSpanEnd(span)
                            if ((spanInicio < end && spanFin > start) || (spanInicio >= start && spanFin <= end)) {
                                spannableStringBuilder.removeSpan(span)
                                Toast.makeText(
                                    getApplicationContext(),
                                    "Una letra, una tinta; palabras sin colores.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        spannableStringBuilder.setSpan(
                            ForegroundColorSpan(colorActual),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        binding!!.etPregunta.text = spannableStringBuilder
                        binding!!.etPregunta.setSelection(end)
                        return true
                    }

                    else -> return false
                }
            }

            public override fun onDestroyActionMode(actionMode: ActionMode) {}
        })
        binding!!.etRespuesta.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                // Inflar el menú personalizado de color.
                actionMode.menuInflater.inflate(R.menu.menu_color, menu)

                // Inflar el menú personalizado sin color.
                // actionMode.getMenuInflater().inflate(R.menu.munu_sin_color, menu);
                return true
            }

            public override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                return false
            }

            public override fun onActionItemClicked(
                actionMode: ActionMode,
                menuItem: MenuItem
            ): Boolean {
                val start: Int
                val end: Int
                val text: Editable?
                val spannableStringBuilder: SpannableStringBuilder
                when (menuItem.itemId) {
                    R.id.color -> {
                        // Acción para traducir la palabra seleccionada
                        start = binding!!.etRespuesta.selectionStart
                        end = binding!!.etRespuesta.selectionEnd
                        text = binding!!.etRespuesta.text
                        spannableStringBuilder = SpannableStringBuilder(text)

                        // Obtén los spans aplicados
                        val spans: Array<ForegroundColorSpan> = spannableStringBuilder.getSpans(
                            0,
                            spannableStringBuilder.length,
                            ForegroundColorSpan::class.java
                        )

                        // Eliminar spans existentes que se superpongan con el nuevo rango
                        for (span: ForegroundColorSpan? in spans) {
                            val spanInicio: Int = spannableStringBuilder.getSpanStart(span)
                            val spanFin: Int = spannableStringBuilder.getSpanEnd(span)
                            if ((spanInicio < end && spanFin > start) || (spanInicio >= start && spanFin <= end)) {
                                spannableStringBuilder.removeSpan(span)
                                Toast.makeText(
                                    applicationContext,
                                    "Una letra, una tinta; palabras sin colores.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        spannableStringBuilder.setSpan(
                            ForegroundColorSpan(colorActual),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        binding!!.etRespuesta.text = spannableStringBuilder
                        binding!!.etRespuesta.setSelection(end)
                        return true
                    }

                    else -> return false
                }
            }

            public override fun onDestroyActionMode(actionMode: ActionMode) {}
        })
        binding!!.btnQuitarColores.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                binding!!.etPregunta.setText(binding!!.etPregunta.text.toString())
                binding!!.etRespuesta.setText(binding!!.etRespuesta.text.toString())
            }
        })
    }

    // Método que se ejecuta cuando el back del telefono es presionado.
    public override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            Toast.makeText(
                applicationContext,
                "No se hicieron cambios en el archivo",
                Toast.LENGTH_SHORT
            ).show()
            onBackPressed()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun borrarCrearXML(nombreArchivo: String?) {
        // Eliminamos el archivo anteriormente creado
        @SuppressLint("SdCardPath") val file: File =
            File("/data/data/com.jonathanev.repasar/files/")
        if (file.exists()) {
            File(file, nombreArchivo).delete()
            Log.d("ArchivoEliminado", "Archivo eliminado")
        } else {
            Log.d("ArchivoEliminado", "Archivo no eliminado")
        }

        //Vamos a crear el archivo que acabamos de eliminar pero con el nuevo cuestionario
        try {
            fos = openFileOutput(nombreArchivo, MODE_PRIVATE)
            serializer.setOutput(fos, "UTF-8")
            serializer.startDocument(null, java.lang.Boolean.valueOf(true))
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            serializer.startTag("", "GuiaEstudio")
            serializer.attribute("", "version", "1.0")
            serializer.startTag("", "Cuestionario")
            serializer.attribute("", "nombreGuia", nombreArchivo)

            // Creo la etiqueta interrogante con su respectiva pregunta
            for (i in preguntas.indices) {
                serializer.startTag("", "Interrogante")
                serializer.attribute("", "pregunta", preguntas.get(i))
                serializer.attribute("", "respuesta", respuestas.get(i))
                serializer.endTag("", "Interrogante")
            }

            // Si los campos estan vacios simplemente cierro las etiquetas y directamente
            // guardo el documento en el teléfono.
            serializer.endTag("", "Cuestionario")
            serializer.endTag("", "GuiaEstudio")
            serializer.endDocument()
            serializer.flush()
            fos?.close()
            Toast.makeText(
                applicationContext, "Guia de estudio modificada exitosamente",
                Toast.LENGTH_SHORT
            ).show()
            val intent: Intent = Intent(applicationContext, Activity_RepasarGuia::class.java)
            intent.putExtra("nombre_archivo", nombreArchivo)
            startActivity(intent)
            finish()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun colocarEtiquetas(colorSpans: Array<ForegroundColorSpan>, editable: Editable) {
        for (colorSpan: ForegroundColorSpan in colorSpans) {
            val start: Int = editable.getSpanStart(colorSpan)
            val end: Int = editable.getSpanEnd(colorSpan)
            val color: Int = colorSpan.foregroundColor

            // Agregar la etiqueta de inicio al texto
            val etiqIni: String = "«$color»"
            val etiqFin: String = "«/$color»"
            editable.replace(start, start, etiqIni)
            // Actualizar la posición de inicio del span
            // colorSpan = new ForegroundColorSpan(colorSpan.getForegroundColor());
            // editable.setSpan(colorSpan, start + etiqIni.length(), end + etiqIni.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Agregar la etiqueta de cierre al texto
            editable.replace(end + etiqIni.length, end + etiqIni.length, etiqFin)
            // Actualizar la posición de finalización del span
            // editable.setSpan(colorSpan, start + etiqIni.length(), end + etiqIni.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private fun pintarTexto(contadorPregunta: Int) {
        var contColorPreg: Int = 0
        var contColorResp: Int = 0
        var inicio: Int = 0
        var fin: Int = 0
        var coloresPregunta: ColoresPregunta? = null
        var texto: String = preguntas[contadorPregunta]
        while (texto.contains("«")) {
            inicio = texto.indexOf("«") + 1
            fin = texto.indexOf("»")
            val color: String = texto.substring(inicio, fin)
            val longColor: Int = color.length
            val colEntero: Int = color.toInt()
            inicio = fin + 1
            fin = texto.indexOf("«", inicio)
            coloresPregunta =
                ColoresPregunta((inicio - longColor - 2), (fin - longColor - 2), colEntero)
            preguntasColor.add(contColorPreg, coloresPregunta)
            // Eliminar la primera etiqueta y su contenido
            texto = texto.replaceFirst("«.*?»".toRegex(), "")

            // Eliminar la segunda etiqueta y su contenido
            texto = texto.replaceFirst("«.*?»".toRegex(), "")
            contColorPreg++
        }
        builder = SpannableStringBuilder(texto)
        for (coloresPreguntas: ColoresPregunta in preguntasColor) {
            val colorSpan: ForegroundColorSpan = ForegroundColorSpan(coloresPreguntas.color)
            builder!!.setSpan(
                colorSpan,
                coloresPreguntas.inicioColor,
                coloresPreguntas.finColor,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding!!.etPregunta.text = builder
        texto = respuestas[contadorPregunta]
        while (texto.contains("«")) {
            inicio = texto.indexOf("«") + 1
            fin = texto.indexOf("»")
            val color: String = texto.substring(inicio, fin)
            val longColor: Int = color.length
            val colEntero: Int = color.toInt()
            inicio = fin + 1
            fin = texto.indexOf("«", inicio)
            coloresPregunta =
                ColoresPregunta((inicio - longColor - 2), (fin - longColor - 2), colEntero)
            respuestasColor.add(contColorResp, coloresPregunta)
            // Eliminar la primera etiqueta y su contenido
            texto = texto.replaceFirst("«.*?»".toRegex(), "")

            // Eliminar la segunda etiqueta y su contenido
            texto = texto.replaceFirst("«.*?»".toRegex(), "")
            contColorResp++
        }
        builder = SpannableStringBuilder(texto)
        for (coloresPreguntas: ColoresPregunta in respuestasColor) {
            val colorSpan: ForegroundColorSpan = ForegroundColorSpan(coloresPreguntas.color)
            builder!!.setSpan(
                colorSpan,
                coloresPreguntas.inicioColor,
                coloresPreguntas.finColor,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding!!.etRespuesta.text = builder
        preguntasColor.clear()
        respuestasColor.clear()
    }

    private fun obtenerDatosXML() {
        var doc: Document? = null
        val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder
        try {
            db = dbf.newDocumentBuilder()
            val fis: FileInputStream = openFileInput(nombreArchivo)
            doc = db.parse(fis)

            // Buscamos los Nodos Interrogante y accedemos a lo que se encuentre dentro.
            val cuestionario: NodeList = doc.getElementsByTagName("Interrogante")
            for (i in 0 until cuestionario.getLength()) {
                // Obtienes el nodo actual y lo guardamos en info.
                // Este no lo utilizamos ya que arriba ya accedimos al ultimo Nodo
                // Node info = cuestionario.item(i);

                // Accedes a los elmentos de dicho nodo
                val e: Element = cuestionario.item(i) as Element

                // Guardo cada uno de los valores en su respectivo arreglo.
                preguntas.add(e.getAttribute("pregunta"))
                respuestas.add(e.getAttribute("respuesta"))
            }
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun colorActual(colorActual: Int) {
        @SuppressLint("UseCompatLoadingForDrawables") val drawable: Drawable =
            resources.getDrawable(R.drawable.boton_redondo)
        drawable.setColorFilter(colorActual, PorterDuff.Mode.SRC_ATOP)
        binding!!.btnColorActual.background = drawable

        // Recibimos el nombre del archivo del popupFragment Nueva Guia.
        this.colorActual = colorActual
    }
}