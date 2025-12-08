package com.jonathanev.review.UI.View

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.UI.ViewModel.Fragments.MainToolbarViewModel
import com.jonathanev.review.UI.ViewModel.MainActivityViewModel
import com.jonathanev.review.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_PERMISSION_CODE = 123
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()
    private val viewModelToolbar: MainToolbarViewModel by viewModels()

    @Inject
    lateinit var filePathsProvider: FilePathsProvider

    @RequiresApi(api = Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        // Ejemplo: verificamos si ya tiene permiso
        val hasPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.checkIfNeedsPermission(hasPermission)

        initUI()
        observers()
        initListeners()
    }

    private fun initListeners() {
        binding.barraSuperiorBack.imgvSave.setOnClickListener {
            viewModelToolbar.btnSaveText()
        }
    }

    private fun observers() {
        // Revisar permisos, sino hay se solicitan
        viewModel.shouldRequestPermission.observe(this) { withoutPermission ->
            if (withoutPermission) requestReadPermission()
        }

        viewModelToolbar.title.observe(this){ title ->
            binding.barraSuperiorBack.tvTituloToolbar.text = title
        }

        viewModelToolbar.isSaveVisible.observe(this){ isVisibleBtnSave ->
            binding.barraSuperiorBack.imgvSave.visibility = isVisibleBtnSave
        }

        viewModelToolbar.isBackVisible.observe(this){ isVisibleBtnBack ->
            binding.barraSuperiorBack.imgvBack.visibility = isVisibleBtnBack
        }
    }

    private fun initUI() {
        viewModelToolbar.init()

        val foldersCreated = foldersGuides()

        if (foldersCreated) {
            viewModel.getAllFolders()
        }
    }

    private fun foldersGuides(): Boolean {
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