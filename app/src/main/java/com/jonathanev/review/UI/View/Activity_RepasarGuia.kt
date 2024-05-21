package com.jonathanev.review.UI.View

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.core.net.toUri
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
    private var binding: ActivityRepasarGuiaBinding? = null
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
        setContentView(binding!!.root)

        // Sección de anuncios
        initLoadAds()

        binding!!.barraSuperiorRegreso.imgvSave.visibility = View.GONE
        //nombreArchivo = intent.extras!!.getString("nombre_archivo").toString()

        //val preferences = getSharedPreferences("MiPref", MODE_PRIVATE)
        //val editor = preferences.edit()
        //editor.putString("nombre_archivo", nombreArchivo)
        //editor.apply()

        ruta = intent.extras!!.getString("ruta").toString()
        initUI(ruta)

        repasarGuiaViewModel.guiaModel.observe(this) {
            //binding!!.barraSuperiorRegreso.tvTituloToolbar.text = "Guia: $it.nombreGuia"
            nombreArchivo = it.nombreGuia

            // Guardo el nombre del archivo enviado desde el popupFragmentListarGuias.
            if (it.nombreGuia.contains(".xml")) {
                nombreArchivo = nombreArchivo.replace(".xml".toRegex(), "")
            }

            binding!!.barraSuperiorRegreso.tvTituloToolbar.text = "Guia: ${it.nombreGuia}"
            nombreArchivo = "${it.nombreGuia}.xml"

            // Obtenemos los datos del XML y los guardamos en su respectivo ArrayList.
            obtenerDatosXML()

            // Pintamos el texto del contador actual.
            pintarTexto()
        }

        binding!!.barraSuperiorRegreso.imgvBack.setOnClickListener { onBackPressed() }

        binding!!.imgvPregResp.setOnClickListener {
            if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                binding!!.lblPregResp.text = "Respuesta"
            } else {
                binding!!.lblPregResp.text = "Pregunta"
            }

            girarCardView()
        }

        binding!!.imgvPrevious.setOnClickListener {
            //binding!!.btnMostrarRespuesta.text = "Mostrar respuesta"
            if (contadorPregunta == 0) {
                Toast.makeText(
                    applicationContext, "No tienes preguntas anteriores",
                    Toast.LENGTH_SHORT
                ).show()

                Log.i("Crear pregunta: ", "No tienes preguntas anteriores")
            } else {
                contadorPregunta--

                binding!!.lblPregResp.text = "Pregunta"
                binding!!.etPregResp.setText("")

                // Pintamos el valor anterior de colores.
                pintarTexto()
            }
        }

        binding!!.imgvNext.setOnClickListener {
            contadorPregunta++
            val preguntasTotales: Int = preguntas.size

            // Validamos que haya mas preguntas, si las hay entra al método sino al else.
            if ((contadorPregunta + 1) <= preguntasTotales) {
                // Pintamos el valor siguiente con colores.

                binding!!.lblPregResp.text = "Pregunta"
                binding!!.etPregResp.setText("")
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

                        binding!!.lblPregResp.text = "Pregunta"
                        binding!!.etPregResp.setText("")

                        // Mostramos el primer valor de la pregunta pintado.
                        pintarTexto()
                    }
                    .setNegativeButton(
                        "Cancelar"
                    ) { dialog, i -> dialog.dismiss() }.create().show()
            }
        }

        binding!!.imgvEdit.setOnClickListener {
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
            pintarTexto()
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
            pintarTexto()
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

    private fun showImageOrText() {
        val disappearAnimation = AlphaAnimation(1.0f, 0.0f)
        disappearAnimation.duration = 200

        val appearAnimation = AlphaAnimation(0.0f, 1.0f)
        appearAnimation.duration = 1000

        disappearAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                if (binding!!.ivImagen.isGone) {
                    binding!!.ivImagen.visibility = View.VISIBLE
                    binding!!.tilContenidoPregResp.visibility = View.GONE
                } else {
                    binding!!.ivImagen.visibility = View.GONE
                    binding!!.tilContenidoPregResp.visibility = View.VISIBLE
                }
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })

        binding!!.ivImagen.startAnimation(disappearAnimation)
        //binding!!.tilContenidoPregResp.startAnimation(appearAnimation)
    }

    private fun pintarTexto() {
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
        } else {
            binding!!.tilContenidoPregResp.visibility = View.VISIBLE
            binding!!.ivImagen.visibility = View.GONE
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
        binding!!.etPregResp.text = builder
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
}