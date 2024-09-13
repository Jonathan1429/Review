package com.jonathanev.review.UI.View

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.view.isGone
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.jonathanev.review.Core.Constants.file
import com.jonathanev.review.Data.Model.ColorPregModel
import com.jonathanev.review.UI.ViewModel.RepasarGuiaViewModel
import com.jonathanev.review.databinding.ActivityRepasarGuiaBinding
import dagger.hilt.android.AndroidEntryPoint
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException


@AndroidEntryPoint
class Activity_RepasarGuia : AppCompatActivity() {
    private lateinit var binding: ActivityRepasarGuiaBinding
    private var nombreArchivo: String = ""
    private val preguntas: ArrayList<String> = ArrayList()
    private val respuestas: ArrayList<String> = ArrayList()
    private val preguntasColor: ArrayList<ColorPregModel> = ArrayList()
    private val respuestasColor: ArrayList<ColorPregModel> = ArrayList()
    private var contadorPregunta: Int = 0
    private var builder: SpannableStringBuilder? = null
    private var uri: Uri? = null
    private val repasarGuiaViewModel: RepasarGuiaViewModel by viewModels()
    private var ruta: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepasarGuiaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sección de anuncios
        initLoadAds()

        binding.barraSuperiorRegreso.imgvSave.visibility = View.GONE
        ruta = intent.extras!!.getString("ruta").toString()
        initUI(ruta)

        repasarGuiaViewModel.guiaModel.observe(this) {
            nombreArchivo = it.nombreGuia

            // Guardo el nombre del archivo enviado desde el popupFragmentListarGuias.
            if (it.nombreGuia.contains(".xml")) {
                nombreArchivo = nombreArchivo.replace(".xml".toRegex(), "")
            }

            binding.barraSuperiorRegreso.tvTituloToolbar.text = "Guia: ${it.nombreGuia}"
            nombreArchivo = "${it.nombreGuia}.xml"

            // Obtenemos los datos del XML y los guardamos en su respectivo ArrayList.
            val texto = repasarGuiaViewModel.getObtenerDatosXML(nombreArchivo, ruta)

            // Agregar el texto en el et cuando hay un builder
            if (!texto.estadoUI.isShowImage) {
                binding.etPregResp.text = texto.builder
            } else {
                // Cuando hay una imagen hay que poner esto
                binding.etPregResp.setText(texto.estadoImagen.textImgEcrypted)
                binding.ivImagen.setImage(ImageSource.uri(texto.estadoImagen.textImgUnencrypted))
            }
        }

        binding.barraSuperiorRegreso.imgvBack.setOnClickListener { finish() }

        binding.imgvPregResp.setOnClickListener {
            if (binding.lblPregResp.text.toString() == "Pregunta") {
                binding.lblPregResp.text = "Respuesta"
            } else {
                binding.lblPregResp.text = "Pregunta"
            }

            girarCardView()
        }

        binding.imgvPrevious.setOnClickListener {
            repasarGuiaViewModel.onClickBefore()
        }

        repasarGuiaViewModel.uiStateBtnBack.observe(this) { uiState ->
            if (uiState.estadoUI.isUpdatedAskAns) {
                binding.lblPregResp.text = "Pregunta"

                // Agregar el texto en el et cuando hay un builder
                if (!uiState.estadoUI.isShowImage) {
                    binding.etPregResp.text = uiState.builder
                } else {
                    // Cuando hay una imagen hay que poner esto
                    binding.etPregResp.setText(uiState.estadoImagen.textImgEcrypted)
                    binding.ivImagen.setImage(ImageSource.uri(uiState.estadoImagen.textImgUnencrypted))
                }

                binding.tilContenidoPregResp.visibility =
                    if (uiState.estadoUI.isShowImage) View.GONE else View.VISIBLE
                binding.ivImagen.visibility =
                    if (uiState.estadoUI.isShowImage) View.VISIBLE else View.GONE
            } else {
                Toast.makeText(applicationContext, uiState.message, Toast.LENGTH_SHORT).show()
            }

            /*if (contadorPregunta == 0) {
                Toast.makeText(
                    applicationContext, "No tienes preguntas anteriores",
                    Toast.LENGTH_SHORT
                ).show()

                Log.i("Crear pregunta: ", "No tienes preguntas anteriores")
            } else {
                contadorPregunta--

                binding.lblPregResp.text = "Pregunta"
                binding.etPregResp.setText("")

                // Pintamos el valor anterior de colores.
                pintarTexto()
            }*/
        }

        binding.imgvNext.setOnClickListener {
            contadorPregunta++
            val preguntasTotales: Int = preguntas.size

            // Validamos que haya mas preguntas, si las hay entra al método sino al else.
            if ((contadorPregunta + 1) <= preguntasTotales) {
                // Pintamos el valor siguiente con colores.

                binding.lblPregResp.text = "Pregunta"
                binding.etPregResp.setText("")
                pintarTexto()
            } else {
                contadorPregunta--
                // noHayMasPreguntas = true;
                // Se ejecuta cuando se regresa sin guardar.
                AlertDialog.Builder(this@Activity_RepasarGuia)
                    .setTitle("¡Atención!")
                    .setMessage("Se acabaron las preguntas, ¿Quieres repetir la guia?")
                    .setPositiveButton(
                        "Si"
                    ) { dialogInterface, i ->
                        contadorPregunta = 0

                        binding.lblPregResp.text = "Pregunta"
                        binding.etPregResp.setText("")

                        // Mostramos el primer valor de la pregunta pintado.
                        pintarTexto()
                    }
                    .setNegativeButton(
                        "Cancelar"
                    ) { dialog, i -> dialog.dismiss() }.create().show()
            }
        }

