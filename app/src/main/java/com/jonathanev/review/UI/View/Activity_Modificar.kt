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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.view.isGone
import androidx.core.widget.ImageViewCompat
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.jonathanev.review.Core.Constants.baseRutaImagen
import com.jonathanev.review.Core.Constants.baseRutaImagenCifrado
import com.jonathanev.review.Core.Constants.file
import com.jonathanev.review.Core.Constants.fileImagesPiv
import com.jonathanev.review.Core.Constants.rutaPrin
import com.jonathanev.review.Data.Model.ColorPregModel
import com.jonathanev.review.Fragments.Fragment_DialogColoresMod_popup
import com.jonathanev.review.UI.ViewModel.ModificarViewModel
import com.jonathanev.review.databinding.ActivityModificarBinding
import dagger.hilt.android.AndroidEntryPoint
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

@AndroidEntryPoint
class Activity_Modificar : AppCompatActivity() {
    private lateinit var binding: ActivityModificarBinding
    private lateinit var nombreArchivo: String
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
    private var imagenPiv = 0
    private var dialMasPreg: Boolean = false
    private var uri: Uri? = null
    private var longCaracteres = 0
    private var textoAnterior = ""
    private var pregResBandera = false // Bandera para cuando se le de click atras o delante.
    private var ruta: String = ""
    private var filename: String = ""

    // Seleccionar imagen
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                // Toma permisos de persistencia para la URI
                takePersistableUriPermission(uri)

