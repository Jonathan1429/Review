package com.jonathanev.review.Activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Xml
import android.view.ActionMode
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.jonathanev.review.Clases.ColoresPregunta
import com.jonathanev.review.Fragments.Fragment_DialogColores_popup
import com.jonathanev.review.R
import com.jonathanev.review.databinding.ActivityCuestionarioBinding
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Activity_Cuestionario constructor() : AppCompatActivity() {
    private var binding: ActivityCuestionarioBinding? = null
    private var nombreArchivo: String? = null
    private var colorActual: Int = 0
    private val preguntas: ArrayList<String> = ArrayList()
    private val respuestas: ArrayList<String> = ArrayList()
    private val preguntasColor: ArrayList<ColoresPregunta> = ArrayList()
    private val respuestasColor: ArrayList<ColoresPregunta> = ArrayList()
    var builder: SpannableStringBuilder? = null
    private var contadorPregunta: Int = 0
    private val mInterstitialAd: InterstitialAd? = null

    // Creamos la serialización y la clase para crear archivos de manera global.
    var serializer: XmlSerializer = Xml.newSerializer()
    var fos: FileOutputStream? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuestionarioBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Sección de anuncios
        /*MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();

        // Anuncio de prueba: ca-app-pub-3940256099942544/1033173712
        // Anuncio de GoogleAdmob: ca-app-pub-5116088101296740/2671658549
        InterstitialAd.load(this,"ca-app-pub-5116088101296740/2671658549", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });*/

        // Recibimos el nombre del archivo del popupFragment Nueva Guia.
        nombreArchivo = intent.extras!!.getString("nombre_archivo")

        // Se cambia el nombre del titulo del toolbar
        binding!!.barraSuperiorRegreso.tvTituloToolbar.text = "Creando: $nombreArchivo"

        colorActual = Color.BLACK
        //colorActual(colorActual)
        binding!!.barraSuperiorRegreso.imgvBack.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                cancelarArchivo()
            }
        })

        /*binding!!.imgvColor.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
                val dialogo: Fragment_DialogColores_popup = Fragment_DialogColores_popup()
                //=====================================================================================================================
                dialogo.show(supportFragmentManager, "FragmentColor")
            }
        })*/

        //===================================================================================================================== VERIFICAR ESTE MÉTODO.
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


                if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                    if (contadorPregunta > (respuestas.size - 1)) {
                        binding!!.tilContenidoPregResp.hint = "Respuesta"
                        preguntas.add(contadorPregunta, editable.toString())
                        binding!!.etPregResp.setText("")
                    } else {
                        binding!!.tilContenidoPregResp.hint = "Respuesta"
                        preguntas[contadorPregunta] = editable.toString()
                        pintarTexto(contadorPregunta)
                    }
                    girarCardView()
                } else {
                    if (contadorPregunta > (respuestas.size - 1)) {
                        binding!!.tilContenidoPregResp.hint = "Pregunta"
                        respuestas.add(contadorPregunta, editable.toString())
                    } else {
                        binding!!.tilContenidoPregResp.hint = "Pregunta"
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
            }
        }

        binding!!.imgvNext.setOnClickListener {
            // Validamos campos vacios en la pregunta o respuesta.
            if (binding!!.etPregResp.text.toString().isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Asegurate de no dejar ningun campo vacio",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Se le quita 1 para hacer referencia al arreglo
                // tamaño 3-1 = 2 [0,1,2].
                //val longi: Int = preguntas.size - 1 HAY QUE VALIDAR SI AQUÍ TAMBIÉN FUNCIONA COMENTANDO ESTE
                val longi: Int = respuestas.size - 1

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

                    if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                        preguntas[contadorPregunta] = editable.toString()
                    } else {
                        respuestas[contadorPregunta] = editable.toString()
                    }

                    // Mientras el contadorPregunta sea menor escribiremos la siguiente pregunta
                    // en los et.
                    if (contadorPregunta < longi) {
                        // Pintamos el texto en la pregunta actual

                        binding!!.tilContenidoPregResp.hint = "Pregunta"
                        pintarTexto(contadorPregunta + 1)
                    } else {
                        // Si le das click a que si, ya no te preguntará nuevamente
                        // aunque te regreses a componer otras preg.
                        // Si el contadorPregunta es igual entonces solo escribiremos los campos vacios.
                        binding!!.tilContenidoPregResp.hint = "Pregunta"
                        binding!!.etPregResp.setText("")
                    }
                } else { // Si contadorPregunta es mayor a lo que hay en el arreglo.
                    if (binding!!.etPregResp.text.toString().isEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "Asegurate de llenar una pregunta y una respuesta",
                            Toast.LENGTH_SHORT
                        ).show()
                        contadorPregunta--
                    } else {
                        // Cuando no hay guardadas las mismas preguntas que respuestas.
                        if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                            Toast.makeText(
                                applicationContext,
                                "Asegurate de llenar una pregunta y una respuesta",
                                Toast.LENGTH_SHORT
                            ).show()
                            contadorPregunta--
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
                            respuestas.add(contadorPregunta, editable.toString())
                            binding!!.tilContenidoPregResp.hint = "Pregunta"
                            binding!!.etPregResp.setText("")
                        }
                    }
                }
                contadorPregunta++
            }

            /*// contadorPregunta++

            // Validamos campos vacios en la pregunta o respuesta.
            if (binding!!.etPregResp.text.toString().isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Asegurate de no dejar ningun campo vacio",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Se le quita 1 para hacer referencia al arreglo
                // tamaño 3-1 = 2 [0,1,2].
                val longi: Int = preguntas.size - 1

                // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                // de lo que esté en la posición 0.
                if (contadorPregunta < longi) {
                    var editable: Editable = Editable.Factory.getInstance().newEditable(
                        binding!!.etPregResp.text
                    )
                    var colorSpans: Array<ForegroundColorSpan> =
                        editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)

                    respuestas[contadorPregunta] = editable.toString()
                    pintarTexto(contadorPregunta + 1)

                    // Mientras el contadorPregunta sea menor escribiremos la siguiente pregunta
                    // en los et y se borran las etiquetas de colores.
                    /*if (contadorPregunta < longi) {
                        // Pintamos el texto

                    } else {
                        // Si el contadorPregunta es igual entonces solo escribiremos los campos vacios.
                        respuestas.add(contadorPregunta, editable.toString())
                        binding!!.etPregResp.setText("")
                    }*/
                } else {
                    // Si el contadorPregunta es mayor entonces agregaremos la pregunta actual a los
                    // arreglos.«»
                    var editable: Editable = Editable.Factory.getInstance()
                        .newEditable(binding!!.etPregResp.text)
                    var colorSpans: Array<ForegroundColorSpan> =
                        editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)
                    respuestas.add(contadorPregunta, editable.toString())
                }

                binding!!.tilContenidoPregResp.hint = "Pregunta"
                binding!!.etPregResp.setText("")
                contadorPregunta++


                if(binding!!.tilContenidoPregResp.hint == "Pregunta"){
                    Toast.makeText(
                        applicationContext,
                        "Asegurate de llenar una pregunta y una respuesta",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                }
            }*/
        }

        // Validar que cuando este lleno y se regrese te diga que no está todo lleno.
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

                        if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                            preguntas[contadorPregunta] = editable.toString()
                        } else {
                            respuestas[contadorPregunta] = editable.toString()
                        }

                        binding!!.tilContenidoPregResp.hint = "Pregunta"
                        // Pintamos el texto en la pregunta actual
                        pintarTexto(contadorPregunta - 1)
                    }
                } else {
                    if (binding!!.tilContenidoPregResp.hint == "Pregunta" && binding!!.etPregResp.text.toString()
                            .isNotEmpty() ||
                        binding!!.tilContenidoPregResp.hint == "Respuesta" && binding!!.etPregResp.text.toString()
                            .isEmpty()
                    ) {
                        Toast.makeText(
                            applicationContext,
                            "Asegurate de llenar pregunta y respuesta",
                            Toast.LENGTH_SHORT
                        ).show()

                        contadorPregunta++
                    } else {
                        if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                            pintarTexto(contadorPregunta - 1)
                        } else {
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

                            binding!!.tilContenidoPregResp.hint = "Pregunta"
                            respuestas.add(contadorPregunta, editable.toString())
                            pintarTexto(contadorPregunta - 1)
                        }
                    }
                }

                contadorPregunta--
            } else {
                Toast.makeText(
                    applicationContext,
                    "Ya no tienes preguntas anteriores",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        /*binding!!.btnAtrasPregunta.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                // El contadorPregunta tiene que ser mayor a 0 sino significa que no hay preguntas anteriores.
                if (contadorPregunta > 0) {
                    // Se le quita 1 para hacer referencia al arreglo
                    // tamaño 3-1 = 2 [0,1,2].
                    val longi: Int = preguntas.size - 1

                    // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                    // de lo que esté en la posición 0.
                    if (contadorPregunta <= longi) {
                        // Validamos campos vacios en la pregunta y respuesta.
                        if ((binding!!.etPregunta.text.toString().isEmpty()
                                    || binding!!.etRespuesta.text.toString().isEmpty())
                        ) {
                            Toast.makeText(
                                applicationContext,
                                "Asegurate de no dejar ningun campo vacio",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Se resta uno al final y así se queda neutral.
                            contadorPregunta++
                        } else {
                            // Si los campos están bien se sobre escribe.
                            var editable: Editable = Editable.Factory.getInstance().newEditable(
                                binding!!.etPregunta.text
                            )
                            var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                                0,
                                editable.length,
                                ForegroundColorSpan::class.java
                            )

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable)
                            preguntas[contadorPregunta] = editable.toString()
                            editable = Editable.Factory.getInstance()
                                .newEditable(binding!!.etRespuesta.text)
                            colorSpans = editable.getSpans(
                                0,
                                editable.length,
                                ForegroundColorSpan::class.java
                            )

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable)
                            respuestas[contadorPregunta] = editable.toString()

                            // Pintamos el texto en la pregunta actual
                            pintarTexto(contadorPregunta - 1)
                        }
                    } else {
                        if ((binding!!.etPregunta.text.toString().isEmpty()
                                    || binding!!.etRespuesta.text.toString().isEmpty())
                        ) {
                            Toast.makeText(
                                applicationContext,
                                "Asegurate de no dejar ningun campo vacio",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Se resta uno al final y así se queda neutral.
                            contadorPregunta++
                        } else {
                            // Si el contadorPregunta es mayor entonces agregaremos la pregunta actual a los
                            // arreglos.«»
                            var editable: Editable = Editable.Factory.getInstance().newEditable(
                                binding!!.etPregunta.text
                            )
                            var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                                0,
                                editable.length,
                                ForegroundColorSpan::class.java
                            )

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable)
                            preguntas.add(contadorPregunta, editable.toString())
                            editable = Editable.Factory.getInstance()
                                .newEditable(binding!!.etRespuesta.text)
                            colorSpans = editable.getSpans(
                                0,
                                editable.length,
                                ForegroundColorSpan::class.java
                            )

                            // Se colocan las etiquetas en cada palabra con color
                            colocarEtiquetas(colorSpans, editable)
                            respuestas.add(contadorPregunta, editable.toString())

                            // Si el contadorPregunta es mayor a lo que hay guardado entonces
                            // pintamos el texto anterior.
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
                }
            }
        })*/

        /*binding!!.btnSiguientePregunta.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                // Validamos campos vacios en la pregunta o respuesta.
                if ((binding!!.etPregunta.text.toString().isEmpty()
                            || binding!!.etRespuesta.text.toString().isEmpty())
                ) {
                    Toast.makeText(
                        applicationContext,
                        "Asegurate de no dejar ningun campo vacio",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Se le quita 1 para hacer referencia al arreglo
                    // tamaño 3-1 = 2 [0,1,2].
                    val longi: Int = preguntas.size - 1

                    // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                    // de lo que esté en la posición 0.
                    if (contadorPregunta <= longi) {
                        var editable: Editable = Editable.Factory.getInstance().newEditable(
                            binding!!.etPregunta.text
                        )
                        var colorSpans: Array<ForegroundColorSpan> =
                            editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)
                        preguntas[contadorPregunta] = editable.toString()
                        editable = Editable.Factory.getInstance()
                            .newEditable(binding!!.etRespuesta.text)
                        colorSpans =
                            editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)
                        respuestas[contadorPregunta] = editable.toString()

                        // Mientras el contadorPregunta sea menor escribiremos la siguiente pregunta
                        // en los et y se borran las etiquetas de colores.
                        if (contadorPregunta < longi) {
                            // Pintamos el texto
                            pintarTexto(contadorPregunta + 1)
                        } else {
                            // Si el contadorPregunta es igual entonces solo escribiremos los campos vacios.
                            binding!!.etPregunta.setText("")
                            binding!!.etRespuesta.setText("")
                            binding!!.etPregunta.requestFocus()
                        }
                    } else {
                        // Si el contadorPregunta es mayor entonces agregaremos la pregunta actual a los
                        // arreglos.«»
                        var editable: Editable = Editable.Factory.getInstance()
                            .newEditable(binding!!.etPregunta.text)
                        var colorSpans: Array<ForegroundColorSpan> =
                            editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)
                        preguntas.add(contadorPregunta, editable.toString())
                        editable = Editable.Factory.getInstance()
                            .newEditable(binding!!.etRespuesta.text)
                        colorSpans =
                            editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)
                        respuestas.add(contadorPregunta, editable.toString())
                        binding!!.etPregunta.setText("")
                        binding!!.etRespuesta.setText("")
                        binding!!.etPregunta.requestFocus()
                    }
                    contadorPregunta++
                }
            }
        })*/

        // Hay que probar que elimine bien tanto en pregunta como en respuesta.
        // No funciona eliminando entre preguntas
        binding!!.imgvEliminar.setOnClickListener {
            AlertDialog.Builder(this@Activity_Cuestionario)
                .setTitle("¡Atención!")
                .setMessage("¿Quieres eliminar la pregunta?")
                .setPositiveButton("Si") { dialogInterface, i ->
                    // Se le quita 1 para hacer referencia al arreglo
                    // tamaño 3-1 = 2 [0,1,2].
                    val longi: Int = respuestas.size - 1

                    // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                    // de lo que esté en la posición 0.
                    if (contadorPregunta <= longi) {
                        preguntas.removeAt(contadorPregunta)
                        respuestas.removeAt(contadorPregunta)

                        // Solo si es mayor a 0 se resta, cuando se elimina una pregunta se acomoda
                        // el arreglo desde la posición 0 a la n
                        if (contadorPregunta != 0){
                            contadorPregunta--
                        }

                        pintarTexto(contadorPregunta)
                        binding!!.tilContenidoPregResp.hint = "Pregunta"
                    } else {
                        if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                            binding!!.etPregResp.setText("")
                        } else {
                            preguntas.removeAt(contadorPregunta)
                            binding!!.tilContenidoPregResp.hint = "Pregunta"
                            binding!!.etPregResp.setText("")
                        }

                        contadorPregunta--
                        pintarTexto(contadorPregunta)
                        binding!!.tilContenidoPregResp.hint = "Pregunta"
                    }

                    // Se le quita 1 para hacer referencia al arreglo
                    // tamaño 3-1 = 2 [0,1,2].
                    /*val longi: Int = respuestas.size - 1

                    // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                    // de lo que esté en la posición 0.
                    if (contadorPregunta <= longi) {
                        preguntas.removeAt(contadorPregunta)
                        respuestas.removeAt(contadorPregunta)

                        contadorPregunta--
                        pintarTexto(contadorPregunta)
                        binding!!.tilContenidoPregResp.hint = "Pregunta"
                    } else {
                        // Si el contadorPregunta es mayor entonces únicamente limpiamos los campos.
                        if (contadorPregunta == preguntas.size-1){
                            preguntas.removeAt(contadorPregunta)

                            if(contadorPregunta == respuestas.size-1){
                                respuestas.removeAt(contadorPregunta)
                            }
                            contadorPregunta--
                        }

                        pintarTexto(contadorPregunta)
                        binding!!.tilContenidoPregResp.hint = "Pregunta"
                    }*/
                }
                .setNegativeButton("Cancelar") { dialog, i ->
                    dialog.dismiss()
                }.create().show()
        }

        /*binding!!.btnEliminar.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                AlertDialog.Builder(this@Activity_Cuestionario)
                    .setTitle("¡Atención!")
                    .setMessage("¿Quieres eliminar la pregunta?")
                    .setPositiveButton("Si", object : DialogInterface.OnClickListener {
                        public override fun onClick(dialogInterface: DialogInterface, i: Int) {
                            // Se le quita 1 para hacer referencia al arreglo
                            // tamaño 3-1 = 2 [0,1,2].
                            val longi: Int = preguntas.size - 1

                            // contadorPregunta tendrá acceso a modificar lo que esté en el rango a excepción
                            // de lo que esté en la posición 0.
                            if (contadorPregunta <= longi) {
                                preguntas.removeAt(contadorPregunta)
                                respuestas.removeAt(contadorPregunta)

                                // Mientras el contadorPregunta sea menor escribiremos la siguiente pregunta
                                // en los et.
                                if (contadorPregunta < longi) {
                                    // Pintamos el texto en la pregunta actual
                                    pintarTexto(contadorPregunta)
                                } else {
                                    // Si el contadorPregunta es igual entonces solo escribiremos los campos vacios.
                                    binding!!.etPregunta.setText("")
                                    binding!!.etRespuesta.setText("")
                                    binding!!.etPregunta.requestFocus()
                                }
                            } else {
                                // Si el contadorPregunta es mayor entonces únicamente limpiamos los campos.
                                binding!!.etPregunta.setText("")
                                binding!!.etRespuesta.setText("")
                                binding!!.etPregunta.requestFocus()
                            }
                        }
                    })
                    .setNegativeButton("Cancelar", object : DialogInterface.OnClickListener {
                        public override fun onClick(dialog: DialogInterface, i: Int) {
                            dialog.dismiss()
                        }
                    }).create().show()
            }
        })*/

        binding!!.imgvSave.setOnClickListener {
            if (respuestas.size != 0) {
                // Se le quita 1 para hacer referencia al arreglo
                // tamaño 3-1 = 2 [0,1,2].
                val longi: Int = respuestas.size - 1

                if (contadorPregunta <= longi) {
                    if (binding!!.etPregResp.text.toString().isEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "Asegurate de llenar una pregunta y una respuesta",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        var editable: Editable =
                            Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
                        var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                            0,
                            editable.length,
                            ForegroundColorSpan::class.java
                        )

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)

                        if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                            preguntas[contadorPregunta] = editable.toString()
                        } else {
                            respuestas[contadorPregunta] = editable.toString()
                        }

                        crearArchivo(nombreArchivo)
                    }
                } else { // Si contadorPregunta es mayor a lo que hay en el arreglo.
                    if (binding!!.etPregResp.text.toString()
                            .isEmpty() && binding!!.tilContenidoPregResp.hint == "Pregunta"
                    ) {
                        crearArchivo(nombreArchivo)
                    } else if (binding!!.etPregResp.text.toString()
                            .isNotEmpty() && binding!!.tilContenidoPregResp.hint == "Pregunta" || binding!!.etPregResp.text.toString()
                            .isEmpty() && binding!!.tilContenidoPregResp.hint == "Respuesta"
                    ) {
                        Toast.makeText(
                            applicationContext,
                            "Asegurate de llenar una pregunta y una respuesta",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Cuando no hay guardadas las mismas preguntas que respuestas.
                        var editable: Editable =
                            Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
                        var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                            0,
                            editable.length,
                            ForegroundColorSpan::class.java
                        )

                        // Se colocan las etiquetas en cada palabra con color
                        colocarEtiquetas(colorSpans, editable)
                        respuestas.add(contadorPregunta, editable.toString())

                        crearArchivo(nombreArchivo)
                    }
                }
            } else if (binding!!.tilContenidoPregResp.hint == "Respuesta") {
                // Cuando no hay guardadas las mismas preguntas que respuestas.
                var editable: Editable =
                    Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
                var colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                    0,
                    editable.length,
                    ForegroundColorSpan::class.java
                )

                // Se colocan las etiquetas en cada palabra con color
                colocarEtiquetas(colorSpans, editable)
                respuestas.add(contadorPregunta, editable.toString())

                crearArchivo(nombreArchivo)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Ocupas como minimo crear una pregunta",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        /*binding!!.btnGuardarGuia.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                // Se le quita 1 para hacer referencia al arreglo
                // tamaño 3-1 = 2 [0,1,2].
                val longi: Int = preguntas.size - 1

                // Si alguno de los dos campos está vacio entra aquí.
                if (binding!!.etPregunta.text.toString().isNotEmpty() &&
                    binding!!.etRespuesta.text.toString().isEmpty() ||
                    binding!!.etPregunta.text.toString().isEmpty() &&
                    binding!!.etRespuesta.text.toString().isNotEmpty()
                ) {
                    Toast.makeText(
                        applicationContext,
                        "Asegurate de no dejar ningún campo vacio",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if ((binding!!.etPregunta.text.toString().isEmpty()
                            && binding!!.etRespuesta.text.toString().isEmpty())
                ) {
                    // Si los dos campos están vacios entra aquí.

                    // Si queremos guardar con campos vacios y no hay preguntas anteriores guardadas
                    // entra aquí.
                    if (contadorPregunta == 0) {
                        Toast.makeText(
                            applicationContext,
                            "¡No puedes guardar una guía sin datos!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Si queremos guardar con campos vacios y hay preguntas anteriores
                        // guardadas entra aquí.
                        crearArchivo(nombreArchivo)
                    }
                } else if (contadorPregunta < longi) {
                    // Si el contadorPregunta no es mayor a lo guardado entonces modificamos lo actual en
                    // el arreglo, además anteriormente ya validamos campos vacios.
                    var editable: Editable =
                        Editable.Factory.getInstance().newEditable(binding!!.etPregunta.text)
                    var colorSpans: Array<ForegroundColorSpan> =
                        editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)
                    preguntas[contadorPregunta] = editable.toString()
                    editable =
                        Editable.Factory.getInstance().newEditable(binding!!.etRespuesta.text)
                    colorSpans =
                        editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)
                    respuestas[contadorPregunta] = editable.toString()
                    crearArchivo(nombreArchivo)
                } else {
                    // Si el contadorPregunta es igual a lo guardado entonces agregamos la pregunta,
                    // anteriormente ya validamos campos vacios.
                    var editable: Editable =
                        Editable.Factory.getInstance().newEditable(binding!!.etPregunta.text)
                    var colorSpans: Array<ForegroundColorSpan> =
                        editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)
                    preguntas.add(contadorPregunta, editable.toString())
                    editable =
                        Editable.Factory.getInstance().newEditable(binding!!.etRespuesta.text)
                    colorSpans =
                        editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)

                    // Se colocan las etiquetas en cada palabra con color
                    colocarEtiquetas(colorSpans, editable)
                    respuestas.add(contadorPregunta, editable.toString())
                    crearArchivo(nombreArchivo)
                }
            }
        })*/

        /*binding!!.etPregunta.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                // Inflar el menú personalizado de color.
                actionMode.menuInflater.inflate(R.menu.menu_color, menu)

                // Inflar el menú personalizado sin color.
                // actionMode.getMenuInflater().inflate(R.menu.munu_sin_color, menu);
                return true
            }

            public override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                return false
            }

            public override fun onActionItemClicked(
                actionMode: ActionMode,
                menuItem: MenuItem
            ): Boolean {
                val start: Int
                val end: Int
                val text: Editable?
                val spannableStringBuilder: SpannableStringBuilder
                when (menuItem.itemId) {
                    R.id.color -> {
                        // Poner un color al rango marcado
                        start = binding!!.etPregunta.selectionStart
                        end = binding!!.etPregunta.selectionEnd
                        text = binding!!.etPregunta.text
                        spannableStringBuilder = SpannableStringBuilder(text)

                        // Obtén los spans aplicados
                        val spans: Array<ForegroundColorSpan> = spannableStringBuilder.getSpans(
                            0,
                            spannableStringBuilder.length,
                            ForegroundColorSpan::class.java
                        )

                        // Eliminar spans existentes que se superpongan con el nuevo rango
                        for (span: ForegroundColorSpan? in spans) {
                            val spanInicio: Int = spannableStringBuilder.getSpanStart(span)
                            val spanFin: Int = spannableStringBuilder.getSpanEnd(span)
                            if ((spanInicio < end && spanFin > start) || (spanInicio >= start && spanFin <= end)) {
                                spannableStringBuilder.removeSpan(span)
                                Toast.makeText(
                                    applicationContext,
                                    "Una letra, una tinta; palabras sin colores.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        spannableStringBuilder.setSpan(
                            ForegroundColorSpan(colorActual),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        binding!!.etPregunta.text = spannableStringBuilder
                        binding!!.etPregunta.setSelection(end)
                        return true
                    }

                    else -> return false
                }
            }

            public override fun onDestroyActionMode(actionMode: ActionMode) {}
        })*/

        /*binding!!.etRespuesta.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                // Inflar el menú personalizado de color.
                actionMode.menuInflater.inflate(R.menu.menu_color, menu)

                // Inflar el menú personalizado sin color.
                // actionMode.getMenuInflater().inflate(R.menu.munu_sin_color, menu);
                return true
            }

            public override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                return false
            }

            public override fun onActionItemClicked(
                actionMode: ActionMode,
                menuItem: MenuItem
            ): Boolean {
                val start: Int
                val end: Int
                val text: Editable?
                val spannableStringBuilder: SpannableStringBuilder
                when (menuItem.itemId) {
                    R.id.color -> {
                        // Poner un color al rango marcado
                        start = binding!!.etRespuesta.selectionStart
                        end = binding!!.etRespuesta.selectionEnd
                        text = binding!!.etRespuesta.text
                        spannableStringBuilder = SpannableStringBuilder(text)

                        // Obtén los spans aplicados
                        val spans: Array<ForegroundColorSpan> = spannableStringBuilder.getSpans(
                            0,
                            spannableStringBuilder.length,
                            ForegroundColorSpan::class.java
                        )

                        // Eliminar spans existentes que se superpongan con el nuevo rango
                        for (span: ForegroundColorSpan? in spans) {
                            val spanInicio: Int = spannableStringBuilder.getSpanStart(span)
                            val spanFin: Int = spannableStringBuilder.getSpanEnd(span)
                            if ((spanInicio < end && spanFin > start) || (spanInicio >= start && spanFin <= end)) {
                                spannableStringBuilder.removeSpan(span)
                                Toast.makeText(
                                    applicationContext,
                                    "Una letra, una tinta; palabras sin colores.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        spannableStringBuilder.setSpan(
                            ForegroundColorSpan(colorActual),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        binding!!.etRespuesta.text = spannableStringBuilder
                        binding!!.etRespuesta.setSelection(end)
                        return true
                    }

                    else -> return false
                }
            }

            public override fun onDestroyActionMode(actionMode: ActionMode) {}
        })*/

        /*binding!!.btnQuitarColores.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                binding!!.etPregunta.setText(binding!!.etPregunta.text.toString())
                binding!!.etRespuesta.setText(binding!!.etRespuesta.text.toString())
            }
        })*/
    }

    private fun girarCardView() {
        val flipAnimator =
            ObjectAnimator.ofFloat(binding!!.tilContenidoPregResp, "rotationY", 0f, 360f)
        flipAnimator.duration = 1000 // Duración de la animación en milisegundos
        flipAnimator.start()
    }

    // Método que se ejecuta cuando el back del telefono es presionado.
    public override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            cancelarArchivo()
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
                @SuppressLint("SdCardPath") val file: File =
                    File("/data/data/com.jonathanev.repasar/files/")
                if (file.exists()) {
                    File(file, "$nombreArchivo.xml").delete()
                    Log.d("ArchivoEliminado", "Archivo eliminado")
                } else {
                    Log.d("ArchivoEliminado", "Archivo no eliminado")
                }
                onBackPressed()
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
            Toast.makeText(
                applicationContext, "Guia de estudio creada exitosamente",
                Toast.LENGTH_SHORT
            ).show()
            val intent: Intent = Intent(applicationContext, Activity_RepasarGuia::class.java)
            intent.putExtra("nombre_archivo", nombreArchivo)
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

    /*fun colocarEtiquetas(colorSpans: Array<ForegroundColorSpan>, editable: Editable) {
        for (colorSpan: ForegroundColorSpan in colorSpans) {
            val start: Int = editable.getSpanStart(colorSpan)
            val end: Int = editable.getSpanEnd(colorSpan)
            val color: Int = colorSpan.foregroundColor

            // Agregar la etiqueta de inicio al texto
            val etiqIni: String = "«$color»"
            val etiqFin: String = "«/$color»"
            editable.replace(start, start, etiqIni)

            // Agregar la etiqueta de cierre al texto
            editable.replace(end + etiqIni.length, end + etiqIni.length, etiqFin)
        }
    }*/

    private fun pintarTexto(contadorPregunta: Int) {
        var contColorPreg: Int = 0
        var inicio: Int = 0
        var fin: Int = 0
        var coloresPregunta: ColoresPregunta? = null
        var texto: String = ""
        if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
            texto = preguntas[contadorPregunta]
        } else {
            texto = respuestas[contadorPregunta]
        }
        while (texto.contains("«")) {
            inicio = texto.indexOf("«") + 1
            fin = texto.indexOf("»")
            val color: String = texto.substring(inicio, fin)
            val longColor: Int = color.length
            val colEntero: Int = color.toInt()
            inicio = fin + 1
            fin = texto.indexOf("«", inicio)
            coloresPregunta =
                ColoresPregunta((inicio - longColor - 2), (fin - longColor - 2), colEntero)

            if (binding!!.tilContenidoPregResp.hint == "Pregunta") {
                preguntasColor.add(contColorPreg, coloresPregunta)
            } else {
                respuestasColor.add(contColorPreg, coloresPregunta)
            }

            // Eliminar la primera etiqueta y su contenido
            texto = texto.replaceFirst("«.*?»".toRegex(), "")

            // Eliminar la segunda etiqueta y su contenido
            texto = texto.replaceFirst("«.*?»".toRegex(), "")
            contColorPreg++
        }

        builder = SpannableStringBuilder(texto)
        for (coloresPreguntas: ColoresPregunta in if (binding!!.tilContenidoPregResp.hint == "Pregunta") preguntasColor else respuestasColor) {
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

    /*fun colorActual(colorActual: Int) {
        @SuppressLint("UseCompatLoadingForDrawables") val drawable: Drawable =
            resources.getDrawable(R.drawable.boton_redondo)
        drawable.setColorFilter(colorActual, PorterDuff.Mode.SRC_ATOP)
        binding!!.btnColorActual.background = drawable

        // Recibimos el nombre del archivo del popupFragment Nueva Guia.
        this.colorActual = colorActual
    }*/
}