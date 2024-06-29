package com.jonathanev.review.UI.View

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Xml
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.core.widget.ImageViewCompat
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.jonathanev.review.Core.Constants.PICK_IMAGE_REQUEST
import com.jonathanev.review.Core.Constants.baseRutaImagen
import com.jonathanev.review.Core.Constants.baseRutaImagenCifrado
import com.jonathanev.review.Core.Constants.file
import com.jonathanev.review.Core.Constants.fileImages
import com.jonathanev.review.Core.Constants.fileImagesPiv
import com.jonathanev.review.Core.Constants.rutaPrin
import com.jonathanev.review.Data.Model.ColorPregModel
import com.jonathanev.review.Fragments.Fragment_DialogColores_popup
import com.jonathanev.review.UI.ViewModel.ActivityCuestionarioViewModel
import com.jonathanev.review.databinding.ActivityCuestionarioBinding
import dagger.hilt.android.AndroidEntryPoint
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@AndroidEntryPoint
class Activity_Cuestionario : AppCompatActivity() {
    private var binding: ActivityCuestionarioBinding? = null
    private var nombreArchivo: String? = null
    private var colorActual: Int = 0
    private var colorPintarPalabra: Int = 0
    private var posColorInicial: Int = -1
    private var posColorFinal: Int = -1
    private val preguntas: ArrayList<String> = ArrayList()
    private val respuestas: ArrayList<String> = ArrayList()
    private val preguntasColor: ArrayList<ColorPregModel> = ArrayList()
    private val respuestasColor: ArrayList<ColorPregModel> = ArrayList()
    var builder: SpannableStringBuilder? = null
    private var contadorPregunta: Int = 0
    private var contadorImagen = 0
    private var uri: Uri? = null
    private var longCaracteres = 0
    private var pregResBandera = false // Bandera para cuando se le de click atras o delante.
    private val activityCuestionarioViewModel by viewModels<ActivityCuestionarioViewModel>()
    private var filename: String = "" // Ruta/imagen.png

    // Seleccionar imagen
    private val pickMedia =
        registerForActivityResult(PickVisualMedia()) { uri ->
            if (uri != null) {
                // Toma permisos de persistencia para la URI
                takePersistableUriPermission(uri)

                if (binding!!.etPregResp.text!!.isNotEmpty()) {
                    AlertDialog.Builder(this@Activity_Cuestionario)
                        .setTitle("¡Atención!")
                        .setMessage("Se borrará el texto para agregar la imagen, ¿Quieres continuar?")
                        .setPositiveButton(
                            "Si"
                        ) { _, _ ->
                            binding!!.ivImagen.setImage(ImageSource.uri(uri)) //setImageURI(uri)
                            binding!!.tilContenidoPregResp.visibility = View.GONE

                            binding!!.ivImagen.visibility = View.VISIBLE
                            binding!!.etPregResp.setText(uri.toString())
                        }
                        .setNegativeButton(
                            "Cancelar"
                        ) { dialog, _ ->
                            dialog.dismiss()
                        }.create().show()
                } else {
                    binding!!.ivImagen.setImage(ImageSource.uri(uri)) //setImageURI(uri)
                    binding!!.tilContenidoPregResp.visibility = View.GONE

                    binding!!.ivImagen.visibility = View.VISIBLE
                    binding!!.etPregResp.setText(uri.toString())
                }
            } else {
                binding!!.imgvCancelar.visibility = View.GONE

                binding!!.imgvQuitColor.visibility = View.VISIBLE
                binding!!.imgvSelColor.visibility = View.VISIBLE
            }
        }

