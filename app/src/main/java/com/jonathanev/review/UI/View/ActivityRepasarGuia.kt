package com.jonathanev.review.UI.View

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.jonathanev.review.Core.Constants.PREGUNTA
import com.jonathanev.review.Core.Constants.RESPUESTA
import com.jonathanev.review.Data.Model.prueba.ColorRange
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.UI.ViewModel.ActivityRepasarGuiaViewModel
import com.jonathanev.review.databinding.ActivityRepasarGuiaBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ActivityRepasarGuia : AppCompatActivity() {
    private lateinit var binding: ActivityRepasarGuiaBinding

    //private var nombreArchivo: String = ""
    private val viewModel: ActivityRepasarGuiaViewModel by viewModels()
    //private var ruta: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepasarGuiaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sección de anuncios
        initLoadAds()

        binding.barraSuperiorRegreso.imgvSave.visibility = View.GONE
        initUI()
        initListeners()

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                if (!uiState.internalRules.isThereMoreAsks){
                    Toast.makeText(
                        applicationContext,
                        uiState.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    return@collect
                }

                when (uiState.content) {
                    is QuestionContent.Image -> {
                        binding.etPregResp.setText(uiState.content.encodedPath)
                        binding.ivImagen.setImage(ImageSource.uri(uiState.content.decodedPath))

                        binding.tilContenidoPregResp.isVisible = uiState.showImage
                    }

                    is QuestionContent.Text -> {
                        val builder = uiState.content.toSpannable(
                            uiState.content.text,
                            uiState.content.colorRanges
                        )
                        binding.etPregResp.text = builder

                        binding.ivImagen.isVisible = uiState.showTextInput
                    }

                    is QuestionContent.None -> Toast.makeText(
                        applicationContext,
                        "No fue posible cargar una guia",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                if (uiState.shouldFlip) {
                    girarCardView()
                }
            }
        }

        viewModel.guiaModel.observe(this) {
            viewModel.getObtenerDatosXML()
            binding.barraSuperiorRegreso.tvTituloToolbar.text = "Guia: ${it.nombreGuia}"
        }
    }

    private fun initListeners() {
        binding.imgvPregResp.setOnClickListener {
            viewModel.onClickRoll()
        }

        binding.imgvNext.setOnClickListener {
            viewModel.onClickNext()
        }

        binding.imgvPrevious.setOnClickListener {
            viewModel.onClickBefore()
        }

        binding.barraSuperiorRegreso.imgvBack.setOnClickListener { finish() }

        binding.imgvEdit.setOnClickListener {
            // Recuperar el valor de SharedPreferences
            // val preferences = getSharedPreferences("MiPref", MODE_PRIVATE)
            // val nombreArchivo = preferences.getString("nombre_archivo", "")

            val intent: Intent = Intent(applicationContext, ActivityModificar::class.java)
            intent.putExtra("ruta", viewModel.getCurrentPath())
            startActivity(intent)
            finish()
        }
    }

    private fun initUI() {
        viewModel.getGuia()
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

    private fun QuestionContent.Text.toSpannable(
        text: String,
        colorRanges: List<ColorRange>
    ): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)

        for (colorRange in colorRanges) {
            val colorSpan = ForegroundColorSpan(colorRange.color)
            builder.setSpan(
                colorSpan,
                colorRange.start,
                colorRange.end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return builder
    }
}