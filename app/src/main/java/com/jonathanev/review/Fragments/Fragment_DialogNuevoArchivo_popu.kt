package com.jonathanev.review.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.jonathanev.review.Core.Constants.file
import com.jonathanev.review.UI.View.Activity_Cuestionario
import com.jonathanev.review.databinding.FragmentNuevoArchivoBinding
import java.io.File
import java.nio.file.Files

class Fragment_DialogNuevoArchivo_popu() : DialogFragment() {
    private var binding: FragmentNuevoArchivoBinding? = null

    interface DialogListener {
        fun onDialogClosed()
    }

    private var listener: DialogListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = parentFragment as DialogListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(requireParentFragment().javaClass.toString() + " debe implementar la interfaz DialogListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    // En algún lugar del código donde cierres el dialogo, notifica al fragmento padre
    private fun cerrarDialogo() {
        if (listener != null) {
            listener!!.onDialogClosed()
        }
        dismiss()
    }

    private fun cerrarTodosDialogos() {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragments = fragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is DialogFragment) {
                fragment.dismiss()
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNuevoArchivoBinding.inflate(layoutInflater)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferenciasFolder =
            requireActivity().getSharedPreferences("crear_folder", Context.MODE_PRIVATE)

        val lv_folder = preferenciasFolder.getString("crear_folder", "no existe")
        val lv_ruta = preferenciasFolder.getString("ruta", "no existe")

        if (lv_folder.equals("creando_folder")) {
            binding!!.tilNombreArchivo.hint = "Nombre de la carpeta"
            binding!!.btnGuardarGuiaEstudio.text = "Crear carpeta"
        } else if (lv_folder.equals("cambiando_nombre")) {
            binding!!.btnGuardarGuiaEstudio.text = "Cambiar nombre"
        } else if (lv_folder.equals("cambiar_nom_folder")) {
            binding!!.tilNombreArchivo.hint = "Nombre de la carpeta"
            binding!!.btnGuardarGuiaEstudio.text = "Cambiar nombre"
        } else {
            binding!!.btnGuardarGuiaEstudio.text = "Guardar guía"
        }

        binding!!.btnGuardarGuiaEstudio.setOnClickListener {
            if (lv_folder.equals("cambiando_nombre")) {
                if (!binding!!.etNombreArchivo.text.toString().isEmpty()) {
                    // Ruta + nombre del archivo.
                    val rutaSinArchivo = lv_ruta.toString().substringBeforeLast("/")
                    //val archivo = lv_ruta?.replace(rutaSinArchivo + "/".toRegex(), "")
                    //var archivo = File("" + lv_ruta)
                    file = File(rutaSinArchivo)
                    if (file.exists()) {
                        var item = ""
                        // Defino la ruta donde busco los ficheros.
                        var archivoExiste = false
                        // Creo el array de tipo File con el contenido de la carpeta.
                        val files = file.listFiles()
                        var archivos: File? = null

                        // Hacemos un ciclo por cada fichero para extraer el nombre uno a uno.
                        if (!files.isNullOrEmpty()) {
                            for (i in files.indices) {
                                // Sacamos del array files el nombre recuperandolo por posición.
                                archivos = files[i]
                                if (!archivos.isDirectory) {
                                    item = archivos.getName().replace(".xml".toRegex(), "")
                                    // Comparamos el texto ingresado en la App con el recuperado.
                                    if ((binding!!.etNombreArchivo.text.toString() == item)) {
                                        archivoExiste = true
                                        break
                                    }
                                }
                            }
                        }

                        // Si hay un archivo existente entra
                        if (archivoExiste) {
                            // Se ejecuta cuando se regresa sin guardar.
                            AlertDialog.Builder(context)
                                .setTitle("¡Atención!")
                                .setMessage(
                                    ("Ya tienes un archivo con el mismo nombre, " +
                                            "si continuas se va a sobreescribir el archivo, " +
                                            "¿seguro deseas continuar?")
                                )
                                .setPositiveButton(
                                    "Continuar"
                                ) { _, _ -> // Si el usuario quiere continuar reemplazamos el archivo.
                                    val nuevoArchivo =
                                        File("$rutaSinArchivo/${binding!!.etNombreArchivo.text.toString()}.xml")
                                    val rutaRenombrar = File(lv_ruta.toString())
                                    if (rutaRenombrar.renameTo(nuevoArchivo)) {
                                        Toast.makeText(
                                            context, "Archivo renombrado exitosamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        cerrarTodosDialogos()
                                    } else {
                                        Toast.makeText(
                                            context, "No se pudo renombrar el archivo",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        cerrarDialogo()
                                    }
                                }
                                .setNegativeButton(
                                    "Cancelar"
                                ) { dialog, _ -> dialog.dismiss() }.create().show()
                        } else {
                            // Si el archivo no existe directamente pasamos a cambiar el nombre.
                            val nuevoArchivo =
                                File("$rutaSinArchivo/${binding!!.etNombreArchivo.text.toString()}.xml")
                            val rutaRenombrar = File(lv_ruta.toString())
                            if (rutaRenombrar.renameTo(nuevoArchivo)) {
                                Toast.makeText(
                                    context, "Archivo renombrado exitosamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                cerrarDialogo()
                            } else {
                                Toast.makeText(
                                    context, "No se pudo renombrar el archivo",
                                    Toast.LENGTH_SHORT
                                ).show()
                                cerrarDialogo()
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        context, "Ingresa un nuevo nombre",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (lv_folder.equals("creando_folder")) {
                if (binding!!.etNombreArchivo.text.toString().isEmpty()) {
                    Toast.makeText(
                        context, "Ingresa un nombre",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val folderName = binding!!.etNombreArchivo.text

                    // Asegúrate de obtener la ruta absoluta del directorio
                    val directorioAbsoluto = file.absolutePath

                    // Concatenar el directorio absoluto con el nombre de la carpeta
                    val rutaCompletaCarpeta = "$directorioAbsoluto/$folderName/"

                    // Crear un objeto File con la ruta completa de la carpeta
                    val folder = File(rutaCompletaCarpeta)

                    if (!folder.exists()) {
                        val created = folder.mkdirs()
                        if (created) {
                            Toast.makeText(
                                context, "Carpeta creada exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            //cerrarDialogo()
                            cerrarTodosDialogos()
                        } else {
                            Toast.makeText(
                                context, "No se pudo crear la carpeta exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            cerrarDialogo()
                        }
                    } else {
                        Toast.makeText(
                            context, "La carpeta ya existe",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                @SuppressLint("SdCardPath")
                var item = ""
                // Defino la ruta donde busco los ficheros.
                var archivoExiste = false
                if (file.exists()) {
                    // Creo el array de tipo File con el contenido de la carpeta.
                    val files = file.listFiles()
                    var archivo: File? = null
                    // Hacemos un ciclo por cada fichero para extraer el nombre uno a uno.
                    for (i in files.indices) {
                        // Sacamos del array files el nombre recuperandolo por posición.
                        archivo = files[i]
                        item = archivo.name.replace(".xml".toRegex(), "")
                        // Comparamos el texto ingresado en la App con el recuperado.
                        if ((binding!!.etNombreArchivo.text.toString() == item)) {
                            archivoExiste = true
                            break
                        }
                    }

                    // Si hay un archivo existente entra
                    if (archivoExiste) {
                        // Se ejecuta cuando se regresa sin guardar.
                        AlertDialog.Builder(context)
                            .setTitle("¡Atención!")
                            .setMessage(
                                ("Ya tienes una guía con el mismo nombre, " +
                                        "si continuas se va a sobreescribir el archivo, " +
                                        "¿seguro deseas continuar?")
                            )
                            .setPositiveButton(
                                "Continuar",
                                object : DialogInterface.OnClickListener {
                                    override fun onClick(
                                        dialogInterface: DialogInterface,
                                        i: Int
                                    ) {
                                        // Si hay un valor dentro del campo enviamos el nombre del archivo a
                                        // Activity_Cuestionario.
                                        val intent =
                                            Intent(activity, Activity_Cuestionario::class.java)
                                        intent.putExtra(
                                            "nombre_archivo",
                                            binding!!.etNombreArchivo.text.toString()
                                        )
                                        // Recuperamos el dialogo abierto actualmente
                                        // (Fragment_DialogNuevoArchivo.java) y lo cerramos.
                                        val dialogActual = dialog
                                        dialogActual!!.dismiss()
                                        binding!!.etNombreArchivo.setText("")
                                        startActivity(intent)
                                    }
                                })
                            .setNegativeButton(
                                "Cancelar",
                                object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface, i: Int) {
                                        dialog.dismiss()
                                    }
                                }).create().show()
                    } else { // Sino hay un archivo existente entra aquí
                        // Si hay un valor dentro del campo enviamos el nombre del archivo a
                        // Activity_Cuestionario.
                        if (binding!!.etNombreArchivo.text.toString().isEmpty()) {
                            Toast.makeText(
                                context,
                                "Tienes que colocar un nombre para el archivo",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val intent = Intent(activity, Activity_Cuestionario::class.java)
                            intent.putExtra(
                                "nombre_archivo",
                                binding!!.etNombreArchivo.text.toString()
                            )
                            // Recuperamos el dialogo abierto actualmente
                            // (Fragment_DialogNuevoArchivo.java) y lo cerramos.
                            val dialogActual = dialog
                            dialogActual!!.dismiss()
                            binding!!.etNombreArchivo.setText("")
                            startActivity(intent)
                        }
                    }
                }
            }
        }
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
            dialog.window!!.setLayout(700, 700)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}