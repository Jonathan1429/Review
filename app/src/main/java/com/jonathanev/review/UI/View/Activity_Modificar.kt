package com.jonathanev.review.UI.View

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.widget.ImageViewCompat
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.jonathanev.review.Core.Constants.file
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
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

@AndroidEntryPoint
class Activity_Modificar : AppCompatActivity() {
    private var binding: ActivityModificarBinding? = null
    private lateinit var nombreArchivo: String
    private var colorActual: Int = 0
    private val preguntas: ArrayList<String> = ArrayList()
    private val respuestas: ArrayList<String> = ArrayList()
    private val preguntasColor: ArrayList<ColorPregModel> = ArrayList()
    private val respuestasColor: ArrayList<ColorPregModel> = ArrayList()
    var builder: SpannableStringBuilder? = null
    private var contadorPregunta: Int = 0
    private var dialMasPreg: Boolean = false
    private var uri: Uri? = null
    private var longCaracteres = 0
    private var pregResBandera = false // Bandera para cuando se le de click atras o delante.
    private var ruta: String = ""

    // Seleccionar imagen
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                // Toma permisos de persistencia para la URI
                takePersistableUriPermission(uri)

                binding!!.ivImagen.setImage(ImageSource.uri(uri)) //setImageURI(uri)
                binding!!.tilContenidoPregResp.visibility = View.GONE

