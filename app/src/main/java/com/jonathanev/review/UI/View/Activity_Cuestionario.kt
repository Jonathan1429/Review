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
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.core.widget.ImageViewCompat
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.jonathanev.review.Core.Constants.PICK_IMAGE_REQUEST
import com.jonathanev.review.Core.Constants.baseRutaImagen
import com.jonathanev.review.Core.Constants.baseRutaImagenCifrado
import com.jonathanev.review.Core.Constants.file
import com.jonathanev.review.Core.Constants.fileImages
import com.jonathanev.review.Core.Constants.fileImagesPiv
import com.jonathanev.review.Core.Constants.rutaPrin
import com.jonathanev.review.Data.Model.ColorPregModel
import com.jonathanev.review.Fragments.Fragment_DialogColores_popup
import com.jonathanev.review.UI.ViewModel.ActivityCuestionarioViewModel
import com.jonathanev.review.databinding.ActivityCuestionarioBinding
import dagger.hilt.android.AndroidEntryPoint
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@AndroidEntryPoint
class Activity_Cuestionario : AppCompatActivity() {
    private var binding: ActivityCuestionarioBinding? = null
    private var nombreArchivo: String? = null
    private var colorActual: Int = 0
    private var colorPintarPalabra: Int = 0
    private var posColorInicial: Int = -1
    private var posColorFinal: Int = -1
    private var preguntas: ArrayList<String> = ArrayList()
    private var respuestas: ArrayList<String> = ArrayList()
    private val preguntasColor: ArrayList<ColorPregModel> = ArrayList()
    private val respuestasColor: ArrayList<ColorPregModel> = ArrayList()
    var builder: SpannableStringBuilder? = null
    private var contadorPregunta: Int = 0
    private var contadorImagen = 0
    private var uri: Uri? = null
    private var longCaracteres = 0
    private var pregResBandera = false // Bandera para cuando se le de click atras o delante.
    private val activityCuestionarioViewModel by viewModels<ActivityCuestionarioViewModel>()
    private var filename: String = "" // Ruta/imagen.png

    // Seleccionar imagen
    private val pickMedia =
        registerForActivityResult(PickVisualMedia()) { uri ->
            if (uri != null) {
                // Toma permisos de persistencia para la URI
                takePersistableUriPermission(uri)

                if (binding!!.etPregResp.text!!.isNotEmpty()) {
                    AlertDialog.Builder(this@Activity_Cuestionario)
                        .setTitle("¡Atención!")
                        .setMessage("Se borrará el texto para agregar la imagen, ¿Quieres continuar?")
                        .setPositiveButton(
                            "Si"
                        ) { _, _ ->
                            binding!!.ivImagen.setImage(ImageSource.uri(uri)) //setImageURI(uri)
                            binding!!.tilContenidoPregResp.visibility = View.GONE

                            binding!!.ivImagen.visibility = View.VISIBLE
                            binding!!.etPregResp.setText(uri.toString())
                        }
                        .setNegativeButton(
                            "Cancelar"
                        ) { dialog, _ ->
                            dialog.dismiss()
                        }.create().show()
                } else {
                    binding!!.ivImagen.setImage(ImageSource.uri(uri)) //setImageURI(uri)
                    binding!!.tilContenidoPregResp.visibility = View.GONE

                    binding!!.ivImagen.visibility = View.VISIBLE
                    binding!!.etPregResp.setText(uri.toString())
                }
            } else {
                binding!!.imgvCancelar.visibility = View.GONE

                binding!!.imgvQuitColor.visibility = View.VISIBLE
                binding!!.imgvSelColor.visibility = View.VISIBLE
            }
        }

    // Creamos la serialización y la clase para crear archivos de manera global.
    var serializer: XmlSerializer = Xml.newSerializer()
    var fos: FileOutputStream? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuestionarioBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

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

        // Recibimos el nombre del archivo del popupFragment Nueva Guia.
        nombreArchivo = intent.extras!!.getString("nombre_archivo")

        // Se cambia el nombre del titulo del toolbar
        binding!!.barraSuperiorRegreso.tvTituloToolbar.text = "Creando: $nombreArchivo"
        colorActual = Color.BLACK

