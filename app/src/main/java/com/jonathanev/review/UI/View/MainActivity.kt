package com.jonathanev.review.UI.View

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jonathanev.review.Core.Constants.file
import com.jonathanev.review.Core.Constants.fileImages
import com.jonathanev.review.Core.Constants.fileImagesPiv
import com.jonathanev.review.UI.ViewModel.MainActivityViewModel
import com.jonathanev.review.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_PERMISSION_CODE = 123
    }

    private var binding: ActivityMainBinding? = null
    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    private var carpetasImagenes = mutableListOf<String>()

    // Array TEXTO donde guardaremos los nombres de los ficheros.
    var item: ArrayList<String> = ArrayList()

    @RequiresApi(api = Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        initUI()

        // Revisar permisos
        checkAndRequestPermissions()

        // Utilizamos un botón que es reutilizado, unicamente le cambiamos el texto.
        binding!!.btnAbrirGuiaEstudioHabilitado.text = "Abrir Guia"
        binding!!.btnNuevaGuiaEstudio.setOnClickListener { // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
            if (createFolders()) {
                val dialogo: Fragment_DialogNuevoArchivo_popu = Fragment_DialogNuevoArchivo_popu()
                dialogo.show(getSupportFragmentManager(), "Fragment_nuevo")

                // Creamos las preferencias y dentro de ellas guardamos el arreglo item
                var preferencias =
                    applicationContext.getSharedPreferences("cambiar_nombre", MODE_PRIVATE)
                var editor = preferencias.edit()
                editor.putString("cambiar_nombre", "no existe")
                editor.commit()

                // Creamos las preferencias y dentro de ellas guardamos el arreglo item
                preferencias =
                    applicationContext.getSharedPreferences(
                        "crear_folder",
                        AppCompatActivity.MODE_PRIVATE
                    )

                editor = preferencias.edit()
                editor.putString("crear_folder", "no existe")
                editor.apply()
            }
        }

        binding!!.btnAbrirGuiaEstudioHabilitado.setOnClickListener {
            if (createFolders()) {
                mainActivityViewModel.getMainPath()

                val dialogo = Fragment_DialogListarGuias_popup()
                dialogo.show(supportFragmentManager, "Fragment")
            }
        }

        mainActivityViewModel.file.observe(this) {
            file = it
            initUI()
        }

        mainActivityViewModel.carpetas.observe(this){
            carpetasImagenes.addAll(it)
        }
    }

    // Create and validate folders
    private fun createFolders(): Boolean {
        var foldersCreated = false

        if (!file.exists()) {
            file.mkdir()
        }

        // Crear carpeta de imagenes
        if (!fileImages.exists()) {
            fileImages.mkdirs()
        }

        if (!fileImagesPiv.exists()){
            fileImagesPiv.mkdirs()
        }

        if (!file.exists() || !fileImagesPiv.exists() || !fileImages.exists()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Error")
            builder.setMessage("No se pudieron crear los ficheros correctamente")

            // Agregar un botón para cerrar el diálogo
            builder.setPositiveButton("Reintentar") { dialog, _ ->
                createFolders()
                dialog.dismiss() // Cerrar el diálogo
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

            // Evitar que el diálogo se cierre al tocar fuera de él o presionar el botón de atrás
            builder.setCancelable(false)

            val dialog: AlertDialog = builder.create()
            dialog.show()
        } else {
            // Crear subcarpetas para las imagenes
            for (subCarpeta in carpetasImagenes){
                var rutaSubcarpeta = File("$fileImages/$subCarpeta")

                // Vas creando y verificando que las carpetas se crean correctamente
                if (!rutaSubcarpeta.exists()) {
                    rutaSubcarpeta.mkdirs()
                    if (!rutaSubcarpeta.exists()){
                        foldersCreated = false
                        break
                    } else {
                        foldersCreated = true
                    }
                } else {
                    foldersCreated = true
                }
            }
        }

        return foldersCreated
    }

    private fun initUI() {
        mainActivityViewModel.getAllFolders(file)

        if (createFolders()) {
            mainActivityViewModel.getAllGuias(file)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionReadExternalStorage = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
            // Si no tienes permisos, solicítalos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_CODE
            )
        }
    }
}