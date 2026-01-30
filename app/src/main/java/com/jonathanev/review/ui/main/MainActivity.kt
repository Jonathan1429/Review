package com.jonathanev.review.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jonathanev.review.databinding.ActivityMainBinding
import com.jonathanev.review.presentation.event.MainUiEvent
import com.jonathanev.review.presentation.viewmodel.MainActivityViewModel
import com.jonathanev.review.presentation.viewmodel.MainToolbarViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    /*companion object {
        private const val REQUEST_PERMISSION_CODE = 123
    }*/

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()
    private val viewModelToolbar: MainToolbarViewModel by viewModels()

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

        //viewModel.checkIfNeedsPermission(hasPermission)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelToolbar.uiState.collect { uiState ->
                    binding.barraSuperiorBack.imgvBack.visibility =
                        if (uiState.showBack) View.VISIBLE else View.GONE
                    binding.barraSuperiorBack.imgvSave.visibility =
                        if (uiState.showSave) View.VISIBLE else View.GONE
                    binding.barraSuperiorBack.btnSuccess.visibility =
                        if (uiState.showSuccess) View.VISIBLE else View.GONE
                    binding.barraSuperiorBack.btnCancel.visibility =
                        if (uiState.showCancel) View.VISIBLE else View.GONE
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    when (event) {
                        MainUiEvent.ShowCreateFoldersError -> {
                            alertWithoutFolders()
                        }
                    }
                }
            }
        }

        initUI()
        observers()
        initListeners()
    }

    private fun initListeners() {
        binding.barraSuperiorBack.imgvSave.setOnClickListener {
            viewModelToolbar.btnSaveText()
        }

        binding.barraSuperiorBack.imgvBack.setOnClickListener {
            viewModelToolbar.btnBefore()
        }

        binding.barraSuperiorBack.btnCancel.setOnClickListener {
            viewModelToolbar.btnCancel()
        }

        binding.barraSuperiorBack.btnSuccess.setOnClickListener {
            viewModelToolbar.btnSuccess()
        }
    }

    private fun observers() {
        // Revisar permisos, sino hay se solicitan
        /*viewModel.shouldRequestPermission.observe(this) { withoutPermission ->
            if (withoutPermission) requestReadPermission()
        }*/

        viewModelToolbar.title.observe(this) { title ->
            binding.barraSuperiorBack.tvTituloToolbar.text = title
        }
    }

    private fun initUI() {
        viewModelToolbar.init()
        viewModel.createFolders()
    }

    private fun alertWithoutFolders() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("No se pudieron crear los ficheros correctamente")

        // Agregar un botón para cerrar el diálogo
        builder.setPositiveButton("Reintentar") { dialog, _ ->
            initUI()
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

    /*private fun requestReadPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_PERMISSION_CODE
        )
    }*/
}