        binding!!.barraSuperiorRegreso.imgvBack.setOnClickListener {
            cancelarArchivo()
            deleteImages()
        }

        binding!!.imgvPregResp.setOnClickListener {
            activityCuestionarioViewModel.clickedRoll()
        }

        binding!!.imgvPrevious.setOnClickListener {
            val editable: Editable =
                Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
            var isEtPregunta = false
            if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                isEtPregunta = true
            }

            activityCuestionarioViewModel.onClickImgvPrevious(
                preguntas,
                respuestas,
                contadorPregunta,
                editable,
                isEtPregunta
            )
        }

        binding!!.imgvNext.setOnClickListener {
            val editable: Editable =
                Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
            var isEtPregunta = false
            if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                isEtPregunta = true
            }

            activityCuestionarioViewModel.onClickImgvNext(
                preguntas,
                respuestas,
                contadorPregunta,
                editable,
                isEtPregunta
            )
        }

        binding!!.imgvEliminar.setOnClickListener {
            AlertDialog.Builder(this@Activity_Cuestionario)
                .setTitle("¡Atención!")
                .setMessage("¿Quieres eliminar la pregunta?")
                .setPositiveButton("Si") { dialogInterface, i ->
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
                        binding!!.imgvSelColor.visibility = View.VISIBLE
                        binding!!.tilContenidoPregResp.visibility = View.VISIBLE
                        binding!!.ivImagen.visibility = View.GONE
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
                }

                .setNegativeButton("Cancelar") { dialog, i ->
                    dialog.dismiss()
                }.create().show()
        }

        binding!!.barraSuperiorRegreso.imgvSave.setOnClickListener {
            val editable: Editable =
                Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
            var isEtPregunta = false
            if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                isEtPregunta = true
            }

            activityCuestionarioViewModel.onClickImgvSave(
                preguntas,
                respuestas,
                contadorPregunta,
                editable,
                nombreArchivo.toString(),
                isEtPregunta
            )
        }

        activityCuestionarioViewModel.uiStateBtnSave.observe(this) { uiState ->
            Toast.makeText(
                applicationContext,
                uiState.message,
                Toast.LENGTH_SHORT
            ).show()

            if (uiState.estadoUI.isCreatedGuia) {
                val intent = Intent(applicationContext, Activity_RepasarGuia::class.java)
                intent.putExtra("ruta", uiState.responseGuia.rutaGuiaEstudio)
                startActivity(intent)
                activityCuestionarioViewModel.procesoActualizacion()
                finish()
            }
        }

        // Visualización del DialogFragment de selección de colores.
        binding!!.imgvSelColor.setOnClickListener {
            // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
            val dialogo: Fragment_DialogColores_popup = Fragment_DialogColores_popup()
            //=====================================================================================================================
            dialogo.show(supportFragmentManager, "FragmentColor")
        }

        // Eliminar textos con colores
        binding!!.imgvQuitColor.setOnClickListener {
            val text = binding!!.etPregResp.text
            val spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder(text)
            spannableStringBuilder.clearSpans()

            binding!!.etPregResp.text = spannableStringBuilder

            colorActual = Color.BLACK
            setColor(colorActual)
        }

        binding!!.imgvImage.setOnClickListener {
            binding!!.imgvSelColor.visibility = View.GONE
            binding!!.imgvQuitColor.visibility = View.GONE

            binding!!.imgvCancelar.visibility = View.VISIBLE

            // pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            openSomeActivityForResult()
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

        binding!!.etPregResp.addTextChangedListener(object : TextWatcher {
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
                longCaracteres = binding!!.etPregResp.length()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Aquí puedes verificar si se ha añadido un salto de línea en este momento
                seAgregoSaltoDeLinea =
                    count > before && s?.subSequence(start, start + count)
                        ?.contains("\n") == true
            }

            override fun afterTextChanged(texto: Editable?) {
                if (!texto.toString()
                        .contains(baseRutaImagenCifrado) && (binding!!.etPregResp.length() - longCaracteres) == 1
                ) {
                    // Si hay un salto de linea o es color negro no se pinta nada
                    if (colorActual != -16777216 && !seAgregoSaltoDeLinea) {
                        pintarLetra(texto)
                    }
                }
            }
        })

        activityCuestionarioViewModel.rollClicked.observe(this) {
            if (it) {
                if (binding!!.etPregResp.text.toString().isNotEmpty()) {
                    setSpanPalabra()

                    var editable: Editable =
                        Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
                    var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                        0,
                        editable.length,
                        ForegroundColorSpan::class.java
                    )

                    // Se colocan las etiquetas en cada palabra con color
                    val editableEditquetas = colocarEtiquetas(colorSpans, editable)

                    if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                        if ((contadorPregunta + 1) > respuestas.size) {
                            binding!!.lblPregResp.text = "Respuesta"
                            // binding!!.lblPregResp.text = "Respuesta"
                            preguntas.add(contadorPregunta, editableEditquetas.toString())
                            binding!!.etPregResp.setText("")
                            binding!!.ivImagen.visibility = View.GONE
                            binding!!.tilContenidoPregResp.visibility = View.VISIBLE

                            binding!!.imgvCancelar.visibility = View.GONE
                            binding!!.imgvQuitColor.visibility = View.VISIBLE
                            binding!!.imgvSelColor.visibility = View.VISIBLE
                        } else {
                            binding!!.lblPregResp.text = "Respuesta"
                            preguntas[contadorPregunta] = editableEditquetas.toString()
                            pintarTexto(contadorPregunta)
                            // binding!!.lblPregResp.text = "Respuesta"
                        }
                        girarCardView()
                    } else {
                        if ((contadorPregunta + 1) > respuestas.size) {
                            binding!!.lblPregResp.text = "Pregunta"
                            respuestas.add(contadorPregunta, editableEditquetas.toString())
                            pintarTexto(contadorPregunta)
                            // binding!!.lblPregResp.text = "Pregunta"
                        } else {
                            binding!!.lblPregResp.text = "Pregunta"
                            respuestas[contadorPregunta] = editableEditquetas.toString()
                            pintarTexto(contadorPregunta)
                            // binding!!.lblPregResp.text = "Pregunta"
                        }
                        girarCardView()
                    }

                    activityCuestionarioViewModel.clickedRoll()
                    posColorFinal = -1
                    posColorInicial = -1
                    colorPintarPalabra = 0
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Asegurate de no dejar ningun campo vacio",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.i("Crear pregunta: ", "Asegurate de no dejar ningun campo vacio")
                }
            }
        }

        activityCuestionarioViewModel.colorAnterior.observe(this) {
            if (posColorInicial == -1) {
                colorPintarPalabra = it

                val cursorPosition = binding!!.etPregResp.selectionStart
                val lastCharIndex = cursorPosition - 1
                posColorInicial = lastCharIndex
            } else {
                /*al cursorPosition = binding!!.etPregResp.selectionStart
                posColorFinal = cursorPosition*/

                // Obtener los spans dentro del rango especificado
                val spansToRemove = binding!!.etPregResp.text!!.getSpans(
                    posColorInicial,
                    posColorFinal,
                    ForegroundColorSpan::class.java
                )

                for (span in spansToRemove) {
                    binding!!.etPregResp.text!!.removeSpan(span)
                }

                binding!!.etPregResp.text!!.setSpan(
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

        activityCuestionarioViewModel.contImagenes.observe(this) { contImagen ->
            contadorImagen = contImagen
            filename = "$contadorImagen.png"
        }

        activityCuestionarioViewModel.uiStateBtnBack.observe(this) { uiState ->
            contadorPregunta = uiState.contadorPregunta

            if (uiState.estadoUI.isUpdatedAskAns) {
                binding!!.lblPregResp.text = "Pregunta"

                if (!uiState.estadoUI.isThereMoreAsks) {
                    binding!!.etPregResp.text?.clear()
                } else {
                    // Agregar el texto en el et cuando hay un builder
                    if (!uiState.estadoUI.isShowImage) {
                        binding!!.etPregResp.text = uiState.builder
                    } else {
                        // Cuando hay una imagen hay que poner esto
                        binding!!.etPregResp.setText(uiState.estadoImagen.textImgEcrypted)
                        binding!!.ivImagen.setImage(ImageSource.uri(uiState.estadoImagen.textImgUnencrypted))
                    }
                }

                binding!!.tilContenidoPregResp.visibility =
                    if (uiState.estadoUI.isShowImage) View.GONE else View.VISIBLE
                binding!!.ivImagen.visibility =
                    if (uiState.estadoUI.isShowImage) View.VISIBLE else View.GONE
                binding!!.imgvCancelar.visibility =
                    if (uiState.estadoUI.isShowCancelar) View.VISIBLE else View.GONE
                binding!!.imgvQuitColor.visibility =
                    if (uiState.estadoUI.isShowQuitColor) View.VISIBLE else View.GONE
                binding!!.imgvSelColor.visibility =
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

        activityCuestionarioViewModel.uiStateBtnNext.observe(this) { uiState ->
            contadorPregunta = uiState.contadorPregunta

            if (uiState.estadoUI.isUpdatedAskAns) {
                binding!!.lblPregResp.text = "Pregunta"
                // val posPregFin = preguntas.size - 1
                if (!uiState.estadoUI.isThereMoreAsks) {
                    binding!!.etPregResp.text?.clear()
                } else {
                    // Agregar el texto en el et cuando hay un builder
                    if (!uiState.estadoUI.isShowImage) {
                        binding!!.etPregResp.text = uiState.builder
                    } else {
                        // Cuando hay una imagen hay que poner esto
                        binding!!.etPregResp.setText(uiState.estadoImagen.textImgEcrypted)
                        binding!!.ivImagen.setImage(ImageSource.uri(uiState.estadoImagen.textImgUnencrypted))
                    }
                }

                binding!!.tilContenidoPregResp.visibility =
                    if (uiState.estadoUI.isShowImage) View.GONE else View.VISIBLE
                binding!!.ivImagen.visibility =
                    if (uiState.estadoUI.isShowImage) View.VISIBLE else View.GONE
                binding!!.imgvCancelar.visibility =
                    if (uiState.estadoUI.isShowCancelar) View.VISIBLE else View.GONE
                binding!!.imgvQuitColor.visibility =
                    if (uiState.estadoUI.isShowQuitColor) View.VISIBLE else View.GONE
                binding!!.imgvSelColor.visibility =
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
    }

    private fun setSpanPalabra() {
        var editable: Editable =
            Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
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
                val spansToRemove = binding!!.etPregResp.text!!.getSpans(
                    start,
                    endAnterior,
                    ForegroundColorSpan::class.java
                )

                for (span in spansToRemove) {
                    binding!!.etPregResp.text!!.removeSpan(span)
                }

                binding!!.etPregResp.text!!.setSpan(
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
            val spansToRemove = binding!!.etPregResp.text!!.getSpans(
                start,
                endAnterior,
                ForegroundColorSpan::class.java
            )

            for (span in spansToRemove) {
                binding!!.etPregResp.text!!.removeSpan(span)
            }

            binding!!.etPregResp.text!!.setSpan(
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

    private fun initUI() {
        activityCuestionarioViewModel.getCountImage()
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
            binding!!.imgvCancelar.visibility = View.GONE

            binding!!.imgvQuitColor.visibility = View.VISIBLE
            binding!!.imgvSelColor.visibility = View.VISIBLE
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        var fos: FileOutputStream? = null
        try {
            fos = openFileOutput(filename, MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)

            if (binding!!.etPregResp.text!!.isNotEmpty() && !binding!!.etPregResp.text!!.contains(
                    baseRutaImagen
                )
            ) {
                AlertDialog.Builder(this@Activity_Cuestionario)
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

                        binding!!.ivImagen.setImage(ImageSource.uri("$fileImagesPiv/$filename")) //setImageURI(uri)
                        binding!!.tilContenidoPregResp.visibility = View.GONE
                        binding!!.ivImagen.visibility = View.VISIBLE
                        val cifrado = cifrar("$baseRutaImagen$fileImages/$filename", 3)
                        binding!!.etPregResp.setText(cifrado)

                        // contadorImagen += 1
                        // filename = "$contadorImagen.png"

                        activityCuestionarioViewModel.llamaCorruIncremento()
                    }
                    .setNegativeButton(
                        "Cancelar"
                    ) { dialog, _ ->
                        dialog.dismiss()
                        binding!!.imgvCancelar.visibility = View.GONE

                        binding!!.imgvQuitColor.visibility = View.VISIBLE
                        binding!!.imgvSelColor.visibility = View.VISIBLE
                    }.create().show()
            } else {
                Files.copy(
                    Paths.get("$rutaPrin/$filename"),
                    Paths.get("$fileImagesPiv/$filename"),
                    StandardCopyOption.REPLACE_EXISTING
                )

                // Borrar archivo
                File(rutaPrin, filename).delete()

                binding!!.ivImagen.setImage(ImageSource.uri("$fileImagesPiv/$filename")) //setImageURI(uri)
                binding!!.tilContenidoPregResp.visibility = View.GONE
                binding!!.ivImagen.visibility = View.VISIBLE

                val cifrado = cifrar("$baseRutaImagen$fileImages/$filename", 3)
                binding!!.etPregResp.setText(cifrado)

                // contadorImagen += 1
                // filename = "$contadorImagen.png"

                activityCuestionarioViewModel.llamaCorruIncremento()
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
                flipAnimator =
                    ObjectAnimator.ofFloat(binding!!.flContenidoPregResp, "rotationY", 180f, 0f)
                flipAnimator.duration = 1000 // Duración de la animación en milisegundos
                flipAnimator.start()
            }
        } else {
            var flipAnimator =
                ObjectAnimator.ofFloat(
                    binding!!.flContenidoPregResp,
                    "rotationY",
                    0f,
                    180f
                ) // ivImagen
            flipAnimator.duration = 0 // Duración de la animación en milisegundos
            flipAnimator.start()
            flipAnimator.doOnEnd {
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
            cancelarArchivo()
            deleteImages()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // Mostrará un mensaje diciendo que el archivo se eliminará ya que no se terminó de crear.
    private fun cancelarArchivo() {
        // Se ejecuta cuando se regresa sin guardar.
        AlertDialog.Builder(this@Activity_Cuestionario)
            .setTitle("¡Atención!")
            .setMessage(
                "Aún no terminas de crear la guia, se borrará el " +
                        "archivo creado, ¿seguro deseas continuar?"
            )
            .setPositiveButton(
                "Continuar"
            ) { dialogInterface, i -> // Si el archivo se creó y existe, se elimina y te informa en consola
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
            ) { dialog, i -> dialog.dismiss() }.create().show()
    }

    // Creamos el archivo en el dispositivo e inicializamos algunas etiquetas.
    private fun crearArchivo(nombreArchivo: String?) {
        try {
            fos = openFileOutput("$nombreArchivo.xml", MODE_PRIVATE)
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
                serializer.attribute("", "pregunta", preguntas.get(i))
                serializer.attribute("", "respuesta", respuestas.get(i))
                serializer.endTag("", "Interrogante")
            }
            // Si los campos estan vacios simplemente cierro las etiquetas y directamente
            // guardo el documento en el teléfono.
            serializer.endTag("", "Cuestionario")
            serializer.endTag("", "GuiaEstudio")
            serializer.endDocument()
            serializer.flush()
            fos?.close()

            val rutaGuiaCreada = File("$rutaPrin/$nombreArchivo.xml")
            if (rutaGuiaCreada.exists()) {
                Toast.makeText(
                    this, "Guia de estudio creada exitosamente",
                    Toast.LENGTH_SHORT
                ).show()
                Log.i("Creación: ", "Guia de estudio creada exitosamente")

                Files.copy(
                    Paths.get("$rutaPrin/$nombreArchivo.xml"),
                    Paths.get("$file/$nombreArchivo.xml"),
                    StandardCopyOption.REPLACE_EXISTING
                )

                // Borrar archivo
                File(rutaPrin, "$nombreArchivo.xml").delete()
                val ruta = "$file/$nombreArchivo.xml"
                val intent: Intent =
                    Intent(applicationContext, Activity_RepasarGuia::class.java)
                intent.putExtra("ruta", ruta)

                startActivity(intent)
                activityCuestionarioViewModel.getAllUpdatedGuides(file)
                copyImages()
                finish()
            } else {
                Toast.makeText(
                    this, "Guia de estudio no se creó correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                Log.i("Creación: ", "Guia de estudio no se creó correctamente")
            }

            /*val ruta = "$file/$nombreArchivo.xml"
            val intent: Intent = Intent(applicationContext, Activity_RepasarGuia::class.java)
            intent.putExtra("ruta", ruta)

            startActivity(intent)
            activityCuestionarioViewModel.getAllUpdatedGuides(file)
            finish()*/
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

                Files.copy(
                    Paths.get("$fileImagesPiv/$name"),
                    Paths.get("$fileImages/$name"),
                    StandardCopyOption.REPLACE_EXISTING
                )

                // Borrar archivo
                File(fileImagesPiv, name).delete()
            }
        }
    }

    private fun colocarEtiquetas(
        colorSpans: Array<ForegroundColorSpan>,
        editable: Editable
    ): Editable {
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

        return editable
    }

    private fun pintarTexto(contadorPregunta: Int) {
        var contColorPreg: Int = 0
        var inicio: Int = 0
        var fin: Int = 0
        var colorPregModel: ColorPregModel? = null
        var texto: String = ""
        if (binding!!.lblPregResp.text.toString() == "Pregunta") {
            texto = preguntas[contadorPregunta]
            // uri = texto.toUri()
        } else {
            texto = respuestas[contadorPregunta]
            // uri = texto.toUri()
        }

        if (texto.contains(baseRutaImagenCifrado)) {
            val descifrado = cifrar(texto, 26 - 3)
            binding!!.etPregResp.setText(texto)
            texto = descifrado.replace(baseRutaImagen.toRegex(), "")
            texto = texto.replace("imagenes".toRegex(), "imagenesPivote")
            // uri = texto.toUri()
            binding!!.ivImagen.setImage(ImageSource.uri(texto)) //setImageURI(uri)
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

            binding!!.etPregResp.text = builder
        }

        preguntasColor.clear()
        respuestasColor.clear()

        // Bandera ingresada para que no haga cambios de color cuando se detecte un cambio en ET.
        // pregResBandera = true
        // pregResBandera = false
    }

    private fun pintarLetra(texto: Editable?) {
        texto?.let {
            if (it.isNotEmpty() && !pregResBandera) {
                val cursorPosition = binding!!.etPregResp.selectionStart
                val lastCharIndex = cursorPosition - 1
                posColorFinal = lastCharIndex + 1

                it.setSpan(
                    ForegroundColorSpan(colorActual),
                    lastCharIndex,
                    lastCharIndex + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                binding!!.etPregResp.setSelection(lastCharIndex + 1)
                pregResBandera = false
            }
        }
    }

    fun setColor(@ColorInt color: Int?) {
        if (color == null) {
            ImageViewCompat.setImageTintList(binding!!.imgvSelColor, null)
            return
        }
        ImageViewCompat.setImageTintMode(binding!!.imgvSelColor, PorterDuff.Mode.SRC_ATOP)
        ImageViewCompat.setImageTintList(binding!!.imgvSelColor, ColorStateList.valueOf(color))
        colorActual = color
    }

    fun cifrar(texto: String, desplazamiento: Int): String {
        val resultado = StringBuilder()

        for (caracter in texto) {
            if (caracter.isLetter()) {
                val base = if (caracter.isUpperCase()) 'A' else 'a'
                val letraCifrada =
                    ((caracter - base + desplazamiento) % 26 + base.code).toChar()
                resultado.append(letraCifrada)
            } else {
                resultado.append(caracter)
            }
        }

        return resultado.toString()
    }
}