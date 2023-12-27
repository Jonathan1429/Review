package com.jonathanev.review.Activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Xml
import android.view.KeyEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.activity.result.registerForActivityResult
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.jonathanev.review.Clases.ColoresPregunta
import com.jonathanev.review.Fragments.Fragment_DialogColores_popup
import com.jonathanev.review.databinding.ActivityCuestionarioBinding
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Activity_Cuestionario : AppCompatActivity() {
    private var binding: ActivityCuestionarioBinding? = null
    private var nombreArchivo: String? = null
    private var colorActual: Int = 0
    private val preguntas: ArrayList<String> = ArrayList()
    private val respuestas: ArrayList<String> = ArrayList()
    private val preguntasColor: ArrayList<ColoresPregunta> = ArrayList()
    private val respuestasColor: ArrayList<ColoresPregunta> = ArrayList()
    var builder: SpannableStringBuilder? = null
    private var contadorPregunta: Int = 0

    private var start = -1
    private var end = -1

    // Seleccionar imagen
    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null){
            val name = applicationContext.packageName
            applicationContext.grantUriPermission(name, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            binding!!.ivImagen.setImage(ImageSource.uri(uri)) //setImageURI(uri)
            binding!!.tilContenidoPregResp.visibility = View.GONE
            binding!!.ivImagen.visibility = View.VISIBLE
            binding!!.etPregResp.setText(uri.toString())
            Log.i("Uri: ", uri.toString())
        }
    }

    /*private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            //Glide.with(this).load(uri).into(binding!!.ivImagen)
            binding!!.ivImagen.setImage(ImageSource.uri(uri)) //setImageURI(uri)
            binding!!.tilContenidoPregResp.visibility = View.GONE
            binding!!.ivImagen.visibility = View.VISIBLE
            binding!!.etPregResp.setText(uri.toString())
            Log.i("Uri: ", uri.toString())
        } else {
            binding!!.tilContenidoPregResp.visibility = View.VISIBLE
            binding!!.ivImagen.visibility = View.GONE
        }
    }*/

    // Creamos la serialización y la clase para crear archivos de manera global.
    var serializer: XmlSerializer = Xml.newSerializer()
    var fos: FileOutputStream? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuestionarioBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Sección de anuncios
        initLoadAds()

        // Recibimos el nombre del archivo del popupFragment Nueva Guia.
        nombreArchivo = intent.extras!!.getString("nombre_archivo")

        // Se cambia el nombre del titulo del toolbar
        binding!!.barraSuperiorRegreso.tvTituloToolbar.text = "Creando: $nombreArchivo"
        colorActual = Color.BLACK
        setColor(colorActual)

        binding!!.barraSuperiorRegreso.imgvBack.setOnClickListener { cancelarArchivo() }

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
                    } else {
                        // Si le das click a que si, ya no te preguntará nuevamente
                        // aunque te regreses a componer otras preg.
                        // Si el contadorPregunta es igual entonces solo escribiremos los campos vacios.
                        binding!!.tilContenidoPregResp.hint = "Pregunta"
                        binding!!.etPregResp.setText("")
                    }
                } else { // Si contadorPregunta es mayor a lo que hay en el arreglo.
                    if (binding!!.etPregResp.text.toString().isEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "Asegurate de llenar una pregunta y una respuesta",
                            Toast.LENGTH_SHORT
                        ).show()
                        contadorPregunta--
                    } else {
                        // Cuando no hay guardadas las mismas preguntas que respuestas.
                        if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                            Toast.makeText(
                                applicationContext,
                                "Asegurate de llenar una pregunta y una respuesta",
                                Toast.LENGTH_SHORT
                            ).show()
                            contadorPregunta--
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
                contadorPregunta++
            }

            /*// contadorPregunta++

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
                val longi: Int = preguntas.size - 1

                // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                // de lo que esté en la posición 0.
                if (contadorPregunta < longi) {
                    var editable: Editable = Editable.Factory.getInstance().newEditable(
                        binding!!.etPregResp.text
                    )
                    var colorSpans: Array<ForegroundColorSpan> =
                        editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)

                    respuestas[contadorPregunta] = editable.toString()
                    pintarTexto(contadorPregunta + 1)

                    // Mientras el contadorPregunta sea menor escribiremos la siguiente pregunta
                    // en los et y se borran las etiquetas de colores.
                    /*if (contadorPregunta < longi) {
                        // Pintamos el texto

                    } else {
                        // Si el contadorPregunta es igual entonces solo escribiremos los campos vacios.
                        respuestas.add(contadorPregunta, editable.toString())
                        binding!!.etPregResp.setText("")
                    }*/
                } else {
                    // Si el contadorPregunta es mayor entonces agregaremos la pregunta actual a los
                    // arreglos.«»
                    var editable: Editable = Editable.Factory.getInstance()
                        .newEditable(binding!!.etPregResp.text)
                    var colorSpans: Array<ForegroundColorSpan> =
                        editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)
                    respuestas.add(contadorPregunta, editable.toString())
                }

                binding!!.tilContenidoPregResp.hint = "Pregunta"
                binding!!.etPregResp.setText("")
                contadorPregunta++


                if(binding!!.tilContenidoPregResp.hint == "Pregunta"){
                    Toast.makeText(
                        applicationContext,
                        "Asegurate de llenar una pregunta y una respuesta",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                }
            }*/
        }

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
                    if (binding!!.tilContenidoPregResp.hint == "Pregunta" && binding!!.etPregResp.text.toString()
                            .isNotEmpty() ||
                        binding!!.tilContenidoPregResp.hint == "Respuesta" && binding!!.etPregResp.text.toString()
                            .isEmpty()
                    ) {
                        Toast.makeText(
                            applicationContext,
                            "Asegurate de llenar pregunta y respuesta",
                            Toast.LENGTH_SHORT
                        ).show()

                        contadorPregunta++
                    } else {
                        if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                            pintarTexto(contadorPregunta - 1)
                        } else {
                            // Si los campos están bien se sobre escribe.
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

        binding!!.imgvEliminar.setOnClickListener {
            AlertDialog.Builder(this@Activity_Cuestionario)
                .setTitle("¡Atención!")
                .setMessage("¿Quieres eliminar la pregunta?")
                .setPositiveButton("Si") { dialogInterface, i ->
                    // Se le quita 1 para hacer referencia al arreglo
                    // tamaño 3-1 = 2 [0,1,2].
                    val longi: Int = respuestas.size

                    if (longi == 0) {
                        //preguntas.removeFirst()
                        //respuestas.removeFirst()

                        if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                            binding!!.etPregResp.setText("")
                        } else {
                            binding!!.tilContenidoPregResp.hint = "Pregunta"
                            binding!!.etPregResp.setText("")
                        }
                    } else if (contadorPregunta <= longi) {
                        // Solo si es mayor a 0 se resta, cuando se elimina una pregunta se acomoda
                        // el arreglo desde la posición 0 a la n
                        if (contadorPregunta < longi) {
                            if (contadorPregunta > 0) {
                                // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                                // de lo que esté en la posición 0.
                                preguntas.removeAt(contadorPregunta)
                                respuestas.removeAt(contadorPregunta)
                                contadorPregunta--
                                pintarTexto(contadorPregunta)
                                binding!!.tilContenidoPregResp.hint = "Pregunta"
                            } else {
                                // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                                // de lo que esté en la posición 0.
                                preguntas.removeAt(contadorPregunta)
                                respuestas.removeAt(contadorPregunta)
                                pintarTexto(contadorPregunta)
                                binding!!.tilContenidoPregResp.hint = "Pregunta"
                            }
                        } else {
                            // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                            // de lo que esté en la posición 0.
                            preguntas.removeAt(contadorPregunta)
                            respuestas.removeAt(contadorPregunta)
                            contadorPregunta--
                            pintarTexto(contadorPregunta)
                            binding!!.tilContenidoPregResp.hint = "Pregunta"
                        }
                    } else {
                        if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                            binding!!.etPregResp.setText("")
                        } else {
                            preguntas.removeAt(contadorPregunta)
                            binding!!.tilContenidoPregResp.hint = "Pregunta"
                            binding!!.etPregResp.setText("")
                        }

                        contadorPregunta--
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
            val longi: Int = respuestas.size

            if (binding!!.etPregResp.text.toString().isEmpty()) {
                if (respuestas.isEmpty() && binding!!.tilContenidoPregResp.hint == "Pregunta") {
                    Toast.makeText(
                        applicationContext,
                        "Debes tener como minimo una pregunta",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if ((contadorPregunta + 1) > longi && binding!!.tilContenidoPregResp.hint == "Pregunta") {
                    crearArchivo(nombreArchivo)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Asegurate de llenar una pregunta y una respuesta",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                    if ((contadorPregunta + 1) <= longi && longi > 0) {
                        var editable: Editable =
                            Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
                        var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                            0,
                            editable.length,
                            ForegroundColorSpan::class.java
                        )

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)
                        preguntas[contadorPregunta] = editable.toString()

                        crearArchivo(nombreArchivo)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Asegurate de llenar una pregunta y una respuesta",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    if (longi == 0) {
                        var editable: Editable =
                            Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
                        var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                            0,
                            editable.length,
                            ForegroundColorSpan::class.java
                        )

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)
                        respuestas.add(editable.toString())

                        crearArchivo(nombreArchivo)
                    } else {
                        if ((contadorPregunta + 1) <= longi) {
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
                            respuestas[contadorPregunta] = editable.toString()

                            crearArchivo(nombreArchivo)
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
                            respuestas.add(editable.toString())

                            crearArchivo(nombreArchivo)
                        }
                    }
                }
            }
        }

        // Visualización del DialogFragment de selección de colores.
        binding!!.imgvColors.setOnClickListener {
            // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
            val dialogo: Fragment_DialogColores_popup = Fragment_DialogColores_popup()
            //=====================================================================================================================
            dialogo.show(supportFragmentManager, "FragmentColor")
        }

        // Cambio de botones visibles
        binding!!.imgvSelColor.setOnClickListener {
            binding!!.imgvEliminar.visibility = View.GONE
            binding!!.imgvSelColor.visibility = View.GONE
            binding!!.imgvPregResp.visibility = View.GONE
            binding!!.imgvSave.visibility = View.GONE

            binding!!.imgvColors.visibility = View.VISIBLE
            binding!!.imgvCheck.visibility = View.VISIBLE
            binding!!.imgvCancelar.visibility = View.VISIBLE
            binding!!.imgvQuitColor.visibility = View.VISIBLE
        }

        // Cambio de botones visibles
        binding!!.imgvCancelar.setOnClickListener {
            binding!!.imgvEliminar.visibility = View.VISIBLE
            binding!!.imgvSelColor.visibility = View.VISIBLE
            binding!!.imgvPregResp.visibility = View.VISIBLE
            binding!!.imgvSave.visibility = View.VISIBLE

            binding!!.imgvColors.visibility = View.GONE
            binding!!.imgvCheck.visibility = View.GONE
            binding!!.imgvCancelar.visibility = View.GONE
            binding!!.imgvQuitColor.visibility = View.GONE

            start = -1
            end = -1
        }

        // Pintar el texto en el ET
        binding!!.imgvCheck.setOnClickListener {
            val text: Editable?
            val spannableStringBuilder: SpannableStringBuilder

            // La posición en el ET comienza en el 0 por eso vale -1.
            if (start == -1) {
                start = binding!!.etPregResp.selectionStart

                Toast.makeText(
                    applicationContext,
                    "Pon el cursor hasta donde quieres pintar",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                end = binding!!.etPregResp.selectionStart
                text = binding!!.etPregResp.text
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

                binding!!.etPregResp.text = spannableStringBuilder
                binding!!.etPregResp.setSelection(end)
                start = -1
                end = -1
            }
        }

        // Eliminar textos con colores
        binding!!.imgvQuitColor.setOnClickListener {
            val text = binding!!.etPregResp.text
            val spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder(text)
            spannableStringBuilder.clearSpans()

            binding!!.etPregResp.text = spannableStringBuilder
        }

        binding!!.imgvImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
    }

    private fun initLoadAds() {
        MobileAds.initialize(this) { }

        val adRequest = AdRequest.Builder().build()
        binding!!.adView.loadAd(adRequest)

        binding!!.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
            }

            override fun onAdOpened() {
            }

            override fun onAdClicked() {
            }

            override fun onAdClosed() {
            }
        }
        /*onAdLoaded: Se llamará cuando el anuncio haya cargado.
        onAdFailedToLoad: Si el anuncio falla al intentar cargar la publicidad se llamará a este método para que podamos volver a intentarlo u ocultar el anuncio.
        onAdOpened: Cuando la publicidad ha sido abierta.
        onAdClicked: Se ejecutará este método cuando se haga clic en el banner.
        onAdLeftApplication: Cuando el usuario abandone la aplicación.
        onAdClosed: Se llama al cerrar la publicidad.*/
    }

    private fun girarCardView() {
        if (binding!!.tilContenidoPregResp.isVisible){
            val flipAnimator =
                ObjectAnimator.ofFloat(binding!!.tilContenidoPregResp, "rotationY", 0f, 360f)
            flipAnimator.duration = 1000 // Duración de la animación en milisegundos
            flipAnimator.start()
        } else {
            val flipAnimator =
                ObjectAnimator.ofFloat(binding!!.ivImagen, "rotationY", 0f, 360f)
            flipAnimator.duration = 1000 // Duración de la animación en milisegundos
            flipAnimator.start()
            flipAnimator.doOnEnd {
                showImageOrText()
                //growCard()
                //binding!!.ivImagen.visibility = View.GONE
                //binding!!.tilContenidoPregResp.visibility = View.VISIBLE
            }
        }
    }

    private fun showImageOrText() {
        val disappearAnimation = AlphaAnimation(1.0f, 0.0f)
        disappearAnimation.duration = 200

        val appearAnimation = AlphaAnimation(0.0f, 1.0f)
        appearAnimation.duration = 1000

        disappearAnimation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                binding!!.ivImagen.visibility = View.GONE
                binding!!.tilContenidoPregResp.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })

        binding!!.ivImagen.startAnimation(disappearAnimation)
        binding!!.tilContenidoPregResp.startAnimation(appearAnimation)
    }

    // Método que se ejecuta cuando el back del telefono es presionado.
    public override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            cancelarArchivo()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // Mostrará un mensaje diciendo que el archivo se eliminará ya que no se terminó de crear.
    private fun cancelarArchivo() {
        // Se ejecuta cuando se regresa sin guardar.
        AlertDialog.Builder(this@Activity_Cuestionario)
            .setTitle("¡Atención!")
            .setMessage(
                "Aún no terminas de crear la guia, se borrará el " +
                        "archivo creado, ¿seguro deseas continuar?"
            )
            .setPositiveButton(
                "Continuar"
            ) { dialogInterface, i -> // Si el archivo se creó y existe, se elimina y te informa en consola
                @SuppressLint("SdCardPath") val file: File =
                    File("/data/data/com.jonathanev.repasar/files/")
                if (file.exists()) {
                    File(file, "$nombreArchivo.xml").delete()
                    Log.d("ArchivoEliminado", "Archivo eliminado")
                } else {
                    Log.d("ArchivoEliminado", "Archivo no eliminado")
                }
                onBackPressed()
            }
            .setNegativeButton(
                "Cancelar"
            ) { dialog, i -> dialog.dismiss() }.create().show()
    }

    // Creamos el archivo en el dispositivo e inicializamos algunas etiquetas.
    private fun crearArchivo(nombreArchivo: String?) {
        try {
            fos = openFileOutput("$nombreArchivo.xml", MODE_PRIVATE)
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
                applicationContext, "Guia de estudio creada exitosamente",
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

    /*fun colocarEtiquetas(colorSpans: Array<ForegroundColorSpan>, editable: Editable) {
        for (colorSpan: ForegroundColorSpan in colorSpans) {
            val start: Int = editable.getSpanStart(colorSpan)
            val end: Int = editable.getSpanEnd(colorSpan)
            val color: Int = colorSpan.foregroundColor

            // Agregar la etiqueta de inicio al texto
            val etiqIni: String = "«$color»"
            val etiqFin: String = "«/$color»"
            editable.replace(start, start, etiqIni)

            // Agregar la etiqueta de cierre al texto
            editable.replace(end + etiqIni.length, end + etiqIni.length, etiqFin)
        }
    }*/

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

    fun setColor(@ColorInt color: Int?) {
        if (color == null) {
            ImageViewCompat.setImageTintList(binding!!.imgvColors, null)
            return
        }
        ImageViewCompat.setImageTintMode(binding!!.imgvColors, PorterDuff.Mode.SRC_ATOP)
        ImageViewCompat.setImageTintList(binding!!.imgvColors, ColorStateList.valueOf(color))
        colorActual = color
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