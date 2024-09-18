package com.jonathanev.review.UI.View

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.jonathanev.review.UI.ViewModel.RepasarGuiaViewModel
import com.jonathanev.review.databinding.ActivityRepasarGuiaBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class Activity_RepasarGuia : AppCompatActivity() {
    private lateinit var binding: ActivityRepasarGuiaBinding
    private var nombreArchivo: String = ""
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
        initListeners()
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

            binding.tilContenidoPregResp.visibility =
                if (texto.estadoUI.isShowImage) View.GONE else View.VISIBLE
            binding.ivImagen.visibility =
                if (texto.estadoUI.isShowImage) View.VISIBLE else View.GONE
        }

        repasarGuiaViewModel.uiStateBtnRoll.observe(this) { uiState ->
            if (uiState.estadoUI.isUpdatedAskAns) {
                girarCardView()

                if (binding.lblPregResp.text == "Respuesta"){
                    binding.lblPregResp.text = "Pregunta"
                } else {
                    binding.lblPregResp.text = "Respuesta"
                }

                if (uiState.estadoUI.isClearText) {
                    binding.etPregResp.text?.clear()
                } else {
                    // Agregar el texto en el et cuando hay un builder
                    if (!uiState.estadoUI.isShowImage) {
                        binding.etPregResp.text = uiState.builder
                    } else {
                        // Cuando hay una imagen hay que poner esto
                        // Se realizan estos cambios porque la ruta guardada en el et no es
                        // totalmente la correcta (solo la ruta se guarda hasta imagenes)
                        val imagen = uiState.estadoImagen.textImgUnencrypted.substringAfterLast("/")
                        val carpetaImagen = ruta.substringBeforeLast("/")
                        var ruta = "$carpetaImagen/$imagen"
                        ruta = ruta.replace("guias", "imagenes")
                        binding.etPregResp.setText(uiState.estadoImagen.textImgEcrypted)
                        binding.ivImagen.setImage(ImageSource.uri(ruta))
                    }
                }

                binding.tilContenidoPregResp.visibility =
                    if (uiState.estadoUI.isShowImage) View.GONE else View.VISIBLE
                binding.ivImagen.visibility =
                    if (uiState.estadoUI.isShowImage) View.VISIBLE else View.GONE
            } else {
                Toast.makeText(applicationContext, uiState.message, Toast.LENGTH_SHORT).show()
            }
        }

        repasarGuiaViewModel.uiStateBtnNext
            .observe(this) { uiState ->
                if (uiState.estadoUI.isUpdatedAskAns) {
                    binding.lblPregResp.text = "Pregunta"

                    // Agregar el texto en el et cuando hay un builder
                    if (!uiState.estadoUI.isShowImage) {
                        binding.etPregResp.text = uiState.builder
                    } else {
                        // Cuando hay una imagen hay que poner esto
                        // Se realizan estos cambios porque la ruta guardada en el et no es
                        // totalmente la correcta (solo la ruta se guarda hasta imagenes)
                        val imagen = uiState.estadoImagen.textImgUnencrypted.substringAfterLast("/")
                        val carpetaImagen = ruta.substringBeforeLast("/")
                        var ruta = "$carpetaImagen/$imagen"
                        ruta = ruta.replace("guias", "imagenes")
                        binding.etPregResp.setText(uiState.estadoImagen.textImgEcrypted)
                        binding.ivImagen.setImage(ImageSource.uri(ruta))
                    }

                    binding.tilContenidoPregResp.visibility =
                        if (uiState.estadoUI.isShowImage) View.GONE else View.VISIBLE
                    binding.ivImagen.visibility =
                        if (uiState.estadoUI.isShowImage) View.VISIBLE else View.GONE
                } else {
                    AlertDialog.Builder(this@Activity_RepasarGuia)
                        .setTitle("¡Atención!")
                        .setMessage(uiState.message)
                        .setPositiveButton(
                            "Si"
                        ) { _, _ ->
                            binding.lblPregResp.text = "Pregunta"
                            repasarGuiaViewModel.contadorPregunta = 0
                            val texto = repasarGuiaViewModel.getReinicioGuia(true)
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
                        }
                        .setNegativeButton(
                            "Cancelar"
                        ) { dialog, _ -> dialog.dismiss() }.create().show()
                }
            }

        repasarGuiaViewModel.uiStateBtnBack.observe(this) { uiState ->
            if (uiState.estadoUI.isUpdatedAskAns) {
                binding.lblPregResp.text = "Pregunta"

                // Agregar el texto en el et cuando hay un builder
                if (!uiState.estadoUI.isShowImage) {
                    binding.etPregResp.text = uiState.builder
                } else {
                    // Cuando hay una imagen hay que poner esto
                    // Se realizan estos cambios porque la ruta guardada en el et no es
                    // totalmente la correcta (solo la ruta se guarda hasta imagenes)
                    val imagen = uiState.estadoImagen.textImgUnencrypted.substringAfterLast("/")
                    val carpetaImagen = ruta.substringBeforeLast("/")
                    var ruta = "$carpetaImagen/$imagen"
                    ruta = ruta.replace("guias", "imagenes")
                    binding.etPregResp.setText(uiState.estadoImagen.textImgEcrypted)
                    binding.ivImagen.setImage(ImageSource.uri(ruta))
                }

                binding.tilContenidoPregResp.visibility =
                    if (uiState.estadoUI.isShowImage) View.GONE else View.VISIBLE
                binding.ivImagen.visibility =
                    if (uiState.estadoUI.isShowImage) View.VISIBLE else View.GONE
            } else {
                Toast.makeText(applicationContext, uiState.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initListeners() {
        binding.imgvPregResp.setOnClickListener {
            var isEtPregunta = true
            if (binding.lblPregResp.text.toString() == "Pregunta") {
                //Get Respuesta
                isEtPregunta = false
            }

            // Get text
            repasarGuiaViewModel.onClickRoll(isEtPregunta)
        }

        binding.imgvNext.setOnClickListener {
            repasarGuiaViewModel.onClickNext()
        }

        binding.imgvPrevious.setOnClickListener {
            repasarGuiaViewModel.onClickBefore()
        }

        binding.barraSuperiorRegreso.imgvBack.setOnClickListener { finish() }

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

    @SuppressLint("SetTextI18n")
    private fun girarCardView() {
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
    }
}