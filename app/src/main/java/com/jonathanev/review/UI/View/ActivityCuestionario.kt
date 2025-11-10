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
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.widget.ImageViewCompat
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.jonathanev.review.Core.Constants.BASERUTA_IMG
import com.jonathanev.review.Core.Constants.BASERUTA_IMG_CIFRADO
import com.jonathanev.review.Core.Constants.PICK_IMAGE_REQUEST
import com.jonathanev.review.Data.Model.TypeFile
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.UI.View.Fragments.FragmentDialogColoresPopup
import com.jonathanev.review.UI.ViewModel.ActivityCuestionarioViewModel
import com.jonathanev.review.databinding.ActivityCuestionarioBinding
import dagger.hilt.android.AndroidEntryPoint
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
        nombreArchivo = intent.extras!!.getString("nombre_archivo") ?: ""
        //ruta = "$ruta$nombreArchivo"

        // Se cambia el nombre del titulo del toolbar
        binding.barraSuperiorRegreso.tvTituloToolbar.text = "Creando: $nombreArchivo"
        colorActual = Color.BLACK

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
                        binding.ivImagen.setImage(ImageSource.uri(uiState.estadoImagen.textImgUnencrypted))
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
                    uiState.estadoUI.typeFile == TypeFile.TEXTO -> binding.etPregResp.text =
                        uiState.builder

                    else -> {
                        // Cuando hay una imagen hay que poner esto
                        binding.etPregResp.setText(uiState.estadoImagen.textImgEcrypted)
                        binding.ivImagen.setImage(ImageSource.uri(uiState.estadoImagen.textImgUnencrypted))
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
                        binding.ivImagen.setImage(ImageSource.uri(uiState.estadoImagen.textImgUnencrypted))
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
                    binding.ivImagen.setImage(ImageSource.uri(uiState.estadoImagen.textImgUnencrypted))
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
                viewModel.procesoActualizacion()
                copyImages()
                finish()
            }
        }

        viewModel.contImagenes.observe(this) { contImagen ->
            contadorImagen = contImagen
            filename = "$contadorImagen.png"
        }
    }

    private fun initListeners() {
        binding.barraSuperiorRegreso.imgvBack.setOnClickListener {
            cancelarArchivo()
            viewModel.deleteContentInPiv(nombreArchivo)
        }

        binding.imgvPregResp.setOnClickListener {
            val editable: Editable =
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
            )
        }

        binding.imgvPrevious.setOnClickListener {
            val editable: Editable =
                Editable.Factory.getInstance().newEditable(binding.etPregResp.text)
            var isEtPregunta = false
            if (binding.lblPregResp.text.toString() == "Pregunta") {
                isEtPregunta = true
            }

            val route = filePathsProvider.buildFile(filePathsProvider.fileGuides, nombreArchivo).toString()
            viewModel.onClickImgvPrevious(
                editable,
                isEtPregunta,
                route
            )
        }

        binding.imgvNext.setOnClickListener {
            val editable: Editable =
                Editable.Factory.getInstance().newEditable(binding.etPregResp.text)
            var isEtPregunta = false
            if (binding.lblPregResp.text.toString() == "Pregunta") {
                isEtPregunta = true
            }

            val route = filePathsProvider.buildFile(filePathsProvider.fileGuides, nombreArchivo).toString()
            viewModel.onClickImgvNext(
                editable,
                isEtPregunta,
                route
            )
        }

        binding.imgvEliminar.setOnClickListener {
            AlertDialog.Builder(this@ActivityCuestionario)
                .setTitle("¡Atención!")
                .setMessage("¿Quieres eliminar pregunta/respuesta?")
                .setPositiveButton("Si") { _, _ ->
                    val route = filePathsProvider.buildFile(filePathsProvider.fileGuides, nombreArchivo).toString()
                    viewModel.onClickEliminar(route)
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

            val route = filePathsProvider.buildFile(filePathsProvider.fileGuides, nombreArchivo).toString()
            viewModel.onClickImgvSave(
                editable,
                "$nombreArchivo.xml",
                isEtPregunta,
                ruta = "$route.xml"
            )
        }

        // Visualización del DialogFragment de selección de colores.
        binding.imgvSelColor.setOnClickListener {
            // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
            val dialogo: FragmentDialogColoresPopup = FragmentDialogColoresPopup()
            //=====================================================================================================================
            dialogo.show(supportFragmentManager, "FragmentColor")
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

        // Cambio de botones visibles
        binding.imgvCancelar.setOnClickListener {
            binding.imgvSelColor.visibility = View.VISIBLE
            binding.imgvQuitColor.visibility = View.VISIBLE
            binding.tilContenidoPregResp.visibility = View.VISIBLE

            binding.ivImagen.visibility = View.GONE
            binding.imgvCancelar.visibility = View.GONE

            binding.etPregResp.setText("")
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
}