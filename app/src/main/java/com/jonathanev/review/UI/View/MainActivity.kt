package com.jonathanev.review.UI.View

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.R
import com.jonathanev.review.UI.View.Fragments.Fragment_DialogListarGuias_popup
import com.jonathanev.review.UI.View.Fragments.Fragment_DialogNuevoArchivo_popu
import com.jonathanev.review.UI.ViewModel.MainActivityViewModel
import com.jonathanev.review.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_PERMISSION_CODE = 123
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()
    private var carpetasImagenes = mutableListOf<String>()

    @Inject
    lateinit var filePathsProvider: FilePathsProvider

    // Array TEXTO donde guardaremos los nombres de los ficheros.
    var item: ArrayList<String> = ArrayList()

    @RequiresApi(api = Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ejemplo: verificamos si ya tiene permiso
        val hasPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.checkIfNeedsPermission(hasPermission)

        initUI()
        // Revisar permisos, sino hay se solicitan
        viewModel.shouldRequestPermission.observe(this){ withoutPermission ->
            if (withoutPermission) requestReadPermission()
        }


        // Utilizamos un botón que es reutilizado, unicamente le cambiamos el texto.
        binding.btnAbrirGuiaEstudioHabilitado.text = resources.getText(R.string.btnAbrirGuia)

        binding.btnNuevaGuiaEstudio.setOnClickListener { // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
            if (viewModel.getFoldersCreated()) {
                val dialogo: Fragment_DialogNuevoArchivo_popu = Fragment_DialogNuevoArchivo_popu()
                dialogo.show(supportFragmentManager, "Fragment_nuevo")

                // Creamos las preferencias y dentro de ellas guardamos el arreglo item
                var preferencias =
                    applicationContext.getSharedPreferences("cambiar_nombre", MODE_PRIVATE)
                var editor = preferencias.edit()
                editor.putString("cambiar_nombre", "no existe")
                editor.apply()

                // Creamos las preferencias y dentro de ellas guardamos el arreglo item
                preferencias =
                    applicationContext.getSharedPreferences(
                        "crear_folder",
                        MODE_PRIVATE
                    )

                editor = preferencias.edit()
                editor.putString("crear_folder", "no existe")
                editor.apply()
            }
        }

        binding.btnAbrirGuiaEstudioHabilitado.setOnClickListener {
            if (viewModel.getFoldersCreated()) {
                // Cuando lo abres cargas el repositorio principal
                viewModel.setCurrentPath()
                viewModel.getAllGuias(File(viewModel.getCurrentPath()))

                val dialogo = Fragment_DialogListarGuias_popup()
                dialogo.show(supportFragmentManager, "Fragment")
            }
        }

        viewModel.guias.observe(this){
            val a = it
            Log.i("a", it.toString())
        }
    }

    private fun initUI() {
        val foldersCreated = foldersGuides()

        // Crear subcarpetas para las imagenes
        if (foldersCreated) {
            for (subCarpeta in carpetasImagenes) {
                val rutaSubcarpeta =
                    filePathsProvider.buildFolder(filePathsProvider.fileImages, subCarpeta)

                // Vas creando y verificando que las carpetas se crean correctamente
                if (!rutaSubcarpeta.exists()) {
                    rutaSubcarpeta.mkdirs()
                }
            }

            //viewModel.setGuiasInProvider()
        }

        viewModel.foldersCreated(foldersCreated)
    }

    private fun foldersGuides(): Boolean{
        val foldersCreated = viewModel.createFolders()

        if (!foldersCreated) {
            alertWithoutFolders()
        }

        return foldersCreated
    }

    private fun alertWithoutFolders() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("No se pudieron crear los ficheros correctamente")

        // Agregar un botón para cerrar el diálogo
        builder.setPositiveButton("Reintentar") { dialog, _ ->
            foldersGuides()
            dialog.dismiss() // Cerrar el diálogo
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        // Evitar que el diálogo se cierre al tocar fuera de él o presionar el botón de atrás
        builder.setCancelable(false)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun requestReadPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_PERMISSION_CODE
        )
    }
}