package com.jonathanev.review.UI.View

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jonathanev.review.Core.Constants.file
import com.jonathanev.review.Fragments.Fragment_DialogNuevoArchivo_popu
import com.jonathanev.review.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_PERMISSION_CODE = 123
    }

    private var binding: ActivityMainBinding? = null

    // Array TEXTO donde guardaremos los nombres de los ficheros.
    var item: ArrayList<String> = ArrayList()
    @RequiresApi(api = Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Revisar permisos
        checkAndRequestPermissions()

        // Utilizamos un botón que es reutilizado, unicamente le cambiamos el texto.
        binding!!.btnAbrirGuiaEstudioHabilitado.text = "Abrir Guia"
        binding!!.btnNuevaGuiaEstudio.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
                val dialogo: Fragment_DialogNuevoArchivo_popu = Fragment_DialogNuevoArchivo_popu()
                dialogo.show(getSupportFragmentManager(), "Fragment_nuevo")

                // Creamos las preferencias y dentro de ellas guardamos el arreglo item
                val preferencias: SharedPreferences =
                    applicationContext.getSharedPreferences("cambiar_nombre", MODE_PRIVATE)
                val editor: SharedPreferences.Editor = preferencias.edit()
                editor.putString("cambiar_nombre", "no existe")
                editor.commit()
            }
        })

        binding!!.btnAbrirGuiaEstudioHabilitado.setOnClickListener {
            if (!file.exists()) {
                if (file.mkdir()) {
                    Toast.makeText(
                        applicationContext,
                        "Ficheros creados correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Hubo un error al momento de crear los ficheros necesarios",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val dialogo = Fragment_DialogListarGuias_popup()
                dialogo.show(supportFragmentManager, "Fragment")
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
}