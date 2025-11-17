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
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
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
import com.jonathanev.review.Core.Constants.PICK_IMAGE_REQUEST
import com.jonathanev.review.Data.Model.MessageActions
import com.jonathanev.review.Data.Model.SpanPalabraModel
import com.jonathanev.review.Data.Model.prueba.ColorRange
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.UI.View.Fragments.FragmentDialogColoresPopup
import com.jonathanev.review.UI.ViewModel.ActivityCuestionarioViewModel
import com.jonathanev.review.databinding.ActivityCuestionarioBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject

@AndroidEntryPoint
class ActivityCuestionario : AppCompatActivity() {
    private lateinit var binding: ActivityCuestionarioBinding
    private lateinit var nombreArchivo: String
    private var colorActual: Int = 0
    private var contadorImagen = 0
    private var longCaracteres = 0
    private val viewModel by viewModels<ActivityCuestionarioViewModel>()
    private var filename: String = "" // Ruta/imagen.png
    //private val route = filePathsProvider.buildFile(filePathsProvider.fileGuides, nombreArchivo)
    //private var ruta: String = "$fileGuias"

    @Inject
    lateinit var filePathsProvider: FilePathsProvider

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuestionarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        initListeners()

        // Recibimos el nombre del archivo del popupFragment Nueva Guia.
        // ruta = "$ruta$nombreArchivo"

        // Se cambia el nombre del titulo del toolbar
        nombreArchivo = intent.extras!!.getString("nombre_archivo") ?: ""
        binding.barraSuperiorRegreso.tvTituloToolbar.text = "Creando: $nombreArchivo"
        //colorActual = Color.BLACK