        binding.imgvEdit.setOnClickListener {
            // Recuperar el valor de SharedPreferences
            // val preferences = getSharedPreferences("MiPref", MODE_PRIVATE)
            // val nombreArchivo = preferences.getString("nombre_archivo", "")

            val intent: Intent = Intent(applicationContext, Activity_Modificar::class.java)
            intent.putExtra("ruta", ruta)
            startActivity(intent)
            finish()
        }
    }

    private fun initUI(ruta: String) {
        repasarGuiaViewModel.getGuia(ruta)
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
        var isEtPregunta = false
        if (binding.lblPregResp.text.toString() == "Pregunta") {
            isEtPregunta = true
        }

        if (!binding.tilContenidoPregResp.isGone) {
            // Get text
            val texto = repasarGuiaViewModel.getPintarTexto(isEtPregunta)
            if (texto.estadoUI.isEtPregunta) {
                binding.lblPregResp.text = "Respuesta"
            } else {
                binding.lblPregResp.text = "Pregunta"
            }

            if (texto.estadoUI.isClearText) {
                binding.etPregResp.text?.clear()
            } else {
                // Agregar el texto en el et cuando hay un builder
                if (!texto.estadoUI.isShowImage) {
                    binding.etPregResp.text = texto.builder
                } else {
                    // Cuando hay una imagen hay que poner esto
                    binding.etPregResp.setText(texto.estadoImagen.textImgEcrypted)
                    binding.ivImagen.setImage(ImageSource.uri(texto.estadoImagen.textImgUnencrypted))
                }
            }

            // Flip animator
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
            // Get text
            val texto = repasarGuiaViewModel.getPintarTexto(isEtPregunta)
            if (texto.estadoUI.isEtPregunta) {
                binding.lblPregResp.text = "Respuesta"
            } else {
                binding.lblPregResp.text = "Pregunta"
            }

            if (texto.estadoUI.isClearText) {
                binding.etPregResp.text?.clear()
            } else {
                // Agregar el texto en el et cuando hay un builder
                if (!texto.estadoUI.isShowImage) {
                    binding.etPregResp.text = texto.builder
                } else {
                    // Cuando hay una imagen hay que poner esto
                    binding.etPregResp.setText(texto.estadoImagen.textImgEcrypted)
                    binding.ivImagen.setImage(ImageSource.uri(texto.estadoImagen.textImgUnencrypted))
                }
            }

            // Flip animator
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

    private fun showImageOrText() {
        val disappearAnimation = AlphaAnimation(1.0f, 0.0f)
        disappearAnimation.duration = 200

        val appearAnimation = AlphaAnimation(0.0f, 1.0f)
        appearAnimation.duration = 1000

        disappearAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                if (binding.ivImagen.isGone) {
                    binding.ivImagen.visibility = View.VISIBLE
                    binding.tilContenidoPregResp.visibility = View.GONE
                } else {
                    binding.ivImagen.visibility = View.GONE
                    binding.tilContenidoPregResp.visibility = View.VISIBLE
                }
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })

        binding.ivImagen.startAnimation(disappearAnimation)
        //binding.tilContenidoPregResp.startAnimation(appearAnimation)
    }

    private fun pintarTexto() {
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

        if (texto.contains("frqwhqw://phgld/slfnhu/")) {
            val descifrado = cifrar(texto, 26 - 3)
            texto = descifrado.replace("content://media/picker/".toRegex(), "")
            // texto = texto.replace("content://media/picker/".toRegex(), "")
            // uri = texto.toUri()
            // binding.ivImagen.setImage(ImageSource.uri("${Constants.fileImages}/4.png")) //setImageURI(uri)
            // setImage puede ser con Uri o con texto, hay que probar en este caso
            val imagen = texto.substringAfterLast("/")
            var uri = "$file/$imagen"
            uri = uri.replace("guias", "imagenes")
            binding.ivImagen.setImage(ImageSource.uri(uri)) //setImageURI(uri)
            binding.tilContenidoPregResp.visibility = View.GONE
            binding.ivImagen.visibility = View.VISIBLE
        } else {
            binding.tilContenidoPregResp.visibility = View.VISIBLE
            binding.ivImagen.visibility = View.GONE

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
        }

        preguntasColor.clear()
        respuestasColor.clear()
        binding.etPregResp.text = builder
    }

    private fun obtenerDatosXML() {
        var doc: Document? = null
        val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder
        try {
            db = dbf.newDocumentBuilder()
            var filePath: File
            if (ruta == "null" || ruta.isEmpty()) {
                filePath = File(file, nombreArchivo)
            } else {
                filePath = File(ruta)
            }

            val fis = FileInputStream(filePath)

            doc = db.parse(fis)

            // Buscamos los Nodos Interrogante y accedemos a lo que se encuentre dentro.
            val cuestionario: NodeList = doc.getElementsByTagName("Interrogante")
            for (i in 0 until cuestionario.length) {
                // Accedes a los elementos de dicho nodo
                val e: Element = cuestionario.item(i) as Element
                preguntas.add(i, e.getAttribute("pregunta"))
                respuestas.add(i, e.getAttribute("respuesta"))
            }
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun cifrar(texto: String, desplazamiento: Int): String {
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