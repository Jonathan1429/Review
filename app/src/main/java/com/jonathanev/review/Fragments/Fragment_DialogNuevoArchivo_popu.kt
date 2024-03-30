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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNuevoArchivoBinding.inflate(layoutInflater)
        return binding!!.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*val preferencias =
            requireActivity().getSharedPreferences("cambiar_nombre", Context.MODE_PRIVATE)*/
        val preferenciasFolder =
            requireActivity().getSharedPreferences("crear_folder", Context.MODE_PRIVATE)

        /*val lv_archivo = preferencias.getString("cambiar_nombre", "no existe")*/
        val lv_folder = preferenciasFolder.getString("crear_folder", "no existe")

        /*if (a.equals("sin nombre")) {
            binding!!.btnGuardarGuiaEstudio.text = "Cambiar nombre"
            binding!!.btnGuardarGuiaEstudio.setOnClickListener {
                if (!binding!!.etNombreArchivo.text.toString().isEmpty()) {
                    // Ruta + nombre del archivo.
                    val preferencias =
                        requireActivity().getSharedPreferences("nombre_archivo", Context.MODE_PRIVATE)

                    val archivo = File(
                        "" + file +
                                preferencias.getString("nombre_archivo", "no existe")
                                + ".xml"
                    )
                    if (archivo.exists()) {
                        var item = ""
                        // Defino la ruta donde busco los ficheros.
                        var archivoExiste = false
                        // Creo el array de tipo File con el contenido de la carpeta.
                        val files = file.listFiles()
                        var archivos: File? = null
                        // Hacemos un ciclo por cada fichero para extraer el nombre uno a uno.
                        for (i in files.indices) {
                            // Sacamos del array files el nombre recuperandolo por posición.
                            archivos = files[i]
                            item = archivos.getName().replace(".xml".toRegex(), "")
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
                                    DialogInterface.OnClickListener { _, _ -> // Si el usuario quiere continuar reemplazamos el archivo.
                                        val nuevoArchivo = File(
                                            archivo.parentFile,
                                            binding!!.etNombreArchivo.text.toString() + ".xml"
                                        ) // Creamos el nuevo objeto File
                                        if (archivo.renameTo(nuevoArchivo)) {
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
                                    })
                                .setNegativeButton(
                                    "Cancelar",
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(dialog: DialogInterface, i: Int) {
                                            dialog.dismiss()
                                        }
                                    }).create().show()
                        } else {
                            // Si el archivo no existe directamente pasamos a cambiar el nombre.
                            val nuevoArchivo = File(
                                archivo.parentFile,
                                binding!!.etNombreArchivo.text.toString() + ".xml"
                            ) // Creamos el nuevo objeto File
                            if (archivo.renameTo(nuevoArchivo)) {
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
            }
        } else {
            binding!!.btnGuardarGuiaEstudio.text = "Guardar guía"
            binding!!.btnGuardarGuiaEstudio.setOnClickListener(object : View.OnClickListener {
                @SuppressLint("SdCardPath")
                override fun onClick(view: View) {
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
            })
        }*/

        if (lv_folder.equals("creando_folder")) {
            binding!!.tilNombreArchivo.hint = "Nombre de la carpeta"

            binding!!.btnGuardarGuiaEstudio.text = "Crear carpeta"
        } else if (lv_folder.equals("cambiando_nombre")) {
            binding!!.btnGuardarGuiaEstudio.text = "Cambiar nombre"
        } else {
            binding!!.btnGuardarGuiaEstudio.text = "Guardar guía"
        }

        binding!!.btnGuardarGuiaEstudio.setOnClickListener {
            if (lv_folder.equals("cambiando_nombre")) {
                if (!binding!!.etNombreArchivo.text.toString().isEmpty()) {
                    // Ruta + nombre del archivo.
                    /*val preferencias =
                        requireActivity().getSharedPreferences(
                            "nombre_archivo",
                            Context.MODE_PRIVATE
                        )*/

                    var archivo = File("" + file)
                    if (archivo.exists()) {
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
                                item = archivos.getName().replace(".xml".toRegex(), "")
                                // Comparamos el texto ingresado en la App con el recuperado.
                                if ((binding!!.etNombreArchivo.text.toString() == item)) {
                                    archivoExiste = true
                                    break
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
                                    "Continuar",
                                    DialogInterface.OnClickListener { _, _ -> // Si el usuario quiere continuar reemplazamos el archivo.
                                        val nuevoArchivo =
                                            File("$file/${binding!!.etNombreArchivo.text.toString()}.xml")
                                        if (archivos?.renameTo(nuevoArchivo) == true) {
                                            Toast.makeText(
                                                context, "Archivo renombrado exitosamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            cerrarDialogo()
                                            cerrarDialogo()
                                        } else {
                                            Toast.makeText(
                                                context, "No se pudo renombrar el archivo",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            cerrarDialogo()
                                        }
                                    })
                                .setNegativeButton(
                                    "Cancelar"
                                ) { dialog, _ -> dialog.dismiss() }.create().show()
                        } else {
                            // Si el archivo no existe directamente pasamos a cambiar el nombre.
                            val nuevoArchivo =
                                File("$file/${binding!!.etNombreArchivo.text.toString()}.xml")
                            if (archivos?.renameTo(nuevoArchivo) == true) {
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
                            cerrarDialogo()
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