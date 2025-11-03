package com.jonathanev.review.UI.View.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.jonathanev.review.Core.Constants.BASERUTA_IMG_CIFRADO
import com.jonathanev.review.Core.Constants.GUIAS
import com.jonathanev.review.Core.Constants.IMAGENES
import com.jonathanev.review.Core.Constants.PRINCIPAL
import com.jonathanev.review.Data.GuiaResult
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Fragments.Adaptadores.ListarGuiasAdapter
import com.jonathanev.review.R
import com.jonathanev.review.UI.View.ActivityModificar
import com.jonathanev.review.UI.View.ActivityRepasarGuia
import com.jonathanev.review.UI.View.Fragments.Fragment_DialogNuevoArchivo_popu.DialogListener
import com.jonathanev.review.UI.ViewModel.Fragments.FragDialListarGuiasViewModel
import com.jonathanev.review.databinding.FragmentListarGuiasBinding
import dagger.hilt.android.AndroidEntryPoint
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

@AndroidEntryPoint
class Fragment_DialogListarGuias_popup : DialogFragment(), DialogListener {
    private var _binding: FragmentListarGuiasBinding? = null
    private val binding get() = _binding!!

    private val guiasViewModel by viewModels<FragDialListarGuiasViewModel>()
    private lateinit var adaptadorListarGuias: ListarGuiasAdapter

    @Inject
    lateinit var filePathsProvider: FilePathsProvider

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

