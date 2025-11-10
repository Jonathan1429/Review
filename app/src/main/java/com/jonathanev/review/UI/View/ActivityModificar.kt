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
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.widget.ImageViewCompat
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.jonathanev.review.Core.Constants.BASERUTA_IMG
import com.jonathanev.review.Core.Constants.BASERUTA_IMG_CIFRADO
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.UI.View.Fragments.FragmentDialogColoresModPopup
import com.jonathanev.review.UI.ViewModel.ActivityModificarViewModel
import com.jonathanev.review.databinding.ActivityModificarBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject

@AndroidEntryPoint
class ActivityModificar : AppCompatActivity() {
    private lateinit var binding: ActivityModificarBinding
    private lateinit var nombreArchivo: String
    private var colorActual: Int = 0
    private var inicioColor: Int = 0
    private var contadorImagen = 0
    private var imagenPiv = 0
    private var longCaracteres = 0
    private var filename: String = ""
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

        viewModel.uiStateBtnRoll.observe(this) { uiState ->
            if (uiState.estadoUI.isUpdatedAskAns) {
                girarCardView()
                binding.lblPregResp.text =
                    if (binding.lblPregResp.text == "Pregunta") "Respuesta" else "Pregunta"

                when {
                    uiState.estadoUI.isClearText -> binding.etPregResp.text?.clear()

                    uiState.estadoUI.typeFile == TypeFile.TEXTO -> binding.etPregResp.text =
                        uiState.builder

                    else -> {
                        // Cuando hay una imagen hay que poner esto
                        binding.etPregResp.setText(uiState.estadoImagen.textImgEcrypted)

                        val imagePath = viewModel.imagePathConverted(uiState)

                        binding.ivImagen.setImage(ImageSource.uri(imagePath))
                    }
                }
                //Log.i("Ruta: ", ruta)
                Log.i("Es imagen: ", uiState.estadoUI.typeFile.toString())

                binding.tilContenidoPregResp.visibility =
                    if (uiState.estadoUI.typeFile == TypeFile.IMAGEN) View.GONE else View.VISIBLE
                binding.ivImagen.visibility =
                    if (uiState.estadoUI.typeFile == TypeFile.IMAGEN) View.VISIBLE else View.GONE
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

        viewModel.uiStateBtnBack.observe(this) { uiState ->
            if (uiState.estadoUI.isUpdatedAskAns) {
                binding.lblPregResp.text = "Pregunta"

                when {
                    //!uiState.estadoUI.isThereMoreAsks -> binding.etPregResp.text?.clear()

                    uiState.estadoUI.typeFile == TypeFile.TEXTO -> binding.etPregResp.text =
                        uiState.builder

                    else -> {
                        // Cuando hay una imagen hay que poner esto
                        binding.etPregResp.setText(uiState.estadoImagen.textImgEcrypted)

                        val imagePath = viewModel.imagePathConverted(uiState)

                        binding.ivImagen.setImage(ImageSource.uri(imagePath))
                    }
                }

                binding.tilContenidoPregResp.visibility =
                    if (uiState.estadoUI.typeFile == TypeFile.IMAGEN) View.GONE else View.VISIBLE
                binding.ivImagen.visibility =
                    if (uiState.estadoUI.typeFile == TypeFile.IMAGEN) View.VISIBLE else View.GONE
                binding.imgvCancelar.visibility =
                    if (uiState.estadoUI.isShowCancelar) View.VISIBLE else View.GONE
                binding.imgvQuitColor.visibility =
                    if (uiState.estadoUI.isShowQuitColor) View.VISIBLE else View.GONE
                binding.imgvSelColor.visibility =
                    if (uiState.estadoUI.isShowSelColor) View.VISIBLE else View.GONE

                if (uiState.responseSpanPalabra?.isDoubleColors == true) {
                    Log.i("Sobreponen palabras", uiState.responseSpanPalabra.message)
                    Toast.makeText(
                        applicationContext,
                        uiState.responseSpanPalabra.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(applicationContext, uiState.message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.uiStateBtnNext.observe(this) { uiState ->
            if (uiState.estadoUI.isUpdatedAskAns) {
                binding.lblPregResp.text = "Pregunta"

                when {
                    !uiState.estadoUI.isThereMoreAsks -> binding.etPregResp.text?.clear()

                    uiState.estadoUI.typeFile == TypeFile.TEXTO -> binding.etPregResp.text =
                        uiState.builder

                    else -> {
                        // Cuando hay una imagen hay que poner esto
                        binding.etPregResp.setText(uiState.estadoImagen.textImgEcrypted)

                        val imagePath = viewModel.imagePathConverted(uiState)

                        binding.ivImagen.setImage(ImageSource.uri(imagePath))
                    }
                }

                binding.tilContenidoPregResp.visibility =
                    if (uiState.estadoUI.typeFile == TypeFile.IMAGEN) View.GONE else View.VISIBLE
                binding.ivImagen.visibility =
                    if (uiState.estadoUI.typeFile == TypeFile.IMAGEN) View.VISIBLE else View.GONE
                binding.imgvCancelar.visibility =
                    if (uiState.estadoUI.isShowCancelar) View.VISIBLE else View.GONE
                binding.imgvQuitColor.visibility =
                    if (uiState.estadoUI.isShowQuitColor) View.VISIBLE else View.GONE
                binding.imgvSelColor.visibility =
                    if (uiState.estadoUI.isShowSelColor) View.VISIBLE else View.GONE

                if (uiState.responseSpanPalabra?.isDoubleColors == true) {
                    Log.i("Sobreponen palabras", uiState.responseSpanPalabra.message)
                    Toast.makeText(
                        applicationContext,
                        uiState.responseSpanPalabra.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(applicationContext, uiState.message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.uiStateBtnEliminar.observe(this) { uiState ->
            binding.lblPregResp.text = "Pregunta"

            when {
                !uiState.estadoUI.isThereMoreAsks -> binding.etPregResp.text?.clear()

                uiState.estadoUI.typeFile == TypeFile.TEXTO -> binding.etPregResp.text =
                    uiState.builder

                else -> {
                    // Cuando hay una imagen hay que poner esto
                    binding.etPregResp.setText(uiState.estadoImagen.textImgEcrypted)

                    val imagePath = viewModel.imagePathConverted(uiState)

                    binding.ivImagen.setImage(ImageSource.uri(imagePath))
                }
            }

            binding.tilContenidoPregResp.visibility =
                if (uiState.estadoUI.typeFile == TypeFile.IMAGEN) View.GONE else View.VISIBLE
            binding.ivImagen.visibility =
                if (uiState.estadoUI.typeFile == TypeFile.IMAGEN) View.VISIBLE else View.GONE
            binding.imgvCancelar.visibility =
                if (uiState.estadoUI.isShowCancelar) View.VISIBLE else View.GONE
            binding.imgvQuitColor.visibility =
                if (uiState.estadoUI.isShowQuitColor) View.VISIBLE else View.GONE
            binding.imgvSelColor.visibility =
                if (uiState.estadoUI.isShowSelColor) View.VISIBLE else View.GONE
        }

        viewModel.uiStateBtnSave.observe(this) { uiState ->
            Toast.makeText(
                applicationContext,
                uiState.message,
                Toast.LENGTH_SHORT
            ).show()

            if (uiState.estadoUI.isCreatedGuia) {
                val intent = Intent(applicationContext, ActivityRepasarGuia::class.java)
                intent.putExtra("ruta", uiState.responseGuia.rutaGuiaEstudio)
                startActivity(intent)
                viewModel.toCopyImages()
                finish()
            }
        }

        // Get image count
        viewModel.contImagenes.observe(this@ActivityModificar) { contImagen ->
            contadorImagen = contImagen
            if (imagenPiv == 0) {
                imagenPiv = contadorImagen
            }

            filename = "$contadorImagen.png"
        }

        // Get review
        viewModel.guiaModel.observe(this) {
            // Obtenemos los datos del XML y los guardamos en su respectivo ArrayList.
            val texto = viewModel.getObtenerDatosXML()

            nombreArchivo = nombreArchivo.replace(".xml".toRegex(), "")

            binding.barraSuperiorRegreso.tvTituloToolbar.text = "Modificando: $nombreArchivo"
            colorActual = Color.BLACK

            when {
                // Agregar el texto en el et cuando hay un builder
                texto.estadoUI.typeFile == TypeFile.TEXTO -> binding.etPregResp.text = texto.builder

                // Cuando hay una imagen hay que poner esto
                else -> {
                    binding.etPregResp.setText(texto.estadoImagen.textImgEcrypted)
                    binding.ivImagen.setImage(ImageSource.uri(texto.estadoImagen.textImgUnencrypted))
                }
            }

            binding.tilContenidoPregResp.visibility =
                if (texto.estadoUI.typeFile == TypeFile.IMAGEN) View.GONE else View.VISIBLE
            binding.ivImagen.visibility =
                if (texto.estadoUI.typeFile == TypeFile.IMAGEN) View.VISIBLE else View.GONE
            binding.imgvCancelar.visibility =
                if (texto.estadoUI.isShowCancelar) View.VISIBLE else View.GONE
            binding.imgvQuitColor.visibility =
                if (texto.estadoUI.isShowQuitColor) View.VISIBLE else View.GONE
            binding.imgvSelColor.visibility =
                if (texto.estadoUI.isShowSelColor) View.VISIBLE else View.GONE
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

    private fun initListeners() {
        binding.barraSuperiorRegreso.imgvBack.setOnClickListener {
            cancelarArchivo()
        }

        binding.imgvPregResp.setOnClickListener {
            val editable: Editable =
                Editable.Factory.getInstance().newEditable(binding.etPregResp.text)

            var isEtPregunta = false
            if (binding.lblPregResp.text.toString() == "Pregunta") {
                isEtPregunta = true
            }
            viewModel.clickedRoll(
                editable,
                isEtPregunta,
                viewModel.currentPath.value
            )
        }

        binding.imgvPrevious.setOnClickListener {
            val editable: Editable =
                Editable.Factory.getInstance().newEditable(binding.etPregResp.text)
            Log.i("Editable", editable.toString())

            var isEtPregunta = false
            if (binding.lblPregResp.text.toString() == "Pregunta") {
                isEtPregunta = true
            }

            viewModel.onClickImgvPrevious(
                editable,
                isEtPregunta,
                viewModel.currentPath.value
            )
        }

        binding.imgvNext.setOnClickListener {
            val editable: Editable =
                Editable.Factory.getInstance().newEditable(binding.etPregResp.text)
            Log.i("Editable", editable.toString())

            var isEtPregunta = false
            if (binding.lblPregResp.text.toString() == "Pregunta") {
                isEtPregunta = true
            }

            // Do you want to add more questions?
            if ((viewModel.contadorPregunta + 1) == viewModel.preguntas.size && viewModel.showMessageMoreQuestions) {
                AlertDialog.Builder(this@ActivityModificar)
                    .setTitle("¡Atención!")
                    .setMessage("Se acabaron las preguntas, ¿Quieres agregar más?")
                    .setPositiveButton(
                        "Si"
                    ) { _, _ ->
                        // Cambia el valor de la bandera
                        viewModel.toggleShowMessageMoreQuestions()

                        viewModel.onClickImgvNext(
                            editable,
                            isEtPregunta,
                            viewModel.currentPath.value
                        )

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
            } else {
                viewModel.onClickImgvNext(
                    editable,
                    isEtPregunta,
                    viewModel.currentPath.value
                )
            }
        }

        binding.imgvEliminar.setOnClickListener {
            AlertDialog.Builder(this@ActivityModificar)
                .setTitle("¡Atención!")
                .setMessage("¿Quieres eliminar pregunta/respuesta?")
                .setPositiveButton("Si") { _, _ ->
                    viewModel.onClickEliminar(viewModel.currentPath.value)
                }.setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }.create().show()
        }

        binding.barraSuperiorRegreso.imgvSave.setOnClickListener {
            val editable: Editable =
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
            )
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
                        viewModel.setPintarLetra(texto, cursorPosition, colorActual)
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

    private fun initUI() {
        viewModel.getCountImage()
        viewModel.getGuia(viewModel.currentPath.value)
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
                fos = openFileOutput(filename, MODE_PRIVATE)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)

                var ruta: String = filePathsProvider.fileGuides.toString()
                ruta = ruta.replace("guias".toRegex(), "imagenes")
                val originPath = filePathsProvider.buildFile(filePathsProvider.rutaPrin, filename)
                val copiedPath =
                    filePathsProvider.buildFile(filePathsProvider.fileImagesPiv, filename)

                if (binding.etPregResp.text!!.isNotEmpty()
                    && !binding.etPregResp.text!!.contains(BASERUTA_IMG_CIFRADO)
                ) {
                    AlertDialog.Builder(this@ActivityModificar)
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
                            ) //setImageURI(uri)
                            val cifrado = viewModel.getUrlImagenCifrada(
                                "$BASERUTA_IMG$ruta/$filename",
                                3
                            )
                            // "$baseRutaImagen$fileImages/$filename",
                            //binding.etPregResp.setText(cifrado)

                            binding.tilContenidoPregResp.visibility = View.GONE
                            binding.ivImagen.visibility = View.VISIBLE

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
                    File(filePathsProvider.rutaPrin, filename).delete()

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
                        "$BASERUTA_IMG$ruta/$filename",
                        3
                    )

                    //binding.etPregResp.setText(cifrado)
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
            viewModel.deleteContentInPiv(nombreArchivo)
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
                viewModel.deleteContentInPiv(nombreArchivo)
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
}