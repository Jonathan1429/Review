package com.jonathanev.review.UI.View

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.jonathanev.review.Core.Constants
import com.jonathanev.review.Core.Constants.file
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Fragments.Adaptadores.ListarGuiasAdapter
import com.jonathanev.review.Fragments.Fragment_DialogNuevoArchivo_popu
import com.jonathanev.review.Fragments.Fragment_DialogNuevoArchivo_popu.DialogListener
import com.jonathanev.review.R
import com.jonathanev.review.UI.ViewModel.FragDialListarGuiasViewModel
import com.jonathanev.review.databinding.FragmentListarGuiasBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@AndroidEntryPoint
class Fragment_DialogListarGuias_popup : DialogFragment(), DialogListener {
    private var _binding: FragmentListarGuiasBinding? = null
    private val binding get() = _binding!!

    private val guiasViewModel by viewModels<FragDialListarGuiasViewModel>()
    private lateinit var adaptadorListarGuias: ListarGuiasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentListarGuiasBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDialogClosed() {
        // Realizar las acciones necesarias después de cerrar el diálogo
        val dialogActual = dialog
        dialogActual!!.dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()

        guiasViewModel.guias.observe(this) {
            showGuias(it)
        }

        binding.imgvNextFolder.setOnClickListener {
            val subMenuBuilder = AlertDialog.Builder(context)
            subMenuBuilder.setTitle("Selecciona la carpeta")

            // val ruta = File("/data/data/com.jonathanev.review/files/")
            val listaCarpetas = file.listFiles { file -> file.isDirectory }

            //val nombresCarpetas = arrayOf<CharSequence>()

            if (listaCarpetas != null) {
                // Método 1: Usando copyOf()
                val nombresCarpetas = listaCarpetas.map { it.name }.toTypedArray()

                subMenuBuilder.setItems(nombresCarpetas) { _, subWhich ->
                    // Manejar la selección de la carpeta dentro del submenú
                    val selectedFolder = nombresCarpetas[subWhich]

                    getGuiasCarpetaSeleccionada(selectedFolder)
                }
            }

            subMenuBuilder.show()
        }

        binding.imgvFolder.setOnClickListener {
            // Creamos las preferencias y dentro de ellas guardamos el arreglo item
            val preferencias: SharedPreferences =
                requireContext().getSharedPreferences(
                    "crear_folder",
                    AppCompatActivity.MODE_PRIVATE
                )

            val editor = preferencias.edit()
            editor.putString("crear_folder", "creando_folder")
            editor.apply()

            // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
            val dialogo: Fragment_DialogNuevoArchivo_popu = Fragment_DialogNuevoArchivo_popu()
            dialogo.show(parentFragmentManager, "Fragment_nuevo")

            /*preferencias = requireContext().getSharedPreferences(
                "cambiar_nombre",
                AppCompatActivity.MODE_PRIVATE
            )

            editor = preferencias.edit()
            editor.putString("cambiar_nombre", "")
            editor.commit()*/
        }
    }

    private fun initUI() {
        guiasViewModel.getAllGuias()
    }

    private fun getGuiasCarpetaSeleccionada(carpetaSeleccionada: String){
        guiasViewModel.getAllGuiasCarpetaSeleccionada(carpetaSeleccionada)
    }

    private fun showGuias(guiaModels: List<GuiaModel>) {
        adaptadorListarGuias =
            ListarGuiasAdapter(guiaModels) { position -> showGuiaOptions(position) }
        binding.lvGuiasEstudio.layoutManager = LinearLayoutManager(context)
        binding.lvGuiasEstudio.setHasFixedSize(true)
        binding.lvGuiasEstudio.adapter = adaptadorListarGuias
    }