                binding!!.ivImagen.visibility = View.VISIBLE
                binding!!.etPregResp.setText(uri.toString())
            } else {
                binding!!.imgvCancelar.visibility = View.GONE

                binding!!.imgvQuitColor.visibility = View.VISIBLE
                binding!!.imgvSelColor.visibility = View.VISIBLE
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
        setContentView(binding!!.root)

        // Sección de anuncios
        initLoadAds()

        ruta = intent.extras!!.getString("ruta").toString()
        initUI(ruta)

        modificarViewModel.guiaModel.observe(this) {
            nombreArchivo = it.nombreGuia
            // Guardo el nombre del archivo enviado desde el popupFragmentListarGuias.
            if (nombreArchivo.contains(".xml")) {
                nombreArchivo = nombreArchivo!!.replace(".xml".toRegex(), "")
            }

            binding!!.barraSuperiorRegreso.tvTituloToolbar.text = "Modificando: $nombreArchivo"
            colorActual = Color.BLACK

            // Aquí simplemente nos aseguramos que tenga el xml, si lo tiene no entramos.
            // En teoria ya todos los archivos no tienen el .xml porque lo recupero del ListarGuias
            if (!nombreArchivo.contains(".xml")) {
                nombreArchivo = "$nombreArchivo.xml"
            }

            // Obtenemos los datos del XML y los guardamos en su respectivo ArrayList.
            obtenerDatosXML()

            // Pintamos el texto del contador actual.
            pintarTexto(contadorPregunta)
        }

        binding!!.barraSuperiorRegreso.imgvBack.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                Toast.makeText(
                    applicationContext,
                    "No se hicieron cambios en el archivo",
                    Toast.LENGTH_SHORT
                ).show()

                Log.i("Modificar archivo: ", "No se hicieron cambios en el archivo")
                onBackPressed()
            }
        })

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


                if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                    if ((contadorPregunta + 1) > respuestas.size) {
                        binding!!.lblPregResp.text = "Respuesta"
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
                    }
                    girarCardView()
                } else {
                    if ((contadorPregunta + 1) > respuestas.size) {
                        binding!!.lblPregResp.text = "Pregunta"
                        respuestas.add(contadorPregunta, editable.toString())
                        pintarTexto(contadorPregunta)
                    } else {
                        binding!!.lblPregResp.text = "Pregunta"
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

                Log.i("Crear pregunta: ", "Asegurate de no dejar ningun campo vacio")
            }
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
                        binding!!.tilContenidoPregResp.visibility = View.VISIBLE
                        binding!!.ivImagen.visibility = View.GONE
                        binding!!.imgvCancelar.visibility = View.GONE
                        binding!!.imgvQuitColor.visibility = View.VISIBLE
                        binding!!.imgvSelColor.visibility = View.VISIBLE
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
                                binding!!.lblPregResp.text = "Pregunta"
                                binding!!.etPregResp.setText("")
                                binding!!.tilContenidoPregResp.visibility = View.VISIBLE
                                binding!!.ivImagen.visibility = View.GONE
                                binding!!.imgvCancelar.visibility = View.GONE
                                binding!!.imgvQuitColor.visibility = View.VISIBLE
                                binding!!.imgvSelColor.visibility = View.VISIBLE
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
                            ) { dialog, i ->
                                dialog.dismiss()
                                contadorPregunta--
                            }.setOnCancelListener {
                                contadorPregunta--
                            }.create().show()
                    } else {
                        // Si le das click a que si, ya no te preguntará nuevamente
                        // aunque te regreses a componer otras preg.
                        // Si el contadorPregunta es igual entonces solo escribiremos los campos vacios.
                        binding!!.lblPregResp.text = "Pregunta"
                        binding!!.tilContenidoPregResp.visibility = View.VISIBLE
                        binding!!.ivImagen.visibility = View.GONE

                        binding!!.imgvCancelar.visibility = View.GONE
                        binding!!.imgvQuitColor.visibility = View.VISIBLE
                        binding!!.imgvSelColor.visibility = View.VISIBLE

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

                    binding!!.imgvCancelar.visibility = View.GONE
                    binding!!.imgvQuitColor.visibility = View.VISIBLE
                    binding!!.imgvSelColor.visibility = View.VISIBLE

                    binding!!.lblPregResp.text = "Pregunta"
                    binding!!.etPregResp.setText("")

                }
                contadorPregunta++
            }
        }

        binding!!.imgvEliminar.setOnClickListener {
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
                        binding!!.imgvSelColor.visibility = View.GONE
                        binding!!.ivImagen.visibility = View.GONE
                        binding!!.tilContenidoPregResp.visibility = View.VISIBLE
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
                }.setNegativeButton("Cancelar") { dialog, i ->
                    dialog.dismiss()
                }.create().show()
        }

        binding!!.barraSuperiorRegreso.imgvSave.setOnClickListener {
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
                } else if ((contadorPregunta + 1) > longi && binding!!.lblPregResp.text.toString() == "Pregunta") {
                    borrarCrearXML(nombreArchivo)
                    binding!!.ivImagen.visibility = View.GONE
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Asegurate de llenar una pregunta y una respuesta",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.i("Crear pregunta: ", "Asegurate de llenar una pregunta y una respuesta")
                }
            } else {
                if (binding!!.lblPregResp.text.toString() == "Pregunta") {
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

                        borrarCrearXML(nombreArchivo)
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

                            borrarCrearXML(nombreArchivo)
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

                            borrarCrearXML(nombreArchivo)
                        }
                    }
                }
            }
        }

        // Visualización del DialogFragment de selección de colores.
        binding!!.imgvSelColor.setOnClickListener {
            // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
            val dialogo: Fragment_DialogColoresMod_popup = Fragment_DialogColoresMod_popup()
            //=====================================================================================================================
            dialogo.show(supportFragmentManager, "FragmentColor")
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

        // Eliminar textos con colores
        binding!!.imgvQuitColor.setOnClickListener {
            val text = binding!!.etPregResp.text
            val spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder(text)
            spannableStringBuilder.clearSpans()

            binding!!.etPregResp.text = spannableStringBuilder
        }

        binding!!.imgvImage.setOnClickListener {
            binding!!.imgvSelColor.visibility = View.GONE
            binding!!.imgvQuitColor.visibility = View.GONE

            binding!!.imgvCancelar.visibility = View.VISIBLE

            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding!!.etPregResp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                longCaracteres = binding!!.etPregResp.length()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(texto: Editable?) {
                val lv_lonCaracAct = binding!!.etPregResp.length()

                if (!texto.toString().contains("content://media/picker") && (lv_lonCaracAct-longCaracteres) == 1) {
                    pintarLetra(texto)
                }
            }
        })
    }

    private fun initUI(ruta: String) {
        modificarViewModel.getGuia(ruta)
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
                //showImageOrText()
                flipAnimator =
                    ObjectAnimator.ofFloat(binding!!.flContenidoPregResp, "rotationY", 180f, 0f)
                flipAnimator.duration = 1000 // Duración de la animación en milisegundos
                flipAnimator.start()
            }
        } else {
            var flipAnimator =
                ObjectAnimator.ofFloat(binding!!.flContenidoPregResp, "rotationY", 0f, 180f) // ivImagen
            flipAnimator.duration = 0 // Duración de la animación en milisegundos
            flipAnimator.start()
            flipAnimator.doOnEnd {
                //showImageOrText()
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
            Toast.makeText(
                applicationContext,
                "No se hicieron cambios en el archivo",
                Toast.LENGTH_SHORT
            ).show()

            Log.i("Crear archivo: ", "No se hicieron cambios en el archivo")
            onBackPressed()
            return true
        }
        return super.onKeyDown(keyCode, event)
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
            val intent: Intent = Intent(applicationContext, Activity_RepasarGuia::class.java)
            intent.putExtra("ruta", ruta)
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
            uri = texto.toUri()
        } else {
            texto = respuestas[contadorPregunta]
            uri = texto.toUri()
        }

        if (texto.contains("content://media/picker")) {
            binding!!.ivImagen.setImage(ImageSource.uri(uri!!)) //setImageURI(uri)
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
        }

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

        preguntasColor.clear()
        respuestasColor.clear()

        // Bandera ingresada para que no haga cambios de color cuando se detecte un cambio en ET.
        pregResBandera = true
        binding!!.etPregResp.text = builder
        pregResBandera = false
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
                val cursorPosition = binding!!.etPregResp.selectionStart

                val currentLength = texto.length
                // if (currentLength > longCaracteres) {
                    val lastCharIndex = cursorPosition - 1

                    it.setSpan(
                        ForegroundColorSpan(colorActual),
                        lastCharIndex,
                        lastCharIndex + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    binding!!.etPregResp.setSelection(lastCharIndex + 1)
                    pregResBandera = false
                // }
            }
        }
    }

    // Cambiar color del icono (ImageView)
    fun setColor(@ColorInt color: Int?) {
        if (color == null) {
            ImageViewCompat.setImageTintList(binding!!.imgvSelColor, null)
            return
        }
        ImageViewCompat.setImageTintMode(binding!!.imgvSelColor, PorterDuff.Mode.SRC_ATOP)
        ImageViewCompat.setImageTintList(binding!!.imgvSelColor, ColorStateList.valueOf(color))
        colorActual = color
    }
}