        guiasViewModel.guias.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.tvSinGuias.visibility = View.VISIBLE
                binding.lvGuiasEstudio.visibility = View.INVISIBLE
            } else {
                binding.tvSinGuias.visibility = View.INVISIBLE
                binding.lvGuiasEstudio.visibility = View.VISIBLE
                showGuias(it)
            }

            binding.progressBar.visibility = View.INVISIBLE
        }

        guiasViewModel.file.observe(this) {
            binding.progressBar.visibility = View.VISIBLE
            //guiasViewModel.getAllUpdatedGuides(it)
        }

        binding.LlFolders.setOnClickListener {
            if (binding.imgvBack.visibility == View.VISIBLE) {
                binding.imgvFolder.visibility = View.VISIBLE
                binding.tvNuevaCarpeta.visibility = View.VISIBLE
                binding.imgvBack.visibility = View.GONE
                binding.tvRegresar.visibility = View.GONE

                guiasViewModel.getFirstPath()
            } else {
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
                val dialogo = Fragment_DialogNuevoArchivo_popu()
                dialogo.show(parentFragmentManager, "Fragment_nuevo")
            }
        }
    }

    private fun initUI() {
        binding.progressBar.visibility = View.VISIBLE
        //guiasViewModel.getAllGuides()
        guiasViewModel.getAllGuias()
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
        val guiaResult = guiasViewModel.getGuia(position)
        //val a = guiasViewModel.guias.value
        //var fileClickeado: File = filePathsProvider.fileGuides

        when (guiaResult) {
            GuiaResult.Empty -> Log.i("Vacio", "Vacio")
            is GuiaResult.Error -> Log.i("Error", "Error")
            is GuiaResult.Success -> {
                if (guiaResult.guia.carpeta) { // fileClickeado.isDirectory
                    val builder = AlertDialog.Builder(context)
                    builder.setIcon(R.drawable.ic_advertencia)
                    builder.setTitle("¿Qué acción deseas realizar?")
                    builder.setItems(
                        arrayOf<CharSequence>(
                            "Abrir",
                            "Eliminar",
                            "Cancelar"
                        )
                    ) { dialog, which ->
                        when (which) {
                            0 -> {
                                guiasViewModel.changeFilePath(guiaResult.guia.nombreGuia)

                                binding.imgvFolder.visibility = View.GONE
                                binding.tvNuevaCarpeta.visibility = View.GONE
                                binding.imgvBack.visibility = View.VISIBLE
                                binding.tvRegresar.visibility = View.VISIBLE
                            }

                            1 -> {
                                // Se ejecuta cuando quiere eliminar la guía.
                                AlertDialog.Builder(context)
                                    .setTitle("¡Atención!")
                                    .setMessage(
                                        "¿Estás seguro que deseas eliminar la carpeta y su contenido?"
                                    )
                                    .setPositiveButton("Si") { _, _ ->
                                        deleteFiles(guiaResult.guia)
                                    }
                                    .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                                    .create().show()
                            }

                            2 -> {
                                // Cuando cancela se ejecuta esta acción
                                dialog.dismiss()
                                Toast.makeText(context, "Cancelaste la acción", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                    builder.create().show()
                } else {
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
                        //val ruta = "$fileClickeado.xml"
                        when (which) {
                            0 -> {
                                guiasViewModel.changeFilePath(guiaResult.guia.nombreGuia)

                                // Si entra al primer item se abre la guía a review
                                val intentAbrirGuia =
                                    Intent(activity, ActivityRepasarGuia::class.java)
                                //intentAbrirGuia.putExtra("nombre_archivo", guia.nombreGuia)
                                //intentAbrirGuia.putExtra("ruta", ruta)
                                startActivity(intentAbrirGuia)

                                // Recuperamos el dialogo abierto actualmente
                                // (Fragment_DialogListarGuias.java) y lo cerramos.
                                val dialogoAbrirGuia = getDialog()
                                dialogoAbrirGuia!!.dismiss()
                            }

                            1 -> {
                                // Si entra al segundo es para modificar la guía de estudio
                                guiasViewModel.changeFilePath(guiaResult.guia.nombreGuia)

                                val intentActivityModificarGuia =
                                    Intent(activity, ActivityModificar::class.java)
                                //intentModificarGuia.putExtra("nombre_archivo", guia.nombreGuia)
                                //intentActivityModificarGuia.putExtra("ruta", ruta)
                                startActivity(intentActivityModificarGuia)
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
                                        deleteFiles(guiaResult.guia)

                                        //guiasViewModel.changeFilePath(guiaResult.guia.nombreGuia)
                                        //val route = guiasViewModel.getCurrentPath()
                                        // Si entra al tercero es para eliminar la guia exitosamente
                                        /*if (filePathsProvider.fileGuides.exists()) {
                                            File(route).delete()
                                            Toast.makeText(
                                                context,
                                                "¡Archivo eliminado exitosamente!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            //guiasViewModel.getAllUpdatedGuides(filePathsProvider.fileGuides)

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
                                        }*/
                                    }
                                    .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                                    .create().show()

                            3 -> {
                                // PENDIENTEEEEEEE
                                // Se ejecuta cuando quiere cambiar el nombre de la guía
                                guiasViewModel.changeFilePath(guiaResult.guia.nombreGuia)
                                val route = guiasViewModel.getCurrentPath()
                                if (File(route).exists()) {
                                    // Creamos las preferencias y dentro de ellas guardamos el arreglo item
                                    /*val preferencias: SharedPreferences =
                                        requireContext().getSharedPreferences(
                                            "crear_folder",
                                            AppCompatActivity.MODE_PRIVATE
                                        )

                                    val editor = preferencias.edit()
                                    editor.putString("crear_folder", "cambiando_nombre")
                                    editor.putString("ruta", ruta)
                                    editor.apply()*/

                                    // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
                                    val dialogo = Fragment_DialogNuevoArchivo_popu()
                                    dialogo.show(childFragmentManager, "Fragment")
                                }
                            }

                            4 -> {
                                val subMenuBuilder = AlertDialog.Builder(context)
                                subMenuBuilder.setTitle("Mover a...")

                                val foldersCreated = guiasViewModel.getFoldersCreated()
                                val currentPath = guiasViewModel.getCurrentPath()

                                subMenuBuilder.setItems(foldersCreated) { _, subWhich ->
                                    // Manejar la selección de la carpeta dentro del submenú
                                    val selectedFolder = foldersCreated[subWhich]

                                    // Mover a la carpeta seleccionada
                                    try {
                                        /*// Copiar el archivo
                                        val guia = guiasViewModel.getGuia(position)*/

                                        // Updated path
                                        val fileName = "${guiaResult.guia.nombreGuia}.xml"
                                        val archivoEnCarpeta = filePathsProvider.buildFileFolder(
                                            File(filePathsProvider.fileGuides.toString()),
                                            selectedFolder,
                                            fileName
                                        )
                                        var creatingFile = false
                                        existingFile(archivoEnCarpeta) { creatingFile = it }

                                        try {
                                            val newPathWithoutFile: String =
                                                archivoEnCarpeta.toString().substringBeforeLast("/")
                                            var newPath: File = File(newPathWithoutFile)
                                            if (selectedFolder == PRINCIPAL) {
                                                newPath = filePathsProvider.fileGuides
                                            }

                                            if (creatingFile) {
                                                relocateGuideWithImages(
                                                    currentPath,
                                                    newPath,
                                                    fileName,
                                                    guiaResult,
                                                    newPathWithoutFile
                                                )
                                            }

                                            /*guiasViewModel.getFirstPath()
                                            guiasViewModel.getAllUpdatedGuides(
                                                filePathsProvider.fileGuides
                                            )*/

                                            binding.imgvFolder.visibility = View.VISIBLE
                                            binding.tvNuevaCarpeta.visibility = View.VISIBLE
                                            binding.imgvBack.visibility = View.GONE
                                            binding.tvRegresar.visibility = View.GONE

                                            Toast.makeText(
                                                context,
                                                "El archivo se movió correctamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } catch (e: Exception) {
                                            println("Error al copiar el archivo: ${e.message}")
                                        }

                                    } catch (e: Exception) {
                                        println("Error al copiar el archivo: ${e.message}")
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
            }
        }
    }

    // Creating file if it's possible
    private fun existingFile(archivoEnCarpeta: File, onResult: (Boolean) -> Unit) {
        if (!archivoEnCarpeta.exists()) {
            return onResult(true)
        }

        AlertDialog.Builder(context)
            .setTitle("¡Atención!")
            .setMessage(
                ("Ya tienes una guía con el mismo nombre, " +
                        "si continuas se va a sobreescribir la guia, " +
                        "¿seguro deseas continuar?")
            )
            .setPositiveButton(
                "Continuar"
            ) { _, _ ->
                onResult(true)
            }
            .setNegativeButton(
                "Cancelar"
            ) { dialog, _ ->
                dialog.dismiss()
                onResult(false)
            }.create().show()
    }

    private fun relocateGuideWithImages(
        currentPath: String,
        newCurrentPath: File,
        fileName: String,
        guiaResult: GuiaResult.Success,
        newPathWithoutFile: String
    ) {
        moverImagenes(File(currentPath), fileName, newPathWithoutFile)
        val currentPathWithFile = "${filePathsProvider.buildFile(File(currentPath), guiaResult.guia.nombreGuia)}.xml"

        Files.copy(
            Paths.get(currentPathWithFile),
            Paths.get(newCurrentPath.toString()),
            StandardCopyOption.REPLACE_EXISTING
        )

        // Borrar archivo
        File(currentPathWithFile).delete()
        //guiasViewModel.getAllUpdatedGuides(filePathsProvider.fileGuides)
        guiasViewModel.getFirstPath()

        Toast.makeText(
            context,
            "El archivo se movió correctamente",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun deleteFiles(guiaResult: GuiaModel) {
        if (!guiasViewModel.existFolder(guiaResult.nombreGuia)) {
            Toast.makeText(
                context,
                "La ruta para eliminar el archivo actualmente no existe.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val message = guiasViewModel.deleteFiles(guiaResult)

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val metrics = resources.displayMetrics

            val maxWidth = 900   // en píxeles
            val maxHeight = 1200 // en píxeles

            val calculatedWidth = (metrics.widthPixels * 0.9).toInt()  // 90% del ancho de pantalla
            val calculatedHeight = (metrics.heightPixels * 0.8).toInt() // 80% del alto de pantalla

            val finalWidth = minOf(calculatedWidth, maxWidth)
            val finalHeight = minOf(calculatedHeight, maxHeight)

            window.setLayout(finalWidth, finalHeight)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun moverImagenes(currentPath: File, fileName: String, newPathWithoutFile: String) {
        //obtenerDatosXML(fileClickeado, selectedFolder)
        //var doc: Document? = null
        val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder

        try {
            db = dbf.newDocumentBuilder()
            val filePath: File = File(currentPath.toString() + fileName)
            val fis = FileInputStream(filePath)

            val doc = db.parse(fis)

            // Buscamos los Nodos Interrogante y accedemos a lo que se encuentre dentro.
            val cuestionario: NodeList = doc.getElementsByTagName("Interrogante")
            for (i in 0 until cuestionario.length) {
                // Accedes a los elementos de dicho nodo
                val e: Element = cuestionario.item(i) as Element

                val value = mapOf(
                    "respuesta" to e.getAttribute("respuesta"),
                    "pregunta" to e.getAttribute("pregunta")
                ).filterValues { it.contains(BASERUTA_IMG_CIFRADO) }

                if (value.isNotEmpty()) {
                    value.keys.forEach { pathImage ->
                        //var descifrado = cifrar(pathImage, 26 - 3)
                        // descifrado = descifrado.replace("content://media/picker/".toRegex(), "")
                        val image = pathImage.substringAfterLast("/")
                        val ruta = currentPath.toString().replace(GUIAS, IMAGENES)
                        val pathImage = "$newPathWithoutFile/$image"

                        Files.copy(
                            Paths.get(ruta + image),
                            Paths.get("" + pathImage),
                            StandardCopyOption.REPLACE_EXISTING
                        )

                        File(ruta + image).delete()
                    }
                }
            }
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun cifrar(texto: String, desplazamiento: Int): String {
        val resultado = StringBuilder()

        for (caracter in texto) {
            if (caracter.isLetter()) {
                val base = if (caracter.isUpperCase()) 'A' else 'a'
                val letraCifrada = ((caracter - base + desplazamiento) % 26 + base.code).toChar()
                resultado.append(letraCifrada)
            } else {
                resultado.append(caracter)
            }
        }

        return resultado.toString()
    }
}