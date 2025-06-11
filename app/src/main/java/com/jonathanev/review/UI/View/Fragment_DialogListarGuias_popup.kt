package com.jonathanev.review.UI.View

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.jonathanev.review.Core.Constants.file
import com.jonathanev.review.Core.Constants.fileImages
import com.jonathanev.review.Core.Constants.path
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Fragments.Adaptadores.ListarGuiasAdapter
import com.jonathanev.review.R
import com.jonathanev.review.UI.View.Fragment_DialogNuevoArchivo_popu.DialogListener
import com.jonathanev.review.UI.ViewModel.FragDialListarGuiasViewModel
import com.jonathanev.review.databinding.FragmentListarGuiasBinding
import dagger.hilt.android.AndroidEntryPoint
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

@AndroidEntryPoint
class Fragment_DialogListarGuias_popup : DialogFragment(), DialogListener {
    private var _binding: FragmentListarGuiasBinding? = null
    private val binding get() = _binding!!

    private val guiasViewModel by viewModels<FragDialListarGuiasViewModel>()
    private lateinit var adaptadorListarGuias: ListarGuiasAdapter
    private val elementosPorCarga = 10 // Cantidad de elementos a cargar por cada carga adicional
    private var numeroElementosCargados = 0 // Número actual de elementos cargados

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

        adaptadorListarGuias =
            ListarGuiasAdapter { position -> showGuiaOptions(position) }

