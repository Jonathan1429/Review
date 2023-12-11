package com.jonathanev.review

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.jonathanev.review.Fragments.Fragment_DialogListarGuias_popup
import com.jonathanev.review.Fragments.Fragment_DialogNuevoArchivo_popu
import com.jonathanev.review.databinding.ActivityMainBinding
import java.io.File

class MainActivity constructor() : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    // Array TEXTO donde guardaremos los nombres de los ficheros.
    var item: ArrayList<String> = ArrayList()
    @RequiresApi(api = Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

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

        binding!!.btnAbrirGuiaEstudioHabilitado.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                // Defino la ruta donde busco los ficheros.
                @SuppressLint("SdCardPath") val file: File =
                    File("/data/data/com.jonathanev.review/files/")

                // Limpio el item por si se borra algun archivo no se quede guardado.
                item.clear()
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
                    // Creo el array de tipo File con el contenido de la carpeta.
                    val files: Array<File> = file.listFiles()

                    // Hacemos un ciclo por cada fichero para extraer el nombre de cada uno.
                    for (i in files.indices) {
                        // Sacamos del array files el primer fichero.
                        val archivo: File = files[i]

                        if(archivo.name.contains(".xml")){
                            // Guardamos el nombre del fichero en la lista item.
                            item.add(archivo.name.replace(".xml".toRegex(), ""))
                        }
                    }

                    // Creamos las preferencias y dentro de ellas guardamos el arreglo item
                    val preferencias: SharedPreferences =
                        getSharedPreferences("nombres_guias", MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = preferencias.edit()
                    val set: MutableSet<String> = HashSet()
                    set.addAll(item)
                    editor.putStringSet("guias_estudio", set)
                    editor.commit()

                    // Teniendo todos los nombre de los archivos abrimos el dialogo.
                    //replaceFragment(Fragment_DialogListarGuias_popup())

                    val dialogo: Fragment_DialogListarGuias_popup =
                        Fragment_DialogListarGuias_popup()
                    dialogo.show(supportFragmentManager, "Fragment")
                }
            }
        })
    }

    /*private fun replaceFragment(guiasFragment: Fragment_DialogListarGuias_popup) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, guiasFragment)
        fragmentTransaction.commit()
    }*/
}