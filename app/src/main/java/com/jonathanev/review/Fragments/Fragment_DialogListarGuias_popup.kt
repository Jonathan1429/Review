package com.jonathanev.review.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jonathanev.review.Activities.Activity_Modificar
import com.jonathanev.review.Activities.Activity_RepasarGuia
import com.jonathanev.review.Clases.Guia
import com.jonathanev.review.Fragments.Adaptadores.ListarGuiasAdapter
import com.jonathanev.review.Fragments.Fragment_DialogNuevoArchivo_popu.DialogListener
import com.jonathanev.review.R
import com.jonathanev.review.databinding.FragmentListarGuiasBinding
import java.io.File

class Fragment_DialogListarGuias_popup : DialogFragment(), DialogListener {
    private var binding: FragmentListarGuiasBinding? = null
    private lateinit var adaptadorListarGuias: ListarGuiasAdapter

    // Guardar la collection del set en este Arreglo
    private val item = ArrayList<String>()

    // Cargar el ListView
    private val guias = ArrayList<Guia>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentListarGuiasBinding.inflate(layoutInflater)
        return binding!!.root
    }

    override fun onDialogClosed() {
        // Realizar las acciones necesarias después de cerrar el diálogo
        val dialogActual = dialog
        dialogActual!!.dismiss()
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recibimos las preferencias y las guardamos nuevamente en un set.
        val preferences =
            requireActivity().getSharedPreferences("nombres_guias", Context.MODE_PRIVATE)
        //val set = preferences.getStringSet("guias_estudio", null)
        val set = preferences.getStringSet("guias_estudio", null) ?: emptySet()

        // Metemos la collection directamente en el arreglo y se ordena automáticamente.
        item.addAll(set)

        // Ordena las guías con el método sort FALTA ESTA PARTE
        item.sort()

        // Sino tengo ninguna guía de estudio aparece el texto que no tengo guías
        if (item.isEmpty()) {
            binding!!.tvSinGuias.visibility = View.VISIBLE
        } else {
            // Si es lo contrario no aparece el texto
            binding!!.tvSinGuias.visibility = View.INVISIBLE
        }

        // Cargamos la lista de los items
        cargarListaGuiasEstudio(item)
    }

    private fun cargarListaGuiasEstudio(item: ArrayList<String>) {
        // Se crea un número random para saber cual imagen aparecerá en el listado.
        var numeroRandom = (1..6).random()
        when (numeroRandom) {
            1 -> numeroRandom = R.drawable.img_estudiante1 //numeroRandom = R.drawable.cerebro
            2 -> numeroRandom = R.drawable.img_estudiante2
            3 -> numeroRandom = R.drawable.img_estudiante3
            4 -> numeroRandom = R.drawable.img_estudiante4
            5 -> numeroRandom = R.drawable.img_estudiante5
            6 -> numeroRandom = R.drawable.img_estudiante6
        }

        //var adaptador: AdaptadorPersonalizadoListarGuias? = null
        var guia: Guia

        // La imagen guardada en numeroRandom se pondrá en el objeto guias
        // Después se irá poniendo el nombre de cada una de las guias en el objeto
        for (i in item.indices) {
            guia = Guia()
            guia.imgGuia = numeroRandom
            guia.nombreGuia = item[i]
            guias.add(guia)
        }
        adaptadorListarGuias = ListarGuiasAdapter(guias) {position -> optionsGuide(position)}
        binding!!.lvGuiasEstudio.layoutManager = LinearLayoutManager(context)
        binding!!.lvGuiasEstudio.setHasFixedSize(true)
        binding!!.lvGuiasEstudio.adapter = adaptadorListarGuias
    }

    private fun optionsGuide(position: Int) {
        val guias = guias[position]

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
                "Cancelar"
            )
        ) { dialog, which ->
            when (which) {
                0 -> {
                    // Si entra al primer item se abre la guía a review
                    val intentAbrirGuia = Intent(activity, Activity_RepasarGuia::class.java)
                    intentAbrirGuia.putExtra("nombre_archivo", guias.nombreGuia)
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
                    intentModificarGuia.putExtra("nombre_archivo", guias.nombreGuia)
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
                            @SuppressLint("SdCardPath") val file =
                                File("/data/data/com.jonathanev.review/files/")
                            if (file.exists()) {
                                File(file, guias.nombreGuia + ".xml").delete()
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
                    @SuppressLint("SdCardPath") val file =
                        File("/data/data/com.jonathanev.review/files/")
                    if (file.exists()) {
                        // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
                        val dialogo = Fragment_DialogNuevoArchivo_popu()
                        dialogo.show(childFragmentManager, "Fragment")

                        // Creamos las preferencias y dentro de ellas guardamos el arreglo item
                        var preferencias = requireContext().getSharedPreferences(
                            "cambiar_nombre",
                            Context.MODE_PRIVATE
                        )
                        var editor = preferencias.edit()
                        editor.putString("cambiar_nombre", "sin nombre")
                        editor.commit()
                        preferencias = requireContext().getSharedPreferences(
                            "nombre_archivo",
                            Context.MODE_PRIVATE
                        )
                        editor = preferencias.edit()
                        editor.putString("nombre_archivo", guias.nombreGuia)
                        editor.commit()
                    } else {
                        Toast.makeText(
                            context, "La ruta para cambiar " +
                                    "el nombre del archivo actualmente no existe.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                4 -> {
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