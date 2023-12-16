package com.jonathanev.review.Activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Xml
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jonathanev.review.Clases.ColoresPregunta
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

    @SuppressLint("ClickableViewAccessibility")
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
        //colorActual(colorActual)
        /*binding!!.imgvColor.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
                val dialogo: Fragment_DialogColoresMod_popup = Fragment_DialogColoresMod_popup()
                //=====================================================================================================================
                dialogo.show(supportFragmentManager, "FragmentColor")
            }
        })*/

        // Aquí simplemente nos aseguramos que tenga el xml, si lo tiene no entramos.
        // En teoria ya todos los archivos no tienen el .xml porque lo recupero del ListarGuias
        if (!nombreArchivo!!.contains(".xml")) {
            nombreArchivo = intent.extras!!.getString("nombre_archivo") + ".xml"
        }

        // Obtenemos los datos del XML y los guardamos en su respectivo ArrayList.
        obtenerDatosXML()

        // Pintamos el texto en la pregunta actual
        pintarTexto(contadorPregunta)

        binding!!.imgvPregResp.setOnClickListener {
            if (binding!!.etPregResp.text.toString().isNotEmpty()) {
                var editable: Editable =
                    Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
                var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                    0,
                    editable.length,
                    ForegroundColorSpan::class.java
                )

                // Se colocan las etiquetas en cada palabra con color
                colocarEtiquetas(colorSpans, editable)


                if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                    if (contadorPregunta > (respuestas.size - 1)) {
                        binding!!.tilContenidoPregResp.hint = "Respuesta"
                        preguntas.add(contadorPregunta, editable.toString())
                        binding!!.etPregResp.setText("")
                    } else {
                        binding!!.tilContenidoPregResp.hint = "Respuesta"
                        preguntas[contadorPregunta] = editable.toString()
                        pintarTexto(contadorPregunta)
                    }
                    girarCardView()
                } else {
                    if (contadorPregunta > (respuestas.size - 1)) {
                        binding!!.tilContenidoPregResp.hint = "Pregunta"
                        respuestas.add(contadorPregunta, editable.toString())
                    } else {
                        binding!!.tilContenidoPregResp.hint = "Pregunta"
                        respuestas[contadorPregunta] = editable.toString()
                        pintarTexto(contadorPregunta)
                    }
                    girarCardView()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Asegurate de no dejar ningun campo vacio",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Validar que cuando este lleno y se regrese te diga que no está todo lleno.
        binding!!.imgvPrevious.setOnClickListener {
            // El contadorPregunta tiene que ser mayor a 0 sino significa que no hay preguntas anteriores.
            if (contadorPregunta > 0) {
                // Se le quita 1 para hacer referencia al arreglo
                // tamaño 3-1 = 2 [0,1,2].
                //val longi: Int = preguntas.size - 1 HAY QUE VALIDAR SI AQUÍ TAMBIÉN FUNCIONA COMENTANDO ESTE
                val longi: Int = respuestas.size - 1

                // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                // de lo que esté en la posición 0.
                if (contadorPregunta <= longi) {
                    // Validamos campos vacios en la pregunta y respuesta.
                    if (binding!!.etPregResp.text.toString().isEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "Asegurate de no dejar ningun campo vacio",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Se resta uno al final y así se queda neutral.
                        contadorPregunta++
                    } else {
                        // Si los campos están bien se sobre escribe.
                        var editable: Editable =
                            Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
                        var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                            0,
                            editable.length,
                            ForegroundColorSpan::class.java
                        )

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)

                        if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                            preguntas[contadorPregunta] = editable.toString()
                        } else {
                            respuestas[contadorPregunta] = editable.toString()
                        }

                        binding!!.tilContenidoPregResp.hint = "Pregunta"
                        // Pintamos el texto en la pregunta actual
                        pintarTexto(contadorPregunta - 1)
                    }
                } else {
                    if (binding!!.etPregResp.text.toString().isNotEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "Asegurate de llenar pregunta y una respuesta",
                            Toast.LENGTH_SHORT
                        ).show()

                        contadorPregunta++
                    } else {
                        if (binding!!.tilContenidoPregResp.hint == "Pregunta"){
                            pintarTexto(contadorPregunta - 1)
                        } else {
                            // Si los campos están bien se sobre escribe.
                            var editable: Editable =
                                Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
                            var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                                0,
                                editable.length,
                                ForegroundColorSpan::class.java
                            )

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable)

                            binding!!.tilContenidoPregResp.hint = "Pregunta"
                            respuestas.add(contadorPregunta, editable.toString())
                            pintarTexto(contadorPregunta - 1)
                        }
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

        binding!!.imgvNext.setOnClickListener {
            // Validamos campos vacios en la pregunta o respuesta.
            if (binding!!.etPregResp.text.toString().isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Asegurate de no dejar ningun campo vacio",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Se le quita 1 para hacer referencia al arreglo
                // tamaño 3-1 = 2 [0,1,2].
                //val longi: Int = preguntas.size - 1 HAY QUE VALIDAR SI AQUÍ TAMBIÉN FUNCIONA COMENTANDO ESTE
                val longi: Int = respuestas.size - 1

                if (contadorPregunta <= longi) {
                    if (binding!!.etPregResp.text.toString().isEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "Asegurate de llenar una pregunta y una respuesta",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        var editable: Editable =
                            Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
                        var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                            0,
                            editable.length,
                            ForegroundColorSpan::class.java
                        )

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)

                        if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                            preguntas[contadorPregunta] = editable.toString()
                        } else {
                            respuestas[contadorPregunta] = editable.toString()
                        }

                        // Mientras el contadorPregunta sea menor escribiremos la siguiente pregunta
                        // en los et.
                        if (contadorPregunta < longi) {
                            // Pintamos el texto en la pregunta actual

                            binding!!.tilContenidoPregResp.hint = "Pregunta"
                            pintarTexto(contadorPregunta + 1)
                        } else if (!dialMasPreg) {
                            // ¿Quieres agregar más preguntas?
                            AlertDialog.Builder(this@Activity_Modificar)
                                .setTitle("¡Atención!")
                                .setMessage("Se acabaron las preguntas, ¿Quieres agregar más?")
                                .setPositiveButton(
                                    "Si"
                                ) { dialogInterface, i -> // Cambiaremos el texto del toolbar.
                                    //binding!!.barraSuperiorRegreso.tvTituloToolbar.text =
                                    //    "Agrega más preguntas a la guía"
                                    binding!!.tilContenidoPregResp.hint = "Pregunta"
                                    binding!!.etPregResp.setText("")
                                    // binding!!.etRespuesta.setText("")
                                    // binding!!.etPregunta.requestFocus()
                                    // contadorPregunta++
                                    dialMasPreg = true
                                    Toast.makeText(
                                        applicationContext, "Ya puedes agregar " +
                                                "mas preguntas", Toast.LENGTH_LONG
                                    ).show()
                                }
                                .setNegativeButton(
                                    "Cancelar"
                                ) { dialog, i -> dialog.dismiss()
                                    contadorPregunta--
                                }.setOnCancelListener {
                                    contadorPregunta--
                                }.create().show()
                        } else {
                            // Si le das click a que si, ya no te preguntará nuevamente
                            // aunque te regreses a componer otras preg.
                            // Si el contadorPregunta es igual entonces solo escribiremos los campos vacios.
                            binding!!.tilContenidoPregResp.hint = "Pregunta"
                            binding!!.etPregResp.setText("")
                            binding!!.etPregResp.requestFocus()
                        }
                    }
                } else { // Si contadorPregunta es mayor a lo que hay en el arreglo.
                    if (binding!!.etPregResp.text.toString().isEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "Asegurate de llenar una pregunta y una respuesta",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Cuando no hay guardadas las mismas preguntas que respuestas.
                        if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                            Toast.makeText(
                                applicationContext,
                                "Asegurate de llenar una pregunta y una respuesta",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            var editable: Editable =
                                Editable.Factory.getInstance()
                                    .newEditable(binding!!.etPregResp.text)
                            var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                                0,
                                editable.length,
                                ForegroundColorSpan::class.java
                            )

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable)
                            respuestas.add(contadorPregunta, editable.toString())
                            binding!!.tilContenidoPregResp.hint = "Pregunta"
                            binding!!.etPregResp.setText("")
                        }
                    }
                }
                Log.i("Contador", contadorPregunta.toString())
                contadorPregunta++
                Log.i("Contador", contadorPregunta.toString())
            }
        }

        // Hay que probar que elimine bien tanto en pregunta como en respuesta.
        binding!!.imgvEliminar.setOnClickListener {
            AlertDialog.Builder(this@Activity_Modificar)
                .setTitle("¡Atención!")
                .setMessage("¿Quieres eliminar la pregunta?")
                .setPositiveButton("Si") { dialogInterface, i ->
                    // Se le quita 1 para hacer referencia al arreglo
                    // tamaño 3-1 = 2 [0,1,2].
                    val longi: Int = respuestas.size - 1

                    // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                    // de lo que esté en la posición 0.
                    if (contadorPregunta <= longi) {
                        preguntas.removeAt(contadorPregunta)
                        respuestas.removeAt(contadorPregunta)

                        // Mientras el contadorPregunta sea menor escribiremos la siguiente pregunta
                        // en los et.
                        /*if (contadorPregunta < longi) {
                            // Pintamos el texto en la pregunta actual
                        } else {
                            // Si el contadorPregunta es igual entonces solo escribiremos los campos vacios.
                            binding!!.etPregResp.setText("")
                            binding!!.etPregResp.requestFocus()
                            pintarTexto(contadorPregunta)
                        }*/

                        contadorPregunta--
                        pintarTexto(contadorPregunta)
                        binding!!.tilContenidoPregResp.hint = "Pregunta"
                    } else {
                        // Si el contadorPregunta es mayor entonces únicamente limpiamos los campos.
                        if (contadorPregunta == preguntas.size-1){
                            preguntas.removeAt(contadorPregunta)

                            if(contadorPregunta == respuestas.size-1){
                                respuestas.removeAt(contadorPregunta)
                            }
                            contadorPregunta--
                        }

                        pintarTexto(contadorPregunta)
                        binding!!.tilContenidoPregResp.hint = "Pregunta"
                    }
                }
                .setNegativeButton("Cancelar") { dialog, i ->
                    dialog.dismiss()
                }.create().show()
        }

        binding!!.imgvSave.setOnClickListener {
            // Se le quita 1 para hacer referencia al arreglo
            // tamaño 3-1 = 2 [0,1,2].
            val longi: Int = respuestas.size - 1

            if (contadorPregunta <= longi) {
                if (binding!!.etPregResp.text.toString().isEmpty()) {
                    Toast.makeText(
                        applicationContext,
                        "Asegurate de llenar una pregunta y una respuesta",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    var editable: Editable =
                        Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
                    var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                        0,
                        editable.length,
                        ForegroundColorSpan::class.java
                    )

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)

                    if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                        preguntas[contadorPregunta] = editable.toString()
                    } else {
                        respuestas[contadorPregunta] = editable.toString()
                    }

                    borrarCrearXML(nombreArchivo)
                }
            } else { // Si contadorPregunta es mayor a lo que hay en el arreglo.
                if (binding!!.etPregResp.text.toString()
                        .isEmpty() && binding!!.tilContenidoPregResp.hint == "Pregunta"
                ) {
                    borrarCrearXML(nombreArchivo)
                } else if (binding!!.etPregResp.text.toString()
                        .isNotEmpty() && binding!!.tilContenidoPregResp.hint == "Pregunta" || binding!!.etPregResp.text.toString()
                        .isEmpty() && binding!!.tilContenidoPregResp.hint == "Respuesta"
                ) {
                    Toast.makeText(
                        applicationContext,
                        "Asegurate de llenar una pregunta y una respuesta",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Cuando no hay guardadas las mismas preguntas que respuestas.
                    var editable: Editable =
                        Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
                    var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                        0,
                        editable.length,
                        ForegroundColorSpan::class.java
                    )

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)
                    respuestas.add(contadorPregunta, editable.toString())

                    borrarCrearXML(nombreArchivo)
                }
            }
        }

        /*binding!!.etRespuesta.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
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
        })*/

        binding!!.imgvQuitColor.setOnClickListener {
            binding!!.etPregResp.text = binding!!.etPregResp.text
        }

        /*binding!!.btnQuitarColores.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {

            }
        })*/

        // Cuando se seleccione desde donde se comenzará a pintar las cosas
        /*binding!!.etRespuesta.setOnFocusChangeListener { view, b ->
            val cursorPos = binding!!.etRespuesta.selectionStart

            // Haz algo con las coordenadas y la posición del cursor (por ejemplo, muestra en el registro)
            Log.i("Posicion", "Cursor Position: $cursorPos")
        }

        binding!!.etRespuesta.setOnClickListener {
            val cursorPos = binding!!.etRespuesta.selectionStart

            // Haz algo con las coordenadas y la posición del cursor (por ejemplo, muestra en el registro)
            Log.i("Posicion", "Cursor Position: $cursorPos")
        }*/
    }

    private fun girarCardView() {
        val flipAnimator =
            ObjectAnimator.ofFloat(binding!!.tilContenidoPregResp, "rotationY", 0f, 360f)
        flipAnimator.duration = 1000 // Duración de la animación en milisegundos
        flipAnimator.start()
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

    private fun colocarEtiquetas(colorSpans: Array<ForegroundColorSpan>, editable: Editable) {
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
        var inicio: Int = 0
        var fin: Int = 0
        var coloresPregunta: ColoresPregunta? = null
        var texto: String = ""
        if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
            texto = preguntas[contadorPregunta]
        } else {
            texto = respuestas[contadorPregunta]
        }
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

            if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                preguntasColor.add(contColorPreg, coloresPregunta)
            } else {
                respuestasColor.add(contColorPreg, coloresPregunta)
            }

            // Eliminar la primera etiqueta y su contenido
            texto = texto.replaceFirst("«.*?»".toRegex(), "")

            // Eliminar la segunda etiqueta y su contenido
            texto = texto.replaceFirst("«.*?»".toRegex(), "")
            contColorPreg++
        }

        builder = SpannableStringBuilder(texto)
        for (coloresPreguntas: ColoresPregunta in if (binding!!.tilContenidoPregResp.hint == "Pregunta") preguntasColor else respuestasColor) {
            val colorSpan: ForegroundColorSpan = ForegroundColorSpan(coloresPreguntas.color)
            builder!!.setSpan(
                colorSpan,
                coloresPreguntas.inicioColor,
                coloresPreguntas.finColor,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        preguntasColor.clear()
        respuestasColor.clear()
        binding!!.etPregResp.text = builder
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

    /*fun colorActual(colorActual: Int) {
        @SuppressLint("UseCompatLoadingForDrawables") val drawable: Drawable =
            resources.getDrawable(R.drawable.boton_redondo)
        drawable.setColorFilter(colorActual, PorterDuff.Mode.SRC_ATOP)
        binding!!.btnColorActual.background = drawable

        // Recibimos el nombre del archivo del popupFragment Nueva Guia.
        this.colorActual = colorActual
    }*/
}