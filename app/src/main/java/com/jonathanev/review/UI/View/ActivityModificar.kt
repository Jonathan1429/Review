package com.jonathanev.review.UI.View

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.jonathanev.review.Core.Constants.BASERUTA_IMG
import com.jonathanev.review.Core.Constants.BASERUTA_IMG_CIFRADO
import com.jonathanev.review.Data.Model.MessageActions
import com.jonathanev.review.Data.Model.SpanPalabraModel
import com.jonathanev.review.Data.Model.prueba.ColorRange
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.UI.Utils.paintLetter
import com.jonathanev.review.UI.View.Fragments.FragmentDialogColoresModPopup
import com.jonathanev.review.UI.ViewModel.ActivityModificarViewModel
import com.jonathanev.review.databinding.ActivityModificarBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class ActivityModificar : AppCompatActivity() {
    private lateinit var binding: ActivityModificarBinding
    private var colorActual: Int = 0
    private var inicioColor: Int = 0
    private var contadorImagen = 0
    private var imagenPiv = 0
    private var longCaracteres = 0
    private var noImage: String = ""
    private val viewModel: ActivityModificarViewModel by viewModels()

    @Inject
    lateinit var filePathsProvider: FilePathsProvider

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModificarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Sección de anuncios
        initLoadAds()
        initUI()
        initListeners()

        viewModel.contImagenes.observe(this@ActivityModificar) { contImagen ->
            contadorImagen = contImagen
            if (imagenPiv == 0) {
                imagenPiv = contadorImagen
            }

            noImage = "$contadorImagen.png"
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    if (!uiState.internalRules.isThereMoreAsks || !uiState.internalRules.isUpdatedAskAns) {
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

                    binding.imgvCancelar.visibility =
                        if (uiState.internalRules.isShowCancelar) View.VISIBLE else View.GONE
                    binding.imgvQuitColor.visibility =
                        if (uiState.internalRules.isShowQuitColor) View.VISIBLE else View.GONE
                    binding.imgvSelColor.visibility =
                        if (uiState.internalRules.isShowSelColor) View.VISIBLE else View.GONE
                }
            }
        }

        lifecycleScope.launch {
            // 1. Especifica el estado (STARTED)
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 2. Mueve la recolección del Flow aquí
                viewModel.navigateToNext.collect { qaUiItem ->
                    val intent = Intent(this@ActivityModificar, ActivityRepasarGuia::class.java)
                    // Asegúrate de que tu clase qaUiItem es Serializable/Parcelable
                    intent.putExtra("qa_data", qaUiItem)
                    startActivity(intent)
                }
            }
        }

        viewModel.guiaModel.observe(this) {
            viewModel.getObtenerDatosXML()
            binding.barraSuperiorRegreso.tvTituloToolbar.text = "Guia: ${it.nombreGuia}"
        }

        viewModel.textoImagenCorrutina.observe(this) { texto ->
            binding.etPregResp.apply {
                setText("")
                post {
                    setText(texto)
                }
            }
        }
    }

    private fun saveCurrentQuestion(): SpanPalabraModel {
        val editable = binding.etPregResp.text

        val colorSpans: Array<ForegroundColorSpan> =
            editable!!.getSpans(0, editable.length, ForegroundColorSpan::class.java)
        val oldEditable = editable
        val sortedSpans = colorSpans.sortedBy { editable.getSpanStart(it) }

        var start = -1
        var end = 0
        var endAnterior = 0
        var isColNuevo = false
        var colorAnterior = 0
        var colorNuevo = 0
        var isDoubleColors = false
        var toCleaningColors = false

        for (colorSpan: ForegroundColorSpan in sortedSpans) {
            if (start == -1) {
                start = editable.getSpanStart(colorSpan)
            }

            if (end > editable.getSpanStart(colorSpan)) {
                isDoubleColors = true
                toCleaningColors = true
            }

            end = editable.getSpanEnd(colorSpan)
            colorNuevo = colorSpan.foregroundColor

            if (colorAnterior != colorNuevo) {
                if (colorAnterior == 0) {
                    isColNuevo = false
                    colorAnterior = colorNuevo
                    endAnterior = end - 1
                } else {
                    isColNuevo = true
                }
            }

            //if ((end - endAnterior) != 1) {
            // Limpiar colores encimados
            if (toCleaningColors) {
                // Obtener los spans dentro del rango especificado
                val spansToRemove = oldEditable.getSpans(
                    start,
                    endAnterior,
                    ForegroundColorSpan::class.java
                )

                for (span in spansToRemove) {
                    if (span.foregroundColor == colorAnterior) {
                        editable.removeSpan(span)
                    }
                }

                start = editable.getSpanStart(colorSpan)
                endAnterior = end

                colorAnterior = colorNuevo
                isColNuevo = false
                toCleaningColors = false
            } else if (isColNuevo || (end - endAnterior) != 1) {
                // Obtener los spans dentro del rango especificado
                val spansToRemove = oldEditable.getSpans(
                    start,
                    endAnterior,
                    ForegroundColorSpan::class.java
                )

                for (span in spansToRemove) {
                    if (span.foregroundColor == colorAnterior) {
                        editable.removeSpan(span)
                    }
                }

                editable.setSpan(
                    ForegroundColorSpan(colorAnterior),
                    start,
                    endAnterior,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                start = editable.getSpanStart(colorSpan)
                endAnterior = end

                colorAnterior = colorNuevo
                isColNuevo = false
                // toCleaningColors = false
            } else {
                endAnterior = end
            }
        }

        // Remove old spans
        if (colorSpans.isNotEmpty()) {
            val spansToRemove =
                oldEditable.getSpans(start, endAnterior, ForegroundColorSpan::class.java)

            for (span in spansToRemove) {
                if (span.foregroundColor == colorAnterior) {
                    editable.removeSpan(span)
                }
            }

            editable.setSpan(
                ForegroundColorSpan(colorAnterior),
                start,
                endAnterior,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val listSpans = binding.etPregResp.text!!.getSpans(
            0, editable.length, ForegroundColorSpan::class.java
        ).map { span ->
            ColorRange(
                start = editable.getSpanStart(span),
                end = editable.getSpanEnd(span),
                color = span.foregroundColor
            )
        }

        val resColocarEtiquetas = viewModel.setColocarEtiquetas(editable.toString(), listSpans)
        viewModel.updateQuestion(resColocarEtiquetas, listSpans)

        return if (isDoubleColors) {
            SpanPalabraModel(
                message = "Sobreescribiste colores y mantuvimos los últimos seleccionados",
                isDoubleColors = true
            )
        } else {
            SpanPalabraModel()
        }
    }

    private fun initListeners() {
        binding.barraSuperiorRegreso.imgvBack.setOnClickListener {
            cancelarArchivo()
        }

        binding.imgvPregResp.setOnClickListener {
            val text = binding.etPregResp.text.toString()
            val response = viewModel.onClickRoll(text)

            when (response) {
                MessageActions.FieldEmpty -> {
                    Toast.makeText(
                        /* context = */ applicationContext,
                        /* text = */ "Asegurate de llenar los campos correspondientes",
                        /* duration = */ Toast.LENGTH_SHORT
                    ).show()
                }

                MessageActions.Continue -> {
                    viewModel.swapTypeContent()
                    val saveCurrentQuestion = saveCurrentQuestion()

                    if (saveCurrentQuestion.isDoubleColors) {
                        Toast.makeText(this, saveCurrentQuestion.message, Toast.LENGTH_LONG).show()
                    }

                    val currentQuestion = viewModel.getTypeContent()
                    viewModel.cargarPregunta(currentQuestion)
                }

                MessageActions.AddMoreQuestions -> {
                    Log.w("ViewAction", "No debería entrar en AddMoreQuestions")
                }

                MessageActions.WithoutQuestionsBefore -> /* Puedes regresar solo este valor Unit*/ {
                    Log.w("ViewAction", "No debería entrar en WithoutQuestionsBefore")
                }
            }
        }

        binding.imgvPrevious.setOnClickListener {
            val text = binding.etPregResp.text.toString()
            val response = viewModel.onClickBefore(text)

            when (response) {
                MessageActions.FieldEmpty -> {
                    Toast.makeText(
                        /* context = */ applicationContext,
                        /* text = */ "Asegurate de llenar los campos correspondientes",
                        /* duration = */ Toast.LENGTH_SHORT
                    ).show()
                }

                MessageActions.Continue -> {
                    val saveCurrentQuestion = saveCurrentQuestion()

                    if (saveCurrentQuestion.isDoubleColors) {
                        Toast.makeText(this, saveCurrentQuestion.message, Toast.LENGTH_LONG).show()
                    }


                    viewModel.setMinusCountQuestion()
                    viewModel.setTypeContent(TypeContent.QUESTION)
                    val currentQuestion = viewModel.getTypeContent()
                    viewModel.cargarPregunta(currentQuestion)
                }

                MessageActions.AddMoreQuestions -> {
                    Log.w("ViewAction", "No debería entrar en AddMoreQuestions")
                }

                MessageActions.WithoutQuestionsBefore -> /* Puedes regresar solo este valor Unit*/ {
                    Toast.makeText(
                        /* context = */ applicationContext,
                        /* text = */ "Asegurate de llenar los campos correspondientes",
                        /* duration = */ Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.imgvNext.setOnClickListener {
            val text = binding.etPregResp.text.toString()
            val response = viewModel.onClickNext(text)

            when (response) {
                MessageActions.FieldEmpty -> {
                    Toast.makeText(
                        /* context = */ applicationContext,
                        /* text = */ "Asegurate de llenar los campos correspondientes",
                        /* duration = */ Toast.LENGTH_SHORT
                    ).show()
                }

                MessageActions.Continue -> {
                    val saveCurrentQuestion = saveCurrentQuestion()

                    if (saveCurrentQuestion.isDoubleColors) {
                        Toast.makeText(this, saveCurrentQuestion.message, Toast.LENGTH_LONG).show()
                    }
                    viewModel.setPlusCountQuestion()
                    viewModel.setTypeContent(TypeContent.QUESTION)
                    val currentQuestion = viewModel.getTypeContent()
                    viewModel.cargarPregunta(currentQuestion)
                }

                MessageActions.AddMoreQuestions -> {
                    AlertDialog.Builder(this@ActivityModificar)
                        .setTitle("¡Atención!")
                        .setMessage("Se acabaron las preguntas, ¿Quieres agregar más?")
                        .setPositiveButton(
                            "Si"
                        ) { _, _ ->
                            val saveCurrentQuestion = saveCurrentQuestion()

                            if (saveCurrentQuestion.isDoubleColors) {
                                Toast.makeText(this, saveCurrentQuestion.message, Toast.LENGTH_LONG)
                                    .show()
                            }
                            viewModel.setPlusCountQuestion()
                            viewModel.toggleShowMessageMoreQuestions()
                            viewModel.setTypeContent(TypeContent.QUESTION)
                            val currentQuestion = viewModel.getTypeContent()
                            viewModel.cargarPregunta(currentQuestion)

                            Toast.makeText(
                                applicationContext, "Ya puedes agregar " +
                                        "mas preguntas", Toast.LENGTH_LONG
                            ).show()
                        }
                        .setNegativeButton(
                            "Cancelar"
                        ) { dialog, _ ->
                            dialog.dismiss()
                        }.setOnCancelListener {

                        }.create().show()
                }

                MessageActions.WithoutQuestionsBefore -> /* Puedes regresar solo este valor Unit*/ {
                    Log.w("ViewAction", "No debería entrar en WithoutQuestionsBefore")
                }
            }
        }

        binding.imgvEliminar.setOnClickListener {
            val response = viewModel.onClickEliminar()

            when (response) {
                MessageActions.FieldEmpty -> {
                    Log.w("ViewAction", "No debería entrar en FieldEmpty")
                }

                MessageActions.Continue -> {
                    AlertDialog.Builder(this@ActivityModificar)
                        .setTitle("¡Atención!")
                        .setMessage("Se eliminará pregunta y respuestas, ¿Quieres continuar?")
                        .setPositiveButton("Si") { _, _ ->
                            viewModel.deleteCurrentQuestion()
                            viewModel.setMinusCountQuestion()
                            viewModel.setTypeContent(TypeContent.QUESTION)
                            val currentQuestion = viewModel.getTypeContent()
                            viewModel.cargarPregunta(currentQuestion)
                        }.setNegativeButton("Cancelar") { dialog, _ ->
                            dialog.dismiss()
                        }.create().show()
                }

                MessageActions.AddMoreQuestions -> {
                    Log.w("ViewAction", "No debería entrar en AddMoreQuestions")
                }

                MessageActions.WithoutQuestionsBefore -> /* Puedes regresar solo este valor Unit*/ {
                    Log.w("ViewAction", "No debería entrar en WithoutQuestionsBefore")
                }
            }
        }

        binding.barraSuperiorRegreso.imgvSave.setOnClickListener {
            val text = binding.etPregResp.text.toString()
            val response = viewModel.onClickNext(text)

            when (response) {
                MessageActions.AddMoreQuestions -> {
                    Log.w("ViewAction", "No debería entrar en AddMoreQuestions")
                }

                MessageActions.Continue -> {
                    val saveCurrentQuestion = saveCurrentQuestion()

                    if (saveCurrentQuestion.isDoubleColors) {
                        Toast.makeText(this, saveCurrentQuestion.message, Toast.LENGTH_LONG).show()
                    }

                    viewModel.setCrearXML()
                }

                MessageActions.FieldEmpty -> {
                    Toast.makeText(
                        /* context = */ applicationContext,
                        /* text = */ "Asegurate de llenar los campos correspondientes",
                        /* duration = */ Toast.LENGTH_SHORT
                    ).show()
                }

                MessageActions.WithoutQuestionsBefore -> {
                    Log.w("ViewAction", "No debería entrar en WithoutQuestionsBefore")
                }
            }

            /*val editable: Editable =
                Editable.Factory.getInstance().newEditable(binding.etPregResp.text)
            var isEtPregunta = false
            if (binding.lblPregResp.text.toString() == "Pregunta") {
                isEtPregunta = true
            }
            val didTheGuideAlreadyExist = true

            viewModel.onClickImgvSave(
                editable,
                nombreArchivo,
                isEtPregunta,
                didTheGuideAlreadyExist,
                viewModel.currentPath.value
            )*/
        }

        // Visualización del DialogFragment de selección de colores.
        binding.imgvSelColor.setOnClickListener {
            // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
            val dialogo: FragmentDialogColoresModPopup = FragmentDialogColoresModPopup()
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

            override fun afterTextChanged(texto: Editable) {
                if (!texto.toString()
                        .contains(BASERUTA_IMG_CIFRADO) && (binding.etPregResp.length() - longCaracteres) == 1
                ) {
                    // Si hay un salto de linea o es color negro no se pinta nada
                    if (colorActual != -16777216 && !seAgregoSaltoDeLinea) {

                        val cursorPosition = binding.etPregResp.selectionStart
                        Log.d("CursorPosition", cursorPosition.toString()) // Verifica el valor

                        texto.paintLetter(cursorPosition, colorActual)
                        //setPintarLetra(texto, cursorPosition, colorActual)
                        binding.etPregResp.setSelection(cursorPosition)
                        binding.etPregResp.invalidate()

                        val metrics = resources.displayMetrics
                        Log.d(
                            "ScreenInfo",
                            "Width: ${metrics.widthPixels}, Height: ${metrics.heightPixels}, Density: ${metrics.density}"
                        )
                    }
                }
            }
        })
    }

    // Save question and paint the next one
    /*private fun shouldContinueFlow() {
        val currentQuestion = viewModel.getTypeContent()
        viewModel.cargarPregunta(currentQuestion)
    }*/

    private fun initUI() {
        viewModel.getCountImage()
        viewModel.getGuia()
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
            if (bitmap != null) {
                fos = openFileOutput(noImage, MODE_PRIVATE)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)

                var ruta: String = filePathsProvider.fileGuides.toString()
                ruta = ruta.replace("guias".toRegex(), "imagenes")
                val originPath = filePathsProvider.buildFile(filePathsProvider.rutaPrin, noImage)
                val copiedPath =
                    filePathsProvider.buildFile(filePathsProvider.fileImagesPiv, noImage)

                val encoded = viewModel.getUrlImagenCifrada(originPath.toString(), 3)
                viewModel.setContent(QuestionContent.Image(originPath.toString(), encoded))
                val isAlertImageDialog = viewModel.shouldWarnImageReplace()
                var processCreatFile = false

                if (isAlertImageDialog) {
                    alertImageDialog { processCreatFile = it }
                } else {
                    processCreatFile = true
                }

                if (processCreatFile) {
                    viewModel.setCreatePivImage(
                        originPath = originPath,
                        copiedPath = copiedPath,
                        noImage = noImage
                    )

                    binding.ivImagen.setImage(ImageSource.uri(copiedPath.toString()))
                    binding.tilContenidoPregResp.visibility = View.GONE
                    binding.ivImagen.visibility = View.VISIBLE

                    val cifrado = viewModel.getUrlImagenCifrada(
                        "$BASERUTA_IMG$ruta/$noImage",
                        3
                    )

                    binding.etPregResp.setText(cifrado)
                    viewModel.llamaCorruIncremento(cifrado)
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

    private fun alertImageDialog(onAddImgToPiv: (Boolean) -> Unit) {
        AlertDialog.Builder(this@ActivityModificar)
            .setTitle("¡Atención!")
            .setMessage("Se borrará el contenido para agregar la imagen, ¿Quieres continuar?")
            .setCancelable(false)
            .setPositiveButton(
                "Si"
            ) { _, _ ->
                onAddImgToPiv(true)
                /*Files.copy(
                    Paths.get(originPath.toString()),
                    Paths.get(copiedPath.toString()),
                    StandardCopyOption.REPLACE_EXISTING
                )

                // Borrar archivo
                File(filePathsProvider.rutaPrin, noImage).delete()

                binding.ivImagen.setImage(
                    ImageSource.uri(
                        filePathsProvider.buildFile(
                            filePathsProvider.fileImagesPiv,
                            noImage
                        ).toString()
                    )
                ) //setImageURI(uri)
                val cifrado = viewModel.getUrlImagenCifrada(
                    urlImagen = "$BASERUTA_IMG$ruta/$noImage",
                    noCifrado = 3
                )
                //"$baseRutaImagen$fileImages/$filename",
                binding.etPregResp.setText(cifrado)

                binding.tilContenidoPregResp.visibility = View.GONE
                binding.ivImagen.visibility = View.VISIBLE*/
            }
            .setNegativeButton(
                "Cancelar"
            ) { dialog, _ ->
                dialog.dismiss()
                onAddImgToPiv(false)
                binding.imgvCancelar.visibility = View.GONE

                binding.imgvQuitColor.visibility = View.VISIBLE
                binding.imgvSelColor.visibility = View.VISIBLE
            }.create().show()
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

    // Método que se ejecuta cuando el back del telefono es presionado.
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            viewModel.deleteContentInPiv()
            cancelarArchivo()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // Mostrará un mensaje diciendo que el archivo se eliminará ya que no se terminó de crear.
    private fun cancelarArchivo() {
        // Se ejecuta cuando se regresa sin guardar.
        AlertDialog.Builder(this@ActivityModificar)
            .setTitle("¡Atención!")
            .setMessage(
                "Aún no terminas de modificar, no se guardará nada, " +
                        "¿seguro deseas continuar?"
            )
            .setPositiveButton(
                "Continuar"
            ) { _, _ -> // Si el archivo se creó y existe, se elimina y te informa en consola
                viewModel.deleteContentInPiv()
                finish()
            }
            .setNegativeButton(
                "Cancelar"
            ) { dialog, _ -> dialog.dismiss() }.create().show()
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
        inicioColor = binding.etPregResp.selectionStart
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