        binding.lvGuiasEstudio.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adaptadorListarGuias
            setHasFixedSize(true)
            itemAnimator = null // Opcional: remueve animaciones que pueden causar lag
        }

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
            guiasViewModel.getAllUpdatedGuides(it)
        }

        // Agregar un OnScrollListener al RecyclerView para detectar cuando el usuario se desplaza.
        /*binding.lvGuiasEstudio.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItems = layoutManager.itemCount
                val ultimoVisible = layoutManager.findLastVisibleItemPosition()

                // Si el último elemento visible es igual al total de elementos menos uno (es decir, estamos cerca del final de la lista).
                if (ultimoVisible == totalItems - 1) {
                    cargarElementos(guiaModels)
                }
            }
        })*/

        binding.LlFolders.setOnClickListener {
            if (binding.imgvBack.visibility == View.VISIBLE) {
                binding.imgvFolder.visibility = View.VISIBLE
                binding.tvNuevaCarpeta.visibility = View.VISIBLE
                binding.imgvBack.visibility = View.GONE
                binding.tvRegresar.visibility = View.GONE

                guiasViewModel.getMainPath()
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
        //guiasViewModel.getMainPath()
        guiasViewModel.getAllGuias()
    }

    private fun showGuias(guiaModels: List<GuiaModel>) {
        adaptadorListarGuias.submitList(guiaModels)

        /*val adaptador = ListarGuiasAdapter(guiaModels) { position ->
            showGuiaOptions(position)
        }
        with(binding.lvGuiasEstudio) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = adaptador
        }*/

        adaptadorListarGuias =
            ListarGuiasAdapter(guiaModels) { position -> showGuiaOptions(position) }
        binding.lvGuiasEstudio.layoutManager = LinearLayoutManager(context)
        binding.lvGuiasEstudio.setHasFixedSize(true)
        binding.lvGuiasEstudio.adapter = adaptadorListarGuias
        /*cargarElementos(guiaModels)*/
    }

    /*private fun cargarElementos(guiaModels: List<GuiaModel>) {
        // Calcula la próxima sección de elementos a cargar
        val nuevosDatos = guiaModels.subList(numeroElementosCargados, minOf(numeroElementosCargados + elementosPorCarga, guiaModels.size))

        // Agrega los nuevos datos al adaptador
        adaptadorListarGuias.agregarGuias(nuevosDatos)

        // Incrementa el contador de elementos cargados
        numeroElementosCargados += nuevosDatos.size
    }*/


    @SuppressLint("SdCardPath")
    private fun showGuiaOptions(position: Int) {
        val guia = guiasViewModel.getGuia(position)

        var fileClickeado: File = file

        guiasViewModel.file.value?.let {
            fileClickeado = it
        }

        fileClickeado = File("" + fileClickeado + "/" + guia.nombreGuia)

        if (guia.carpeta) { // fileClickeado.isDirectory
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
                //guiasViewModel.getMainPath()
                when (which) {
                    0 -> {
                        changePath(guia.nombreGuia)

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
                                // Si entra al tercero es para eliminar la guia exitosamente
                                if (fileClickeado!!.exists()) {
                                    val exito = deleteFolder(fileClickeado!!)
                                    if (exito) {
                                        Toast.makeText(
                                            context,
                                            "¡Carpeta eliminada exitosamente!",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        guiasViewModel.getAllUpdatedGuides(file)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error al eliminar la carpeta",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }


                                    // Recuperamos el dialogo abierto actualmente
                                    // (Fragment_DialogListarGuias_popup.java)
                                    // y lo cerramos.
                                    val dialogoEliminarGuia = getDialog()
                                    dialogoEliminarGuia?.dismiss()

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
                    }
                    /*2 ->
                        // Se ejecuta cuando quiere cambiar el nombre de la carpeta
                        if (fileClickeado.exists()) {
                            // Creamos las preferencias y dentro de ellas guardamos el arreglo item
                            val preferencias: SharedPreferences =
                                requireContext().getSharedPreferences(
                                    "crear_folder",
                                    AppCompatActivity.MODE_PRIVATE
                                )

                            val editor = preferencias.edit()
                            editor.putString("crear_folder", "cambiar_nom_folder")
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
                        }*/
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
                val ruta = "$fileClickeado.xml"
                when (which) {
                    0 -> {
                        // Si entra al primer item se abre la guía a review
                        val intentAbrirGuia = Intent(activity, Activity_RepasarGuia::class.java)
                        //intentAbrirGuia.putExtra("nombre_archivo", guia.nombreGuia)
                        intentAbrirGuia.putExtra("ruta", ruta)
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
                        intentModificarGuia.putExtra("ruta", ruta)
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
                                    File(ruta).delete()
                                    Toast.makeText(
                                        context,
                                        "¡Archivo eliminado exitosamente!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    guiasViewModel.getAllUpdatedGuides(file)

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
                            editor.putString("ruta", ruta)
                            editor.apply()

                            // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
                            val dialogo = Fragment_DialogNuevoArchivo_popu()
                            dialogo.show(childFragmentManager, "Fragment")
                        }
                    }

                    4 -> {
                        val subMenuBuilder = AlertDialog.Builder(context)
                        subMenuBuilder.setTitle("Mover a...")

                        var rutaSinArchivo = ruta.substringBeforeLast("/")

                        if (rutaSinArchivo == path) {
                            val listaCarpetas = file.listFiles { file -> file.isDirectory }

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

                                        val archivoEnCarpeta =
                                            File("" + file + "/" + selectedFolder + "/" + guia.nombreGuia + ".xml")
                                        if (archivoEnCarpeta.exists()) {
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
                                                    moverImagenes(ruta, selectedFolder)

                                                    Files.copy(
                                                        Paths.get("" + file + "/" + guia.nombreGuia + ".xml"),
                                                        Paths.get("" + file + "/" + selectedFolder + "/" + guia.nombreGuia + ".xml"),
                                                        StandardCopyOption.REPLACE_EXISTING
                                                    )

                                                    // Borrar archivo
                                                    File(file, guia.nombreGuia + ".xml").delete()

                                                    guiasViewModel.getAllUpdatedGuides(file)
                                                    /*val dialogoEliminarGuia = getDialog()
                                                    dialogoEliminarGuia!!.dismiss()*/

                                                    Toast.makeText(
                                                        context,
                                                        "El archivo se movió correctamente",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                .setNegativeButton(
                                                    "Cancelar"
                                                ) { dialog, _ -> dialog.dismiss() }.create().show()
                                        } else {
                                            moverImagenes(ruta, selectedFolder)

                                            Files.copy(
                                                Paths.get("" + file + "/" + guia.nombreGuia + ".xml"),
                                                Paths.get("" + file + "/" + selectedFolder + "/" + guia.nombreGuia + ".xml"),
                                                StandardCopyOption.REPLACE_EXISTING
                                            )

                                            // Borrar archivo
                                            File(file, guia.nombreGuia + ".xml").delete()

                                            guiasViewModel.getAllUpdatedGuides(file)
                                            /*val dialogoEliminarGuia = getDialog()
                                            dialogoEliminarGuia!!.dismiss()*/


                                            Toast.makeText(
                                                context, "El archivo se movió correctamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    } catch (e: Exception) {
                                        println("Error al copiar el archivo: ${e.message}")
                                    }
                                }
                            }
                        } else {
                            subMenuBuilder.setItems(
                                arrayOf<CharSequence>(
                                    "Principal",
                                )
                            ) { _, subWhich ->
                                // Manejar la selección de la carpeta dentro del submenú
                                when (subWhich) {
                                    0 -> {
                                        // Mover a la carpeta seleccionada
                                        try {
                                            // Copiar el archivo
                                            val guia = guiasViewModel.getGuia(position)

                                            val selectedFolder = "Principal"
                                            moverImagenes(ruta, selectedFolder)

                                            Files.copy(
                                                Paths.get("$fileClickeado.xml"),
                                                Paths.get(path + "/" + guia.nombreGuia + ".xml"),
                                                StandardCopyOption.REPLACE_EXISTING
                                            )

                                            // Borrar archivo
                                            val rutaPrincipal = "$fileClickeado.xml"
                                            File(rutaPrincipal).delete()
                                            // guiasViewModel.getAllUpdatedGuides(file)
                                            /*val dialogoEliminarGuia = getDialog()
                                            dialogoEliminarGuia!!.dismiss()*/

                                            guiasViewModel.getMainPath()
                                            guiasViewModel.getAllUpdatedGuides(file)

                                            binding.imgvFolder.visibility = View.VISIBLE
                                            binding.tvNuevaCarpeta.visibility = View.VISIBLE
                                            binding.imgvBack.visibility = View.GONE
                                            binding.tvRegresar.visibility = View.GONE

                                            Toast.makeText(
                                                context, "El archivo se movió correctamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } catch (e: Exception) {
                                            println("Error al copiar el archivo: ${e.message}")
                                        }
                                    }
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
    }

    private fun changePath(folder: String) {
        guiasViewModel.changeFilePath(folder)
    }

    private fun deleteFolder(fileClickeado: File): Boolean {
        if (fileClickeado.isDirectory) {
            val children = fileClickeado.listFiles()
            if (children != null) {
                for (child in children) {
                    val success = deleteFolder(child)
                    if (!success) {
                        return false
                    }
                }
            }
        }
        return fileClickeado.delete()
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

    private fun moverImagenes(fileClickeado: String, selectedFolder: String) {
        //obtenerDatosXML(fileClickeado, selectedFolder)
        var doc: Document? = null
        val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder
        try {
            db = dbf.newDocumentBuilder()
            var filePath: File = File(fileClickeado)
            val fis = FileInputStream(filePath)

            doc = db.parse(fis)

            // Buscamos los Nodos Interrogante y accedemos a lo que se encuentre dentro.
            val cuestionario: NodeList = doc.getElementsByTagName("Interrogante")
            for (i in 0 until cuestionario.length) {
                // Accedes a los elementos de dicho nodo
                val e: Element = cuestionario.item(i) as Element
                if (e.getAttribute("pregunta").contains("frqwhqw://phgld/slfnhu/")) {
                    var descifrado = cifrar(e.getAttribute("pregunta"), 26 - 3)
                    // descifrado = descifrado.replace("content://media/picker/".toRegex(), "")
                    val imagen = descifrado.substringAfterLast("/")
                    val ruta = fileClickeado.substringBeforeLast("/")
                    var rutaImagen = ruta.replace("guias", "imagenes")
                    val rutaActual = "$rutaImagen/$imagen"

                    if (selectedFolder != "Principal") {
                        rutaImagen = "$rutaImagen/$selectedFolder/$imagen"
                    } else {
                        rutaImagen = "$fileImages/$imagen"
                    }

                    Files.copy(
                        Paths.get("" + rutaActual),
                        Paths.get("" + rutaImagen),
                        StandardCopyOption.REPLACE_EXISTING
                    )

                    File(rutaActual).delete()
                }

                if (e.getAttribute("respuesta").contains("frqwhqw://phgld/slfnhu/")) {
                    var descifrado = cifrar(e.getAttribute("respuesta"), 26 - 3)
                    val imagen = descifrado.substringAfterLast("/")
                    val ruta = fileClickeado.substringBeforeLast("/")
                    var rutaImagen = ruta.replace("guias", "imagenes")
                    val rutaActual = "$rutaImagen/$imagen"

                    if (selectedFolder != "Principal") {
                        rutaImagen = "$rutaImagen/$selectedFolder/$imagen"
                    } else {
                        rutaImagen = "$fileImages/$imagen"
                    }

                    Files.copy(
                        Paths.get("" + rutaActual),
                        Paths.get("" + rutaImagen),
                        StandardCopyOption.REPLACE_EXISTING
                    )

                    File(rutaActual).delete()
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