    // Creamos la serialización y la clase para crear archivos de manera global.
    var serializer: XmlSerializer = Xml.newSerializer()
    var fos: FileOutputStream? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuestionarioBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            PICK_IMAGE_REQUEST
        )

        // Sección de anuncios
        initLoadAds()
        initUI()

        // Recibimos el nombre del archivo del popupFragment Nueva Guia.
        nombreArchivo = intent.extras!!.getString("nombre_archivo")

        // Se cambia el nombre del titulo del toolbar
        binding!!.barraSuperiorRegreso.tvTituloToolbar.text = "Creando: $nombreArchivo"
        colorActual = Color.BLACK

        binding!!.barraSuperiorRegreso.imgvBack.setOnClickListener {
            cancelarArchivo()
            deleteImages()
        }

        binding!!.imgvPregResp.setOnClickListener {
            activityCuestionarioViewModel.setColorAnterior(colorPintarPalabra)
            activityCuestionarioViewModel.clickedRoll()
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

                        Log.i("Crear pregunta: ", "Asegurate de no dejar ningun campo vacio")
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

                        if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                            preguntas[contadorPregunta] = editable.toString()
                        } else {
                            respuestas[contadorPregunta] = editable.toString()
                        }

                        binding!!.lblPregResp.text = "Pregunta"
                        // Pintamos el texto en la pregunta actual
                        pintarTexto(contadorPregunta - 1)
                    }
                } else {
                    if (binding!!.lblPregResp.text.toString() == "Pregunta" && binding!!.etPregResp.text.toString()
                            .isNotEmpty() ||
                        binding!!.lblPregResp.text.toString() == "Respuesta" && binding!!.etPregResp.text.toString()
                            .isEmpty()
                    ) {
                        Toast.makeText(
                            applicationContext,
                            "Asegurate de llenar pregunta y respuesta",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.i("Crear pregunta: ", "Asegurate de llenar pregunta y respuesta")
                        contadorPregunta++
                    } else {
                        if (binding!!.lblPregResp.text.toString() == "Respuesta") {
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

                            respuestas.add(contadorPregunta, editable.toString())
                            binding!!.lblPregResp.text = "Pregunta"
                        }
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

                Log.i("Crear pregunta: ", "Ya no tienes preguntas anteriores")
            }
        }

        binding!!.imgvNext.setOnClickListener {
            // Validamos campos vacios en la pregunta o respuesta.
            val longi: Int = respuestas.size - 1

            if ((contadorPregunta <= longi && binding!!.etPregResp.text.toString()
                    .isEmpty()) || (binding!!.lblPregResp.text == "Pregunta" && contadorPregunta > longi) || binding!!.etPregResp.text.toString()
                    .isEmpty()
            ) {
                Toast.makeText(
                    applicationContext,
                    "Asegurate de no dejar ningun campo vacio",
                    Toast.LENGTH_SHORT
                ).show()

                Log.i("Crear pregunta: ", "Asegurate de no dejar ningun campo vacio")
            } else {
                // Se le quita 1 para hacer referencia al arreglo
                // tamaño 3-1 = 2 [0,1,2].
                //val longi: Int = preguntas.size - 1 HAY QUE VALIDAR SI AQUÍ TAMBIÉN FUNCIONA COMENTANDO ESTE

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

                    if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                        preguntas[contadorPregunta] = editable.toString()
                    } else {
                        respuestas[contadorPregunta] = editable.toString()
                    }

                    // Mientras el contadorPregunta sea menor escribiremos la siguiente pregunta
                    // en los et.
                    if (contadorPregunta < longi) {
                        // Pintamos el texto en la pregunta actual
                        binding!!.lblPregResp.text = "Pregunta"
                        pintarTexto(contadorPregunta + 1)
                    } else {
                        // binding!!.lblPregResp.text = "Pregunta"
                        binding!!.tilContenidoPregResp.visibility = View.VISIBLE
                        binding!!.ivImagen.visibility = View.GONE
                        binding!!.etPregResp.setText("")
                    }
                } else { // Si contadorPregunta es mayor a lo que hay en el arreglo.
                    binding!!.tilContenidoPregResp.visibility = View.VISIBLE
                    binding!!.ivImagen.visibility = View.GONE

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
                    binding!!.etPregResp.setText("")
                }

                binding!!.imgvCancelar.visibility = View.GONE
                binding!!.imgvQuitColor.visibility = View.VISIBLE
                binding!!.imgvSelColor.visibility = View.VISIBLE

                binding!!.lblPregResp.text = "Pregunta"

                contadorPregunta++
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

                        if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                            binding!!.etPregResp.setText("")
                        } else {
                            binding!!.lblPregResp.text = "Pregunta"
                            binding!!.etPregResp.setText("")
                        }
                    } else if ((contadorPregunta + 1) == longi && (contadorPregunta + 1) == 1) {
                        preguntas.removeAt(contadorPregunta)
                        respuestas.removeAt(contadorPregunta)
                        binding!!.lblPregResp.text = "Pregunta"
                        binding!!.imgvSelColor.visibility = View.VISIBLE
                        binding!!.tilContenidoPregResp.visibility = View.VISIBLE
                        binding!!.ivImagen.visibility = View.GONE
                        binding!!.etPregResp.setText("")
                    } else if ((contadorPregunta + 1) == longi) {
                        preguntas.removeAt(contadorPregunta)
                        respuestas.removeAt(contadorPregunta)
                        contadorPregunta--
                        binding!!.lblPregResp.text = "Pregunta"
                        pintarTexto(contadorPregunta)
                    } else if (contadorPregunta < longi) {
                        preguntas.removeAt(contadorPregunta)
                        respuestas.removeAt(contadorPregunta)
                        binding!!.lblPregResp.text = "Pregunta"
                        pintarTexto(contadorPregunta)
                    } else { // Cuando el contador es mayor a longi
                        if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                            contadorPregunta--
                            pintarTexto(contadorPregunta)
                        } else {
                            preguntas.removeAt(contadorPregunta)
                            contadorPregunta--
                            pintarTexto(contadorPregunta)
                        }
                    }
                }

                .setNegativeButton("Cancelar") { dialog, i ->
                    dialog.dismiss()
                }.create().show()
        }

        binding!!.barraSuperiorRegreso.imgvSave.setOnClickListener {
            activityCuestionarioViewModel.setColorAnterior(colorPintarPalabra)
            activityCuestionarioViewModel.clickedSave()
        }

        // Visualización del DialogFragment de selección de colores.
        binding!!.imgvSelColor.setOnClickListener {
            // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
            val dialogo: Fragment_DialogColores_popup = Fragment_DialogColores_popup()
            //=====================================================================================================================
            dialogo.show(supportFragmentManager, "FragmentColor")
        }

        // Eliminar textos con colores
        binding!!.imgvQuitColor.setOnClickListener {
            val text = binding!!.etPregResp.text
            val spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder(text)
            spannableStringBuilder.clearSpans()

            binding!!.etPregResp.text = spannableStringBuilder

            colorActual = Color.BLACK
            setColor(colorActual)
        }

        binding!!.imgvImage.setOnClickListener {
            binding!!.imgvSelColor.visibility = View.GONE
            binding!!.imgvQuitColor.visibility = View.GONE

            binding!!.imgvCancelar.visibility = View.VISIBLE

            // pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            openSomeActivityForResult()
        }

        // Cambio de botones visibles
        binding!!.imgvCancelar.setOnClickListener {
            binding!!.imgvSelColor.visibility = View.VISIBLE
            binding!!.imgvQuitColor.visibility = View.VISIBLE
            binding!!.tilContenidoPregResp.visibility = View.VISIBLE

            binding!!.ivImagen.visibility = View.GONE
            binding!!.imgvCancelar.visibility = View.GONE

            binding!!.etPregResp.setText("")
        }

        binding!!.etPregResp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                longCaracteres = binding!!.etPregResp.length()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(texto: Editable?) {
                val lv_lonCaracAct = binding!!.etPregResp.length()

                if (!texto.toString()
                        .contains(baseRutaImagenCifrado) && (lv_lonCaracAct - longCaracteres) == 1
                ) {
                    if (colorActual != -16777216) {
                        pintarLetra(texto)
                    }
                } /*else {
                activityCuestionarioViewModel.getColorAnteriorInicial()
            }*/
            }
        })

        activityCuestionarioViewModel.saveClicked.observe(this) {
            if (it) {
                // Se le quita 1 para hacer referencia al arreglo
                // tamaño 3-1 = 2 [0,1,2].
                val longi: Int = respuestas.size

                if (binding!!.etPregResp.text.toString().isEmpty()) {
                    if (respuestas.isEmpty() && binding!!.lblPregResp.text.toString() == "Pregunta") {
                        Toast.makeText(
                            applicationContext,
                            "Debes tener como minimo una pregunta",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.i("Crear pregunta: ", "Debes tener como minimo una pregunta")

                        activityCuestionarioViewModel.clickedSave()
                    } else if ((contadorPregunta + 1) > longi && binding!!.lblPregResp.text.toString() == "Pregunta") {
                        crearArchivo(nombreArchivo)
                        binding!!.ivImagen.visibility = View.GONE
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Asegurate de llenar una pregunta y una respuesta",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.i(
                            "Crear pregunta: ",
                            "Asegurate de llenar una pregunta y una respuesta"
                        )

                        activityCuestionarioViewModel.clickedSave()
                    }
                } else {
                    if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                        if ((contadorPregunta + 1) <= longi && longi > 0) {
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
                            preguntas[contadorPregunta] = editable.toString()

                            crearArchivo(nombreArchivo)
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Asegurate de llenar una pregunta y una respuesta",
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.i(
                                "Crear pregunta: ",
                                "Asegurate de llenar una pregunta y una respuesta"
                            )

                            activityCuestionarioViewModel.clickedSave()
                        }
                    } else {
                        if (longi == 0) {
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
        }

        activityCuestionarioViewModel.rollClicked.observe(this) {
            if (it) {
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

                    if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                        if ((contadorPregunta + 1) > respuestas.size) {
                            binding!!.lblPregResp.text = "Respuesta"
                            // binding!!.lblPregResp.text = "Respuesta"
                            preguntas.add(contadorPregunta, editable.toString())
                            binding!!.etPregResp.setText("")
                            binding!!.ivImagen.visibility = View.GONE
                            binding!!.tilContenidoPregResp.visibility = View.VISIBLE

                            binding!!.imgvCancelar.visibility = View.GONE
                            binding!!.imgvQuitColor.visibility = View.VISIBLE
                            binding!!.imgvSelColor.visibility = View.VISIBLE
                        } else {
                            binding!!.lblPregResp.text = "Respuesta"
                            preguntas[contadorPregunta] = editable.toString()
                            pintarTexto(contadorPregunta)
                            // binding!!.lblPregResp.text = "Respuesta"
                        }
                        girarCardView()
                    } else {
                        if ((contadorPregunta + 1) > respuestas.size) {
                            binding!!.lblPregResp.text = "Pregunta"
                            respuestas.add(contadorPregunta, editable.toString())
                            pintarTexto(contadorPregunta)
                            // binding!!.lblPregResp.text = "Pregunta"
                        } else {
                            binding!!.lblPregResp.text = "Pregunta"
                            respuestas[contadorPregunta] = editable.toString()
                            pintarTexto(contadorPregunta)
                            // binding!!.lblPregResp.text = "Pregunta"
                        }
                        girarCardView()
                    }

                    activityCuestionarioViewModel.clickedRoll()
                    posColorFinal = -1
                    posColorInicial = -1
                    colorPintarPalabra = 0
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Asegurate de no dejar ningun campo vacio",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.i("Crear pregunta: ", "Asegurate de no dejar ningun campo vacio")
                }
            }
        }

        activityCuestionarioViewModel.colorAnterior.observe(this) {
            if (posColorInicial == -1) {
                colorPintarPalabra = it

                val cursorPosition = binding!!.etPregResp.selectionStart
                val lastCharIndex = cursorPosition - 1
                posColorInicial = lastCharIndex
            } else {
                val cursorPosition = binding!!.etPregResp.selectionStart
                posColorFinal = cursorPosition

                // Obtener los spans dentro del rango especificado
                val spansToRemove = binding!!.etPregResp.text!!.getSpans(
                    posColorInicial,
                    posColorFinal,
                    ForegroundColorSpan::class.java
                )

                for (span in spansToRemove) {
                    binding!!.etPregResp.text!!.removeSpan(span)
                }

                binding!!.etPregResp.text!!.setSpan(
                    ForegroundColorSpan(colorPintarPalabra),
                    posColorInicial,
                    posColorFinal,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                posColorInicial = -1
                posColorFinal = -1
                colorPintarPalabra = 0
            }
        }

        activityCuestionarioViewModel.contImagenes.observe(this@Activity_Cuestionario) { contImagen ->
            contadorImagen = contImagen
            filename = "$contadorImagen.png"
        }
    }


    /*private suspend fun guardarContadorImagen(contadorImagen: Int) {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey("contador")] = contadorImagen
        }
    }*/

    private fun deleteImages() {
        if (fileImagesPiv.exists()) {
            borrarContenidoEnPiv()
        }
    }

    private fun borrarContenidoEnPiv() {
        val files = fileImagesPiv.listFiles()
        if (files != null) {
            for (subFile in files) {
                subFile.delete()
            }
        }
    }

    /*private fun getCountImage() = dataStore.data.map { preferences ->
        ImagenCont(contadorImagen = preferences[intPreferencesKey("contador")] ?: 75)
    }*/

    private fun initUI() {
        activityCuestionarioViewModel.getCountImage()

        /*val ltImages = fileImages.listFiles()
        val imagenes = mutableListOf<String>()
        if (ltImages!!.isNotEmpty()) {
            for (i in ltImages.indices) {
                // Sacamos del array files el primer fichero.
                val archivo: File = ltImages[i]
                var name = ""

                if (!archivo.isDirectory) {                    // Folder (guias)
                    name = archivo.name
                    imagenes.add(name)
                }
            }
        }

        if (imagenes.isNotEmpty()) {
            for (i in imagenes) {
                var ultimaImagen = i.substringAfterLast("/")
                ultimaImagen = ultimaImagen.replace(".png".toRegex(), "")
                var ultimaImagenEntero = ultimaImagen.toInt()
                if (contadorImagen < ultimaImagenEntero) {
                    contadorImagen = ultimaImagenEntero
                }
            }

            contadorImagen += 1
            filename = "$contadorImagen.png"
        } else {
            filename = "$contadorImagen.png"
        }*/
    }

    private fun openSomeActivityForResult() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private var resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null && data.data != null) {
                val selectedImageUri: Uri? = data.data
                if (selectedImageUri != null) {
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                    saveImageToInternalStorage(bitmap)
                }
            }
        } else {
            binding!!.imgvCancelar.visibility = View.GONE

            binding!!.imgvQuitColor.visibility = View.VISIBLE
            binding!!.imgvSelColor.visibility = View.VISIBLE
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        var fos: FileOutputStream? = null
        try {
            // val f = File(fileImagesPiv, filename)
            fos = openFileOutput(filename, MODE_PRIVATE)
            // fos = FileOutputStream(f)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)

            if (binding!!.etPregResp.text!!.isNotEmpty() && !binding!!.etPregResp.text!!.contains(
                    baseRutaImagen
                )
            ) {
                AlertDialog.Builder(this@Activity_Cuestionario)
                    .setTitle("¡Atención!")
                    .setMessage("Se borrará el contenido para agregar la imagen, ¿Quieres continuar?")
                    .setCancelable(false)
                    .setPositiveButton(
                        "Si"
                    ) { _, _ ->
                        Files.copy(
                            Paths.get("$rutaPrin/$filename"),
                            Paths.get("$fileImagesPiv/$filename"),
                            StandardCopyOption.REPLACE_EXISTING
                        )

                        // Borrar archivo
                        File(rutaPrin, filename).delete()

                        binding!!.ivImagen.setImage(ImageSource.uri("$fileImagesPiv/$filename")) //setImageURI(uri)
                        binding!!.tilContenidoPregResp.visibility = View.GONE
                        binding!!.ivImagen.visibility = View.VISIBLE
                        val cifrado = cifrar("$baseRutaImagen$fileImages/$filename", 3)
                        binding!!.etPregResp.setText(cifrado)

                        // contadorImagen += 1
                        // filename = "$contadorImagen.png"

                        activityCuestionarioViewModel.llamaCorruIncremento()
                    }
                    .setNegativeButton(
                        "Cancelar"
                    ) { dialog, _ ->
                        dialog.dismiss()
                        binding!!.imgvCancelar.visibility = View.GONE

                        binding!!.imgvQuitColor.visibility = View.VISIBLE
                        binding!!.imgvSelColor.visibility = View.VISIBLE
                    }.create().show()
            } else {
                Files.copy(
                    Paths.get("$rutaPrin/$filename"),
                    Paths.get("$fileImagesPiv/$filename"),
                    StandardCopyOption.REPLACE_EXISTING
                )

                // Borrar archivo
                File(rutaPrin, filename).delete()

                binding!!.ivImagen.setImage(ImageSource.uri("$fileImagesPiv/$filename")) //setImageURI(uri)
                binding!!.tilContenidoPregResp.visibility = View.GONE
                binding!!.ivImagen.visibility = View.VISIBLE

                val cifrado = cifrar("$baseRutaImagen$fileImages/$filename", 3)
                binding!!.etPregResp.setText(cifrado)

                // contadorImagen += 1
                // filename = "$contadorImagen.png"

                activityCuestionarioViewModel.llamaCorruIncremento()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
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
        if (!binding!!.tilContenidoPregResp.isGone) {
            var flipAnimator =
                ObjectAnimator.ofFloat(binding!!.flContenidoPregResp, "rotationY", 0f, 180f)
            flipAnimator.duration = 0 // Duración de la animación en milisegundos
            flipAnimator.start()
            flipAnimator.doOnEnd {
                flipAnimator =
                    ObjectAnimator.ofFloat(binding!!.flContenidoPregResp, "rotationY", 180f, 0f)
                flipAnimator.duration = 1000 // Duración de la animación en milisegundos
                flipAnimator.start()
            }
        } else {
            var flipAnimator =
                ObjectAnimator.ofFloat(
                    binding!!.flContenidoPregResp,
                    "rotationY",
                    0f,
                    180f
                ) // ivImagen
            flipAnimator.duration = 0 // Duración de la animación en milisegundos
            flipAnimator.start()
            flipAnimator.doOnEnd {
                flipAnimator =
                    ObjectAnimator.ofFloat(binding!!.flContenidoPregResp, "rotationY", 180f, 0f)
                flipAnimator.duration = 1000 // Duración de la animación en milisegundos
                flipAnimator.start()
            }
        }
    }

    // Toma permisos de persistencia para la URI
    private fun takePersistableUriPermission(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                //Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
    }

    // Método que se ejecuta cuando el back del telefono es presionado.
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            cancelarArchivo()
            deleteImages()
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
                if (file.exists()) {
                    File(file, "$nombreArchivo.xml").delete()
                    Log.d("ArchivoEliminado", "Archivo eliminado")
                } else {
                    Log.d("ArchivoEliminado", "Archivo no eliminado")
                }
                finish()
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

            val rutaGuiaCreada = File("$rutaPrin/$nombreArchivo.xml")
            if (rutaGuiaCreada.exists()) {
                Toast.makeText(
                    this, "Guia de estudio creada exitosamente",
                    Toast.LENGTH_SHORT
                ).show()
                Log.i("Creación: ", "Guia de estudio creada exitosamente")

                Files.copy(
                    Paths.get("$rutaPrin/$nombreArchivo.xml"),
                    Paths.get("$file/$nombreArchivo.xml"),
                    StandardCopyOption.REPLACE_EXISTING
                )

                // Borrar archivo
                File(rutaPrin, "$nombreArchivo.xml").delete()
                val ruta = "$file/$nombreArchivo.xml"
                val intent: Intent = Intent(applicationContext, Activity_RepasarGuia::class.java)
                intent.putExtra("ruta", ruta)

                startActivity(intent)
                activityCuestionarioViewModel.getAllUpdatedGuides(file)
                copyImages()
                finish()
            } else {
                Toast.makeText(
                    this, "Guia de estudio no se creó correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                Log.i("Creación: ", "Guia de estudio no se creó correctamente")
            }

            /*val ruta = "$file/$nombreArchivo.xml"
            val intent: Intent = Intent(applicationContext, Activity_RepasarGuia::class.java)
            intent.putExtra("ruta", ruta)

            startActivity(intent)
            activityCuestionarioViewModel.getAllUpdatedGuides(file)
            finish()*/
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun copyImages() {
        val images = fileImagesPiv.listFiles()
        // Hacemos un ciclo por cada fichero para extraer el nombre de cada uno.
        if (!images.isNullOrEmpty()) {
            for (i in images.indices) {
                // Sacamos del array files el primer fichero.
                val archivo: File = images[i]
                var name = ""

                name = archivo.name

                Files.copy(
                    Paths.get("$fileImagesPiv/$name"),
                    Paths.get("$fileImages/$name"),
                    StandardCopyOption.REPLACE_EXISTING
                )

                // Borrar archivo
                File(fileImagesPiv, name).delete()
            }
        }
    }

    private fun colocarEtiquetas(colorSpans: Array<ForegroundColorSpan>, editable: Editable) {
        for (colorSpan: ForegroundColorSpan in colorSpans) {
            val start: Int = editable.getSpanStart(colorSpan)
            val end: Int = editable.getSpanEnd(colorSpan)
            val color: Int = colorSpan.foregroundColor

            // Agregar la etiqueta de inicio al texto
            if (color != -16777216) {
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
    }

    private fun pintarTexto(contadorPregunta: Int) {
        var contColorPreg: Int = 0
        var inicio: Int = 0
        var fin: Int = 0
        var colorPregModel: ColorPregModel? = null
        var texto: String = ""
        if (binding!!.lblPregResp.text.toString() == "Pregunta") {
            texto = preguntas[contadorPregunta]
            // uri = texto.toUri()
        } else {
            texto = respuestas[contadorPregunta]
            // uri = texto.toUri()
        }

        if (texto.contains(baseRutaImagenCifrado)) {
            val descifrado = cifrar(texto, 26 - 3)
            binding!!.etPregResp.setText(texto)
            texto = descifrado.replace(baseRutaImagen.toRegex(), "")
            texto = texto.replace("imagenes".toRegex(), "imagenesPivote")
            // uri = texto.toUri()
            binding!!.ivImagen.setImage(ImageSource.uri(texto)) //setImageURI(uri)
            binding!!.tilContenidoPregResp.visibility = View.GONE
            binding!!.ivImagen.visibility = View.VISIBLE

            binding!!.imgvCancelar.visibility = View.VISIBLE
            binding!!.imgvQuitColor.visibility = View.GONE
            binding!!.imgvSelColor.visibility = View.GONE
        } else {
            binding!!.tilContenidoPregResp.visibility = View.VISIBLE
            binding!!.ivImagen.visibility = View.GONE

            binding!!.imgvCancelar.visibility = View.GONE
            binding!!.imgvQuitColor.visibility = View.VISIBLE
            binding!!.imgvSelColor.visibility = View.VISIBLE

            while (texto.contains("«")) {
                inicio = texto.indexOf("«") + 1
                fin = texto.indexOf("»")
                val color: String = texto.substring(inicio, fin)
                val longColor: Int = color.length
                val colEntero: Int = color.toInt()
                inicio = fin + 1
                fin = texto.indexOf("«", inicio)
                colorPregModel =
                    ColorPregModel((inicio - longColor - 2), (fin - longColor - 2), colEntero)

                if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                    preguntasColor.add(contColorPreg, colorPregModel)
                } else {
                    respuestasColor.add(contColorPreg, colorPregModel)
                }

                // Eliminar la primera etiqueta y su contenido
                texto = texto.replaceFirst("«.*?»".toRegex(), "")

                // Eliminar la segunda etiqueta y su contenido
                texto = texto.replaceFirst("«.*?»".toRegex(), "")
                contColorPreg++
            }


            builder = SpannableStringBuilder(texto)
            for (coloresPreguntas: ColorPregModel in if (binding!!.lblPregResp.text.toString() == "Pregunta") preguntasColor else respuestasColor) {
                val colorSpan: ForegroundColorSpan = ForegroundColorSpan(coloresPreguntas.color)
                builder!!.setSpan(
                    colorSpan,
                    coloresPreguntas.inicioColor,
                    coloresPreguntas.finColor,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            binding!!.etPregResp.text = builder
        }

        preguntasColor.clear()
        respuestasColor.clear()

        // Bandera ingresada para que no haga cambios de color cuando se detecte un cambio en ET.
        // pregResBandera = true
        // pregResBandera = false
    }

    private fun pintarLetra(texto: Editable?) {
        texto?.let {
            if (it.isNotEmpty() && !pregResBandera) {
                val cursorPosition = binding!!.etPregResp.selectionStart

                val lastCharIndex = cursorPosition - 1

                if (colorActual != colorPintarPalabra) {
                    if (colorPintarPalabra == 0) {
                        activityCuestionarioViewModel.setColorAnterior(colorActual)
                    } /*else {
                        activityCuestionarioViewModel.setColorAnterior(colorPintarPalabra)
                    }*/
                }

                it.setSpan(
                    ForegroundColorSpan(colorActual),
                    lastCharIndex,
                    lastCharIndex + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                binding!!.etPregResp.setSelection(lastCharIndex + 1)
                pregResBandera = false
            }
        }
    }

    fun setColor(@ColorInt color: Int?) {
        if (color == null) {
            ImageViewCompat.setImageTintList(binding!!.imgvSelColor, null)
            return
        }
        ImageViewCompat.setImageTintMode(binding!!.imgvSelColor, PorterDuff.Mode.SRC_ATOP)
        ImageViewCompat.setImageTintList(binding!!.imgvSelColor, ColorStateList.valueOf(color))
        colorActual = color

        if (posColorInicial != -1) {
            activityCuestionarioViewModel.setColorAnterior(colorPintarPalabra)
        }
    }

    fun cifrar(texto: String, desplazamiento: Int): String {
        val resultado = StringBuilder()

        for (caracter in texto) {
            if (caracter.isLetter()) {
                val base = if (caracter.isUpperCase()) 'A' else 'a'
                val letraCifrada = ((caracter - base + desplazamiento) % 26 + base.code).toChar()
                resultado.append(letraCifrada)
            } else {
                resultado.append(caracter)
            }
        }

        return resultado.toString()
    }
}