    @SuppressLint("SdCardPath")
    private fun showGuiaOptions(position: Int) {
        // Creo una alerta donde me saldrán una lista de items
        val builder = AlertDialog.Builder(context)
        builder.setIcon(R.drawable.ic_advertencia)
        builder.setTitle("¿Qué acción deseas realizar?")
        builder.setItems(
            arrayOf<CharSequence>(
                "Abrir",
                "Modificar",
                "Eliminar",
                "Cambiar nombre",
                "Mover a",
                "Cancelar"
            )
        ) { dialog, which ->
            when (which) {
                0 -> {
                    // Si entra al primer item se abre la guía a review
                    val intentAbrirGuia = Intent(activity, Activity_RepasarGuia::class.java)
                    //intentAbrirGuia.putExtra("nombre_archivo", guia.nombreGuia)
                    intentAbrirGuia.putExtra("file_position", position)
                    startActivity(intentAbrirGuia)
                    // Recuperamos el dialogo abierto actualmente
                    // (Fragment_DialogListarGuias.java) y lo cerramos.
                    val dialogoAbrirGuia = getDialog()
                    dialogoAbrirGuia!!.dismiss()
                }

                1 -> {
                    // Si entra al segundo es para modificar la guía de estudio
                    val intentModificarGuia =
                        Intent(activity, Activity_Modificar::class.java)
                    //intentModificarGuia.putExtra("nombre_archivo", guia.nombreGuia)
                    intentModificarGuia.putExtra("file_position", position)
                    startActivity(intentModificarGuia)
                    // Recuperamos el dialogo abierto actualmente
                    // (Fragment_DialogListarGuias.java) y lo cerramos.
                    val dialogoModificarGuia = getDialog()
                    dialogoModificarGuia!!.dismiss()
                }

                2 ->
                    // Se ejecuta cuando quiere eliminar la guía.
                    AlertDialog.Builder(context)
                        .setTitle("¡Atención!")
                        .setMessage(
                            "¿Estás seguro que deseas eliminar la" +
                                    " guia?"
                        )
                        .setPositiveButton("Si") { _, _ ->
                            // Si entra al tercero es para eliminar la guia exitosamente
                            if (file.exists()) {
                                val guia = guiasViewModel.getGuia(position)

                                File(file, guia.nombreGuia + ".xml").delete()
                                Toast.makeText(
                                    context,
                                    "¡Archivo eliminado exitosamente!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Recuperamos el dialogo abierto actualmente
                                // (Fragment_DialogListarGuias_popup.java)
                                // y lo cerramos.
                                val dialogoEliminarGuia = getDialog()
                                dialogoEliminarGuia!!.dismiss()
                            } else {
                                Toast.makeText(
                                    context, "La ruta para eliminar el " +
                                            "archivo actualmente no existe.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                        .create().show()

                3 -> {
                    // Se ejecuta cuando quiere cambiar el nombre de la guía
                    if (file.exists()) {
                        // Creamos las preferencias y dentro de ellas guardamos el arreglo item
                        val preferencias: SharedPreferences =
                            requireContext().getSharedPreferences(
                                "crear_folder",
                                AppCompatActivity.MODE_PRIVATE
                            )

                        val editor = preferencias.edit()
                        editor.putString("crear_folder", "cambiando_nombre")
                        editor.apply()

                        // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
                        val dialogo = Fragment_DialogNuevoArchivo_popu()
                        dialogo.show(childFragmentManager, "Fragment")
                    } else {
                        Toast.makeText(
                            context, "La ruta para cambiar " +
                                    "el nombre del archivo actualmente no existe.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                4 -> {
                    val subMenuBuilder = AlertDialog.Builder(context)
                    subMenuBuilder.setTitle("Selecciona la carpeta")

                    // val ruta = File("/data/data/com.jonathanev.review/files/")
                    val listaCarpetas = file.listFiles { file -> file.isDirectory }

                    //val nombresCarpetas = arrayOf<CharSequence>()

                    if (listaCarpetas != null) {
                        // Método 1: Usando copyOf()
                        val nombresCarpetas = listaCarpetas.map { it.name }.toTypedArray()

                        subMenuBuilder.setItems(nombresCarpetas) { _, subWhich ->
                            // Manejar la selección de la carpeta dentro del submenú
                            val selectedFolder = nombresCarpetas[subWhich]

                            // Mover a la carpeta seleccionada
                            try {
                                // Copiar el archivo
                                val guia = guiasViewModel.getGuia(position)

                                Files.copy(
                                    Paths.get("" + file + "/" + guia.nombreGuia + ".xml"),
                                    Paths.get("" + file + "/" + selectedFolder + "/" + guia.nombreGuia + ".xml"),
                                    StandardCopyOption.REPLACE_EXISTING
                                )

                                // Borrar archivo
                                File(file, guia.nombreGuia + ".xml").delete()

                                Toast.makeText(
                                    context, "El archivo se copió correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                println("Error al copiar el archivo: ${e.message}")
                            }
                        }
                    }

                    subMenuBuilder.show()
                }

                5 -> {
                    // Cuando cancela se ejecuta esta acción
                    dialog.dismiss()
                    Toast.makeText(context, "Cancelaste la acción", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        builder.create().show()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(900, 1200)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}