                if (binding.etPregResp.text!!.isNotEmpty()) {
                    AlertDialog.Builder(this@Activity_Modificar)
                        .setTitle("¡Atención!")
                        .setMessage("Se borrará el texto para agregar la imagen, ¿Quieres continuar?")
                        .setPositiveButton(
                            "Si"
                        ) { _, _ ->
                            binding.ivImagen.setImage(ImageSource.uri(uri)) //setImageURI(uri)
                            binding.tilContenidoPregResp.visibility = View.GONE

                            binding.ivImagen.visibility = View.VISIBLE
                            binding.etPregResp.setText(uri.toString())
                        }
                        .setNegativeButton(
                            "Cancelar"
                        ) { dialog, _ ->
                            dialog.dismiss()
                        }.create().show()
                } else {
                    binding.ivImagen.setImage(ImageSource.uri(uri)) //setImageURI(uri)
                    binding.tilContenidoPregResp.visibility = View.GONE

                    binding.ivImagen.visibility = View.VISIBLE
                    binding.etPregResp.setText(uri.toString())
                }
            } else {
                binding.imgvCancelar.visibility = View.GONE

                binding.imgvQuitColor.visibility = View.VISIBLE
                binding.imgvSelColor.visibility = View.VISIBLE
            }
        }

    // Creamos la serialización y la clase para crear archivos de manera global.
    private var serializer: XmlSerializer = Xml.newSerializer()
    private var fos: FileOutputStream? = null
    private val modificarViewModel: ModificarViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModificarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sección de anuncios
        initLoadAds()

        initUI()
        ruta = intent.extras!!.getString("ruta").toString()
        binding.barraSuperiorRegreso.imgvBack.setOnClickListener {
            cancelarArchivo()
            deleteImages()
        }

        binding.imgvPregResp.setOnClickListener {
            val editable: Editable =
                Editable.Factory.getInstance().newEditable(binding.etPregResp.text)

            var isEtPregunta = false
            if (binding.lblPregResp.text.toString() == "Pregunta") {
                isEtPregunta = true
            }
            modificarViewModel.clickedRoll(
                editable,
                isEtPregunta
            )
        }

        modificarViewModel.uiStateBtnRoll.observe(this) { uiState ->
            if (uiState.estadoUI.isUpdatedAskAns) {
                girarCardView()
                if (uiState.estadoUI.isEtPregunta) {
                    binding.lblPregResp.text = "Respuesta"
                } else {
                    binding.lblPregResp.text = "Pregunta"
                }

                if (uiState.estadoUI.isClearText) {
                    binding.etPregResp.text?.clear()
                } else {
                    // Agregar el texto en el et cuando hay un builder
                    if (!uiState.estadoUI.isShowImage) {
                        binding.etPregResp.text = uiState.builder
                    } else {
                        // Cuando hay una imagen hay que poner esto
                        binding.etPregResp.setText(uiState.estadoImagen.textImgEcrypted)
                        binding.ivImagen.setImage(ImageSource.uri(uiState.estadoImagen.textImgUnencrypted))
                    }
                }

                binding.tilContenidoPregResp.visibility =
                    if (uiState.estadoUI.isShowImage) View.GONE else View.VISIBLE
                binding.ivImagen.visibility =
                    if (uiState.estadoUI.isShowImage) View.VISIBLE else View.GONE
                binding.imgvCancelar.visibility =
                    if (uiState.estadoUI.isShowCancelar) View.VISIBLE else View.GONE
                binding.imgvQuitColor.visibility =
                    if (uiState.estadoUI.isShowQuitColor) View.VISIBLE else View.GONE
                binding.imgvSelColor.visibility =
                    if (uiState.estadoUI.isShowSelColor) View.VISIBLE else View.GONE

                if (uiState.responseSpanPalabra?.isDoubleColors == true) {
                    Toast.makeText(
                        applicationContext,
                        uiState.responseSpanPalabra.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    uiState.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.imgvPrevious.setOnClickListener {
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
                    if (binding.etPregResp.text.toString().isEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "Asegurate de no dejar ningun campo vacio",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.i("Crear pregunta: ", "Asegurate de no dejar ningun campo vacio")

                        // Se resta uno al final y así se queda neutral.
                        contadorPregunta++
                    } else {
                        setSpanPalabra()

                        // Si los campos están bien se sobre escribe.
                        var editable: Editable =
                            Editable.Factory.getInstance().newEditable(binding.etPregResp.text)
                        var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                            0,
                            editable.length,
                            ForegroundColorSpan::class.java
                        )

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)

                        if (binding.lblPregResp.text.toString() == "Pregunta") {
                            preguntas[contadorPregunta] = editable.toString()
                        } else {
                            respuestas[contadorPregunta] = editable.toString()
                        }

                        binding.lblPregResp.text = "Pregunta"
                        // Pintamos el texto en la pregunta actual
                        pintarTexto(contadorPregunta - 1)
                    }
                } else {
                    if (binding.lblPregResp.text.toString() == "Pregunta" && binding!!.etPregResp.text.toString()
                            .isNotEmpty() ||
                        binding.lblPregResp.text.toString() == "Respuesta" && binding!!.etPregResp.text.toString()
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
                        setSpanPalabra()

                        if (binding.lblPregResp.text.toString() == "Respuesta") {
                            // Si los campos están bien se sobre escribe.
                            var editable: Editable =
                                Editable.Factory.getInstance()
                                    .newEditable(binding.etPregResp.text)
                            var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                                0,
                                editable.length,
                                ForegroundColorSpan::class.java
                            )

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable)

                            respuestas.add(contadorPregunta, editable.toString())
                            binding.lblPregResp.text = "Pregunta"
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

        binding.imgvNext.setOnClickListener {
            // Validamos campos vacios en la pregunta o respuesta.
            val longi: Int = respuestas.size - 1

            if ((contadorPregunta <= longi && binding.etPregResp.text.toString()
                    .isEmpty()) || (binding.lblPregResp.text == "Pregunta" && contadorPregunta > longi) || binding!!.etPregResp.text.toString()
                    .isEmpty()
            ) {
                Toast.makeText(
                    applicationContext,
                    "Asegurate de no dejar ningun campo vacio",
                    Toast.LENGTH_SHORT
                ).show()

                Log.i("Crear pregunta: ", "Asegurate de no dejar ningun campo vacio")
            } else {
                setSpanPalabra()

                // Se le quita 1 para hacer referencia al arreglo
                // tamaño 3-1 = 2 [0,1,2].
                //val longi: Int = preguntas.size - 1 HAY QUE VALIDAR SI AQUÍ TAMBIÉN FUNCIONA COMENTANDO ESTE

                if (contadorPregunta <= longi) {
                    var editable: Editable =
                        Editable.Factory.getInstance().newEditable(binding.etPregResp.text)
                    var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                        0,
                        editable.length,
                        ForegroundColorSpan::class.java
                    )

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)

                    if (binding.lblPregResp.text.toString() == "Pregunta") {
                        preguntas[contadorPregunta] = editable.toString()
                    } else {
                        respuestas[contadorPregunta] = editable.toString()
                    }

                    // Mientras el contadorPregunta sea menor escribiremos la siguiente pregunta
                    // en los et.
                    if (contadorPregunta < longi) {
                        // Pintamos el texto en la pregunta actual

                        binding.lblPregResp.text = "Pregunta"
                        binding.tilContenidoPregResp.visibility = View.VISIBLE
                        binding.ivImagen.visibility = View.GONE
                        binding.imgvCancelar.visibility = View.GONE
                        binding.imgvQuitColor.visibility = View.VISIBLE
                        binding.imgvSelColor.visibility = View.VISIBLE
                        pintarTexto(contadorPregunta + 1)
                    } else if (!dialMasPreg) {
                        // ¿Quieres agregar más preguntas?
                        AlertDialog.Builder(this@Activity_Modificar)
                            .setTitle("¡Atención!")
                            .setMessage("Se acabaron las preguntas, ¿Quieres agregar más?")
                            .setPositiveButton(
                                "Si"
                            ) { _, _ -> // Cambiaremos el texto del toolbar.
                                //binding.barraSuperiorRegreso.tvTituloToolbar.text =
                                //    "Agrega más preguntas a la guía"
                                binding.lblPregResp.text = "Pregunta"
                                binding.etPregResp.setText("")
                                binding.tilContenidoPregResp.visibility = View.VISIBLE
                                binding.ivImagen.visibility = View.GONE
                                binding.imgvCancelar.visibility = View.GONE
                                binding.imgvQuitColor.visibility = View.VISIBLE
                                binding.imgvSelColor.visibility = View.VISIBLE
                                dialMasPreg = true
                                Toast.makeText(
                                    applicationContext, "Ya puedes agregar " +
                                            "mas preguntas", Toast.LENGTH_LONG
                                ).show()

                                Log.i(
                                    "Crear pregunta: ", "Ya puedes agregar " +
                                            "mas preguntas"
                                )
                            }
                            .setNegativeButton(
                                "Cancelar"
                            ) { dialog, _ ->
                                dialog.dismiss()
                                contadorPregunta--
                            }.setOnCancelListener {
                                contadorPregunta--
                            }.create().show()
                    } else {
                        // Si le das click a que si, ya no te preguntará nuevamente
                        // aunque te regreses a componer otras preg.
                        // Si el contadorPregunta es igual entonces solo escribiremos los campos vacios.
                        binding.lblPregResp.text = "Pregunta"
                        binding.tilContenidoPregResp.visibility = View.VISIBLE
                        binding.ivImagen.visibility = View.GONE

                        binding.imgvCancelar.visibility = View.GONE
                        binding.imgvQuitColor.visibility = View.VISIBLE
                        binding.imgvSelColor.visibility = View.VISIBLE

                        binding.etPregResp.setText("")
                    }
                } else { // Si contadorPregunta es mayor a lo que hay en el arreglo.
                    binding.tilContenidoPregResp.visibility = View.VISIBLE
                    binding.ivImagen.visibility = View.GONE

                    var editable: Editable =
                        Editable.Factory.getInstance()
                            .newEditable(binding.etPregResp.text)
                    var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                        0,
                        editable.length,
                        ForegroundColorSpan::class.java
                    )

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)
                    respuestas.add(contadorPregunta, editable.toString())

                    binding.imgvCancelar.visibility = View.GONE
                    binding.imgvQuitColor.visibility = View.VISIBLE
                    binding.imgvSelColor.visibility = View.VISIBLE

                    binding.lblPregResp.text = "Pregunta"
                    binding.etPregResp.setText("")

                }
                contadorPregunta++
            }
        }

        binding.imgvEliminar.setOnClickListener {
            AlertDialog.Builder(this@Activity_Modificar)
                .setTitle("¡Atención!")
                .setMessage("¿Quieres eliminar la pregunta?")
                .setPositiveButton("Si") { _, _ ->
                    // Se le quita 1 para hacer referencia al arreglo
                    // tamaño 3-1 = 2 [0,1,2].
                    val longi: Int = respuestas.size

                    if (longi == 0) {
                        //preguntas.removeFirst()
                        //respuestas.removeFirst()

                        if (binding.lblPregResp.text.toString() == "Pregunta") {
                            binding.etPregResp.setText("")
                        } else {
                            binding.lblPregResp.text = "Pregunta"
                            binding.etPregResp.setText("")
                        }
                    } else if ((contadorPregunta + 1) == longi && (contadorPregunta + 1) == 1) {
                        preguntas.removeAt(contadorPregunta)
                        respuestas.removeAt(contadorPregunta)
                        binding.lblPregResp.text = "Pregunta"
                        binding.imgvSelColor.visibility = View.GONE
                        binding.ivImagen.visibility = View.GONE
                        binding.tilContenidoPregResp.visibility = View.VISIBLE
                        binding.etPregResp.setText("")
                    } else if ((contadorPregunta + 1) == longi) {
                        preguntas.removeAt(contadorPregunta)
                        respuestas.removeAt(contadorPregunta)
                        contadorPregunta--
                        binding.lblPregResp.text = "Pregunta"
                        pintarTexto(contadorPregunta)
                    } else if (contadorPregunta < longi) {
                        preguntas.removeAt(contadorPregunta)
                        respuestas.removeAt(contadorPregunta)
                        binding.lblPregResp.text = "Pregunta"
                        pintarTexto(contadorPregunta)
                    } else { // Cuando el contador es mayor a longi
                        if (binding.lblPregResp.text.toString() == "Pregunta") {
                            contadorPregunta--
                            pintarTexto(contadorPregunta)
                        } else {
                            preguntas.removeAt(contadorPregunta)
                            contadorPregunta--
                            pintarTexto(contadorPregunta)
                        }
                    }
                }.setNegativeButton("Cancelar") { dialog, i ->
                    dialog.dismiss()
                }.create().show()
        }

        binding.barraSuperiorRegreso.imgvSave.setOnClickListener {
            modificarViewModel.clickedSave()
        }

        // Visualización del DialogFragment de selección de colores.
        binding.imgvSelColor.setOnClickListener {
            // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
            val dialogo: Fragment_DialogColoresMod_popup = Fragment_DialogColoresMod_popup()
            //=====================================================================================================================
            dialogo.show(supportFragmentManager, "FragmentColor")
        }

        // Cambio de botones visibles
        binding.imgvCancelar.setOnClickListener {
            binding.imgvSelColor.visibility = View.VISIBLE
            binding.imgvQuitColor.visibility = View.VISIBLE
            binding.tilContenidoPregResp.visibility = View.VISIBLE

            binding.ivImagen.visibility = View.GONE
            binding.imgvCancelar.visibility = View.GONE

            binding.etPregResp.setText("")
        }

        // Eliminar textos con colores
        binding.imgvQuitColor.setOnClickListener {
            val text = binding.etPregResp.text
            val spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder(text)
            spannableStringBuilder.clearSpans()

            binding.etPregResp.text = spannableStringBuilder
        }

        binding.imgvImage.setOnClickListener {
            binding.imgvSelColor.visibility = View.GONE
            binding.imgvQuitColor.visibility = View.GONE

            binding.imgvCancelar.visibility = View.VISIBLE

            // pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            openSomeActivityForResult()
        }

        binding.etPregResp.addTextChangedListener(object : TextWatcher {
            private var textoAnterior: String = ""
            private var seAgregoSaltoDeLinea = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Guarda el texto antes del cambio
                textoAnterior = s?.toString() ?: ""
                longCaracteres = binding.etPregResp.length()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Aquí puedes verificar si se ha añadido un salto de línea en este momento
                seAgregoSaltoDeLinea =
                    count > before && s?.subSequence(start, start + count)?.contains("\n") == true
            }

            override fun afterTextChanged(texto: Editable?) {
                if (!texto.toString()
                        .contains(baseRutaImagenCifrado) && (binding.etPregResp.length() - longCaracteres) == 1
                ) {
                    // Si hay un salto de linea o es color negro no se pinta nada
                    if (colorActual != -16777216 && !seAgregoSaltoDeLinea) {
                        pintarLetra(texto)
                    }
                }
            }
        })

        modificarViewModel.saveClicked.observe(this) {
            if (it) {
                // Se le quita 1 para hacer referencia al arreglo
                // tamaño 3-1 = 2 [0,1,2].
                val longi: Int = respuestas.size

                if (binding.etPregResp.text.toString().isEmpty()) {
                    if (respuestas.isEmpty() && binding.lblPregResp.text.toString() == "Pregunta") {
                        Toast.makeText(
                            applicationContext,
                            "Debes tener como minimo una pregunta",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.i("Crear pregunta: ", "Debes tener como minimo una pregunta")

                        modificarViewModel.clickedSave()
                    } else if ((contadorPregunta + 1) > longi && binding.lblPregResp.text.toString() == "Pregunta") {
                        setSpanPalabra()
                        borrarCrearXML(nombreArchivo)
                        binding.ivImagen.visibility = View.GONE
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

                        modificarViewModel.clickedSave()
                    }
                } else {
                    if (binding.lblPregResp.text.toString() == "Pregunta") {
                        if ((contadorPregunta + 1) <= longi && longi > 0) {
                            setSpanPalabra()

                            var editable: Editable =
                                Editable.Factory.getInstance()
                                    .newEditable(binding.etPregResp.text)
                            var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                                0,
                                editable.length,
                                ForegroundColorSpan::class.java
                            )

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable)
                            preguntas[contadorPregunta] = editable.toString()

                            borrarCrearXML(nombreArchivo)
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

                            modificarViewModel.clickedSave()
                        }
                    } else {
                        if (longi == 0) {
                            setSpanPalabra()

                            var editable: Editable =
                                Editable.Factory.getInstance()
                                    .newEditable(binding.etPregResp.text)
                            var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                                0,
                                editable.length,
                                ForegroundColorSpan::class.java
                            )

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable)
                            respuestas.add(editable.toString())

                            borrarCrearXML(nombreArchivo)
                        } else {
                            if ((contadorPregunta + 1) <= longi) {
                                setSpanPalabra()

                                var editable: Editable =
                                    Editable.Factory.getInstance()
                                        .newEditable(binding.etPregResp.text)
                                var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                                    0,
                                    editable.length,
                                    ForegroundColorSpan::class.java
                                )

                                // Se colocan las etiquetas en cada palabra con color
                                colocarEtiquetas(colorSpans, editable)
                                respuestas[contadorPregunta] = editable.toString()

                                borrarCrearXML(nombreArchivo)
                            } else {
                                setSpanPalabra()

                                var editable: Editable =
                                    Editable.Factory.getInstance()
                                        .newEditable(binding.etPregResp.text)
                                var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                                    0,
                                    editable.length,
                                    ForegroundColorSpan::class.java
                                )

                                // Se colocan las etiquetas en cada palabra con color
                                colocarEtiquetas(colorSpans, editable)
                                respuestas.add(editable.toString())

                                borrarCrearXML(nombreArchivo)
                            }
                        }
                    }
                }
            }
        }

        modificarViewModel.colorAnterior.observe(this) {
            if (posColorInicial == -1) {
                colorPintarPalabra = it

                val cursorPosition = binding.etPregResp.selectionStart
                val lastCharIndex = cursorPosition - 1
                posColorInicial = lastCharIndex
            } else {
                /*val cursorPosition = binding.etPregResp.selectionStart
                posColorFinal = cursorPosition*/

                // Obtener los spans dentro del rango especificado
                val spansToRemove = binding.etPregResp.text!!.getSpans(
                    posColorInicial,
                    posColorFinal,
                    ForegroundColorSpan::class.java
                )

                for (span in spansToRemove) {
                    binding.etPregResp.text!!.removeSpan(span)
                }

                binding.etPregResp.text!!.setSpan(
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

        modificarViewModel.contImagenes.observe(this@Activity_Modificar) { contImagen ->
            contadorImagen = contImagen
            if (imagenPiv == 0) {
                imagenPiv = contadorImagen
            }

            filename = "$contadorImagen.png"

            if (preguntas.isEmpty()) {
                modificarViewModel.getGuia(ruta)
            }
        }

        modificarViewModel.guiaModel.observe(this) {
            nombreArchivo = it.nombreGuia
            // Guardo el nombre del archivo enviado desde el popupFragmentListarGuias.
            if (nombreArchivo.contains(".xml")) {
                nombreArchivo = nombreArchivo.replace(".xml".toRegex(), "")
            }

            binding.barraSuperiorRegreso.tvTituloToolbar.text = "Modificando: $nombreArchivo"
            colorActual = Color.BLACK

            // Aquí simplemente nos aseguramos que tenga el xml, si lo tiene no entramos.
            // En teoria ya todos los archivos no tienen el .xml porque lo recupero del ListarGuias
            if (!nombreArchivo.contains(".xml")) {
                nombreArchivo = "$nombreArchivo.xml"
            }

            // Obtenemos los datos del XML y los guardamos en su respectivo ArrayList.
            obtenerDatosXML()
            modificarViewModel.getObtenerDatosXML(nombreArchivo, ruta)
        }

        modificarViewModel.uiShowDates.observe(this){
            // Pintamos el texto del contador actual.
            pintarTexto(contadorPregunta)
        }
    }

    private fun setSpanPalabra() {
        var editable: Editable =
            Editable.Factory.getInstance().newEditable(binding.etPregResp.text)
        var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
            0,
            editable.length,
            ForegroundColorSpan::class.java
        )
        val sortedSpans = colorSpans.sortedBy { editable.getSpanStart(it) }

        var start = -1
        var end = 0
        var endAnterior = 0
        var isColNuevo = false
        var colorAnterior = 0
        var colorNuevo = 0
        var isDoubleColors = false

        for (colorSpan: ForegroundColorSpan in sortedSpans) {
            // Se ejecuta solo al inicio
            if (start == -1) {
                start = editable.getSpanStart(colorSpan)
            }

            // Verificación de superposición de colores
            if (end > editable.getSpanEnd(colorSpan)) {
                // Mensaje de suporposición de colores
                isDoubleColors = true
            }
            end = editable.getSpanEnd(colorSpan)
            colorNuevo = colorSpan.foregroundColor

            // Logica del color
            if (colorAnterior != colorNuevo) {
                if (colorAnterior == 0) {
                    isColNuevo = false
                    colorAnterior = colorNuevo
                    endAnterior = end - 1
                } else {
                    isColNuevo = true
                }
            }

            // Se agrupan los valores por colores (En un solo span se agrupa)
            if (isColNuevo || (end - endAnterior) != 1) {
                // Obtener los spans dentro del rango especificado
                val spansToRemove = binding.etPregResp.text!!.getSpans(
                    start,
                    endAnterior,
                    ForegroundColorSpan::class.java
                )

                for (span in spansToRemove) {
                    binding.etPregResp.text!!.removeSpan(span)
                }

                binding.etPregResp.text!!.setSpan(
                    ForegroundColorSpan(colorAnterior),
                    start,
                    endAnterior,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                start = editable.getSpanStart(colorSpan)
                endAnterior = end

                if (isColNuevo) {
                    colorAnterior = colorNuevo
                    isColNuevo = false
                }
            } else {
                endAnterior = end
            }
        }

        // El ultimo span de color se pinta aquí
        if (colorSpans.isNotEmpty()) {
            // Obtener los spans dentro del rango especificado
            val spansToRemove = binding.etPregResp.text!!.getSpans(
                start,
                endAnterior,
                ForegroundColorSpan::class.java
            )

            for (span in spansToRemove) {
                binding.etPregResp.text!!.removeSpan(span)
            }

            binding.etPregResp.text!!.setSpan(
                ForegroundColorSpan(colorAnterior),
                start,
                endAnterior,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        if (isDoubleColors) {
            Toast.makeText(
                applicationContext,
                "Sobreescribiste colores y mantuvimos los últimos seleccionados",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun initUI() {
        modificarViewModel.getCountImage()
    }

    private fun openSomeActivityForResult() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null && data.data != null) {
                    val selectedImageUri: Uri? = data.data
                    if (selectedImageUri != null) {
                        val bitmap =
                            MediaStore.Images.Media.getBitmap(
                                this.contentResolver,
                                selectedImageUri
                            )
                        saveImageToInternalStorage(bitmap)
                    }
                }
            } else {
                binding.imgvCancelar.visibility = View.GONE

                binding.imgvQuitColor.visibility = View.VISIBLE
                binding.imgvSelColor.visibility = View.VISIBLE
            }
        }

    private fun saveImageToInternalStorage(bitmap: Bitmap?) {
        var fos: FileOutputStream? = null
        try {
            // val f = File(fileImagesPiv, filename)
            if (bitmap != null) {
                fos = openFileOutput(filename, MODE_PRIVATE)
                // fos = FileOutputStream(f)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)

                var ruta: String = file.toString()
                ruta = ruta.replace("guias".toRegex(), "imagenes")

                if (binding.etPregResp.text!!.isNotEmpty()
                    && !binding.etPregResp.text!!.contains(baseRutaImagenCifrado)
                ) {
                    AlertDialog.Builder(this@Activity_Modificar)
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

                            binding.ivImagen.setImage(ImageSource.uri("$fileImagesPiv/$filename")) //setImageURI(uri)
                            val cifrado = cifrar("$baseRutaImagen$ruta/$filename", 3)
                            binding.etPregResp.setText(cifrado)
                            // contadorImagen += 1
                            // filename = "$contadorImagen.png"

                            binding.tilContenidoPregResp.visibility = View.GONE
                            binding.ivImagen.visibility = View.VISIBLE

                            modificarViewModel.llamaCorruIncremento()
                        }
                        .setNegativeButton(
                            "Cancelar"
                        ) { dialog, _ ->
                            dialog.dismiss()
                            binding.imgvCancelar.visibility = View.GONE

                            binding.imgvQuitColor.visibility = View.VISIBLE
                            binding.imgvSelColor.visibility = View.VISIBLE
                        }.create().show()
                } else {
                    Files.copy(
                        Paths.get("$rutaPrin/$filename"),
                        Paths.get("$fileImagesPiv/$filename"),
                        StandardCopyOption.REPLACE_EXISTING
                    )

                    // Borrar archivo
                    File(rutaPrin, filename).delete()

                    binding.ivImagen.setImage(ImageSource.uri("$fileImagesPiv/$filename")) //setImageURI(uri)
                    binding.tilContenidoPregResp.visibility = View.GONE
                    binding.ivImagen.visibility = View.VISIBLE

                    val cifrado = cifrar("$baseRutaImagen$ruta/$filename", 3)
                    binding.etPregResp.setText(cifrado)
                    val l = preguntas[contadorPregunta]
                    // contadorImagen += 1
                    // filename = "$contadorImagen.png"

                    modificarViewModel.llamaCorruIncremento()
                }
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
        binding.adView.loadAd(adRequest)

        binding.adView.adListener = object : AdListener() {
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
        if (!binding.tilContenidoPregResp.isGone) {
            var flipAnimator =
                ObjectAnimator.ofFloat(binding.flContenidoPregResp, "rotationY", 0f, 180f)
            flipAnimator.duration = 0 // Duración de la animación en milisegundos
            flipAnimator.start()
            flipAnimator.doOnEnd {
                //showImageOrText()
                flipAnimator =
                    ObjectAnimator.ofFloat(binding.flContenidoPregResp, "rotationY", 180f, 0f)
                flipAnimator.duration = 1000 // Duración de la animación en milisegundos
                flipAnimator.start()
            }
        } else {
            var flipAnimator =
                ObjectAnimator.ofFloat(
                    binding.flContenidoPregResp,
                    "rotationY",
                    0f,
                    180f
                ) // ivImagen
            flipAnimator.duration = 0 // Duración de la animación en milisegundos
            flipAnimator.start()
            flipAnimator.doOnEnd {
                //showImageOrText()
                flipAnimator =
                    ObjectAnimator.ofFloat(binding.flContenidoPregResp, "rotationY", 180f, 0f)
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
            deleteImages()
            cancelarArchivo()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // Mostrará un mensaje diciendo que el archivo se eliminará ya que no se terminó de crear.
    private fun cancelarArchivo() {
        // Se ejecuta cuando se regresa sin guardar.
        AlertDialog.Builder(this@Activity_Modificar)
            .setTitle("¡Atención!")
            .setMessage(
                "Aún no terminas de modificar, no se guardará nada, " +
                        "¿seguro deseas continuar?"
            )
            .setPositiveButton(
                "Continuar"
            ) { _, _ -> // Si el archivo se creó y existe, se elimina y te informa en consola
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
            ) { dialog, _ -> dialog.dismiss() }.create().show()
    }

    private fun borrarCrearXML(nombreArchivo: String?) {
        // Eliminamos el archivo anteriormente creado
        // val path = File(file, nombreArchivo.toString())

        File(ruta).delete()
        Log.d("ArchivoEliminado", "Archivo eliminado")
        //Vamos a crear el archivo que acabamos de eliminar pero con el nuevo cuestionario

        try {
            // fos = openFileOutput(nombreArchivo, MODE_PRIVATE)
            fos = FileOutputStream(ruta)
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
                serializer.attribute("", "pregunta", preguntas[i])
                serializer.attribute("", "respuesta", respuestas[i])
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

            Log.i("Crear archivo: ", "Guia de estudio modificada exitosamente")

            val ruta = "$file/$nombreArchivo"
            val intent: Intent = Intent(applicationContext, Activity_RepasarGuia::class.java)
            intent.putExtra("ruta", ruta)
            startActivity(intent)
            copyImages()
            finish()
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

                var rutaImagen = file.toString()
                rutaImagen = rutaImagen.replace("guias".toRegex(), "imagenes")
                val rutaImagPath = File(rutaImagen)
                Files.copy(
                    Paths.get("$fileImagesPiv/$name"),
                    Paths.get("$rutaImagPath/$name"),
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

            val etiqIni: String = "«$color»"
            val etiqFin: String = "«/$color»"

            // Agregar la etiqueta de inicio al texto
            editable.replace(start, start, etiqIni)

            // Agregar la etiqueta de cierre al texto
            editable.replace(end + etiqIni.length, end + etiqIni.length, etiqFin)
        }
    }

    private fun pintarTexto(contadorPregunta: Int) {
        var contColorPreg: Int = 0
        var inicio: Int = 0
        var fin: Int = 0
        var colorPregModel: ColorPregModel? = null
        var texto: String = ""
        if (binding.lblPregResp.text.toString() == "Pregunta") {
            texto = preguntas[contadorPregunta]
            // uri = texto.toUri()
        } else {
            texto = respuestas[contadorPregunta]
            // uri = texto.toUri()
        }

        if (texto.contains(baseRutaImagenCifrado)) {
            val descifrado = cifrar(texto, 26 - 3)
            binding.etPregResp.setText(texto)
            // texto = descifrado.replace("content://media/picker/".toRegex(), "")
            // texto = texto.replace("imagenes".toRegex(), "imagenesPivote")
            val imagen = descifrado.substringAfterLast("/")
            // file
            val imagenInt = imagen.replace(".png", "").toInt()
            if (imagenInt >= imagenPiv) {
                binding.ivImagen.setImage(ImageSource.uri("$fileImagesPiv/$imagen")) //setImageURI(uri)
            } else {
                var uri = "$file/$imagen"
                uri = uri.replace("guias", "imagenes")
                binding.ivImagen.setImage(ImageSource.uri(uri))
            }

            binding.tilContenidoPregResp.visibility = View.GONE
            binding.ivImagen.visibility = View.VISIBLE

            binding.imgvCancelar.visibility = View.VISIBLE
            binding.imgvQuitColor.visibility = View.GONE
            binding.imgvSelColor.visibility = View.GONE

        } else {
            binding.tilContenidoPregResp.visibility = View.VISIBLE
            binding.ivImagen.visibility = View.GONE

            binding.imgvCancelar.visibility = View.GONE
            binding.imgvQuitColor.visibility = View.VISIBLE
            binding.imgvSelColor.visibility = View.VISIBLE

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

                if (binding.lblPregResp.text.toString() == "Pregunta") {
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
            for (coloresPreguntas: ColorPregModel in if (binding.lblPregResp.text.toString() == "Pregunta") preguntasColor else respuestasColor) {
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

            // Bandera ingresada para que no haga cambios de color cuando se detecte un cambio en ET.
            pregResBandera = true
            binding.etPregResp.text = builder
            pregResBandera = false
        }
    }

    private fun obtenerDatosXML() {
        var doc: Document? = null
        val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder
        try {
            db = dbf.newDocumentBuilder()
            var filePath: File
            if (ruta == "null") {
                filePath = File(file, nombreArchivo)
            } else {
                filePath = File(ruta)
            }
            doc = db.parse(filePath)

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

    private fun pintarLetra(texto: Editable?) {
        texto?.let {
            if (it.isNotEmpty() && !pregResBandera) {
                val cursorPosition = binding.etPregResp.selectionStart
                val lastCharIndex = cursorPosition - 1
                posColorFinal = lastCharIndex + 1

                it.setSpan(
                    ForegroundColorSpan(colorActual),
                    lastCharIndex,
                    lastCharIndex + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                binding.etPregResp.setSelection(lastCharIndex + 1)
                pregResBandera = false
            }
        }
    }

    // Cambiar color del icono (ImageView)
    fun setColor(@ColorInt color: Int?) {
        if (color == null) {
            ImageViewCompat.setImageTintList(binding.imgvSelColor, null)
            return
        }
        ImageViewCompat.setImageTintMode(binding.imgvSelColor, PorterDuff.Mode.SRC_ATOP)
        ImageViewCompat.setImageTintList(binding.imgvSelColor, ColorStateList.valueOf(color))
        colorActual = color
    }

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