        viewModel.contImagenes.observe(this) { contImagen ->
            contadorImagen = contImagen
            filename = "$contadorImagen.png"
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
                    val intent = Intent(this@ActivityCuestionario, ActivityRepasarGuia::class.java)
                    // Asegúrate de que tu clase qaUiItem es Serializable/Parcelable
                    intent.putExtra("qa_data", qaUiItem)
                    startActivity(intent)
                }
            }
        }
    }

    private fun initListeners() {
        binding.barraSuperiorRegreso.imgvBack.setOnClickListener {
            cancelarArchivo()
            viewModel.deleteContentInPiv(nombreArchivo)
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

                    val currentQuestion = viewModel.typeContent.value
                    viewModel.cargarPregunta(currentQuestion)
                }

                MessageActions.AddMoreQuestions -> {
                    Log.w("ViewAction", "No debería entrar en AddMoreQuestions")
                }

                MessageActions.WithoutQuestionsBefore -> /* Puedes regresar solo este valor Unit*/ {
                    Log.w("ViewAction", "No debería entrar en WithoutQuestionsBefore")
                }
            }

            /*val editable: Editable =
                Editable.Factory.getInstance().newEditable(binding.etPregResp.text)

            var isEtPregunta = false
            if (binding.lblPregResp.text.toString() == "Pregunta") {
                isEtPregunta = true
            }

            val route = filePathsProvider.buildFile(filePathsProvider.fileGuides, nombreArchivo).toString()
            viewModel.clickedRoll(
                editable,
                isEtPregunta,
                route
            )*/
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
                    val currentQuestion = viewModel.typeContent.value
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
                    val currentQuestion = viewModel.typeContent.value
                    viewModel.cargarPregunta(currentQuestion)
                }

                MessageActions.AddMoreQuestions -> {
                    AlertDialog.Builder(this@ActivityCuestionario)
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
                            val currentQuestion = viewModel.typeContent.value
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
                    AlertDialog.Builder(this@ActivityCuestionario)
                        .setTitle("¡Atención!")
                        .setMessage("Se eliminará pregunta y respuestas, ¿Quieres continuar?")
                        .setPositiveButton("Si") { _, _ ->
                            viewModel.deleteCurrentQuestion()
                            viewModel.setMinusCountQuestion()
                            viewModel.setTypeContent(TypeContent.QUESTION)
                            val currentQuestion = viewModel.typeContent.value
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
        }

        // Visualización del DialogFragment de selección de colores.
        binding.imgvSelColor.setOnClickListener {
            // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
            val dialogo: FragmentDialogColoresPopup = FragmentDialogColoresPopup()
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

            colorActual = Color.BLACK
            setColor(colorActual)
        }

        binding.imgvImage.setOnClickListener {
            binding.imgvSelColor.visibility = View.GONE
            binding.imgvQuitColor.visibility = View.GONE

            binding.imgvCancelar.visibility = View.VISIBLE

            // pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            openSomeActivityForResult()
        }

        binding.etPregResp.addTextChangedListener(object : TextWatcher {
            private var textoAnterior: String = ""
            private var seAgregoSaltoDeLinea = false

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Guarda el texto antes del cambio
                textoAnterior = s?.toString() ?: ""
                longCaracteres = binding.etPregResp.length()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Aquí puedes verificar si se ha añadido un salto de línea en este momento
                seAgregoSaltoDeLinea =
                    count > before && s?.subSequence(start, start + count)
                        ?.contains("\n") == true
            }

            override fun afterTextChanged(texto: Editable?) {
                if (!texto.toString()
                        .contains(BASERUTA_IMG_CIFRADO) && (binding.etPregResp.length() - longCaracteres) == 1
                ) {
                    // Si hay un salto de linea o es color negro no se pinta nada
                    if (colorActual != -16777216 && !seAgregoSaltoDeLinea) {
                        val cursorPosition = binding.etPregResp.selectionStart
                        viewModel.setPintarLetra(
                            texto!!,
                            cursorPosition,
                            colorActual
                        )
                    }
                }
            }
        })
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

    private fun initUI() {
        viewModel.getCountImage()
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

    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        var fos: FileOutputStream? = null
        try {
            fos = openFileOutput(filename, MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            val originPath = filePathsProvider.buildFile(filePathsProvider.rutaPrin, filename)
            val copiedPath = filePathsProvider.buildFile(filePathsProvider.fileImagesPiv, filename)

            if (binding.etPregResp.text!!.isNotEmpty() && !binding.etPregResp.text!!.contains(
                    BASERUTA_IMG
                )
            ) {
                AlertDialog.Builder(this@ActivityCuestionario)
                    .setTitle("¡Atención!")
                    .setMessage("Se borrará el contenido para agregar la imagen, ¿Quieres continuar?")
                    .setCancelable(false)
                    .setPositiveButton(
                        "Si"
                    ) { _, _ ->
                        Files.copy(
                            Paths.get(originPath.toString()),
                            Paths.get(copiedPath.toString()),
                            StandardCopyOption.REPLACE_EXISTING
                        )

                        // Borrar archivo
                        File(filePathsProvider.rutaPrin, filename).delete()

                        binding.ivImagen.setImage(
                            ImageSource.uri(
                                filePathsProvider.buildFile(
                                    filePathsProvider.fileImagesPiv,
                                    filename
                                ).toString()
                            )
                        )//setImageURI(uri)
                        binding.tilContenidoPregResp.visibility = View.GONE
                        binding.ivImagen.visibility = View.VISIBLE
                        val cifrado = viewModel.getUrlImagenCifrada(
                            filePathsProvider.buildFileFolder(
                                filePathsProvider.rutaPrinImgCifrado,
                                filePathsProvider.fileImages.toString(),
                                filename
                            ).toString(),
                            3
                        )
                        binding.etPregResp.setText(cifrado)

                        viewModel.llamaCorruIncremento(cifrado)
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
                    Paths.get(originPath.toString()),
                    Paths.get(copiedPath.toString()),
                    StandardCopyOption.REPLACE_EXISTING
                )

                // Borrar archivo
                filePathsProvider.buildFile(filePathsProvider.rutaPrin, filename).delete()

                binding.ivImagen.setImage(
                    ImageSource.uri(
                        filePathsProvider.buildFile(
                            filePathsProvider.fileImagesPiv,
                            filename
                        ).toString()
                    )
                ) //setImageURI(uri)
                binding.tilContenidoPregResp.visibility = View.GONE
                binding.ivImagen.visibility = View.VISIBLE

                val cifrado = viewModel.getUrlImagenCifrada(
                    filePathsProvider.buildFileFolder(
                        filePathsProvider.rutaPrinImg,
                        filename,
                        filename
                    ).toString(),
                    3
                )
                binding.etPregResp.setText(cifrado)

                viewModel.llamaCorruIncremento(cifrado)
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
            flipAnimator =
                ObjectAnimator.ofFloat(binding.flContenidoPregResp, "rotationY", 180f, 0f)
            flipAnimator.duration = 1000 // Duración de la animación en milisegundos
            flipAnimator.start()
        }
    }

    // Método que se ejecuta cuando el back del telefono es presionado.
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            cancelarArchivo()
            viewModel.deleteContentInPiv(nombreArchivo)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // Mostrará un mensaje diciendo que el archivo se eliminará ya que no se terminó de crear.
    private fun cancelarArchivo() {
        // Se ejecuta cuando se regresa sin guardar.
        AlertDialog.Builder(this@ActivityCuestionario)
            .setTitle("¡Atención!")
            .setMessage(
                "Aún no terminas de crear la guia, se borrará el " +
                        "archivo creado, ¿seguro deseas continuar?"
            )
            .setPositiveButton(
                "Continuar"
            ) { _, _ -> // Si el archivo se creó y existe, se elimina y te informa en consola
                viewModel.deleteContentInPiv(nombreArchivo)
                finish()
            }
            .setNegativeButton(
                "Cancelar"
            ) { dialog, _ -> dialog.dismiss() }.create().show()
    }

    private fun copyImages() {
        val images = filePathsProvider.fileImagesPiv.listFiles()
        // Hacemos un ciclo por cada fichero para extraer el nombre de cada uno.
        if (!images.isNullOrEmpty()) {
            for (i in images.indices) {
                // Sacamos del array files el primer fichero.
                val archivo: File = images[i]
                var name = ""

                name = archivo.name

                Files.copy(
                    Paths.get(
                        filePathsProvider.buildFile(filePathsProvider.fileImagesPiv, name)
                            .toString()
                    ),
                    Paths.get(
                        filePathsProvider.buildFile(filePathsProvider.fileImages, name).toString()
                    ),
                    StandardCopyOption.REPLACE_EXISTING
                )

                // Borrar archivo
                File(filePathsProvider.fileImagesPiv, name).delete()
            }
        }
    }

    fun setColor(@ColorInt color: Int?) {
        if (color == null) {
            ImageViewCompat.setImageTintList(binding.imgvSelColor, null)
            return
        }
        ImageViewCompat.setImageTintMode(binding.imgvSelColor, PorterDuff.Mode.SRC_ATOP)
        ImageViewCompat.setImageTintList(binding.imgvSelColor, ColorStateList.valueOf(color))
        colorActual = color
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
