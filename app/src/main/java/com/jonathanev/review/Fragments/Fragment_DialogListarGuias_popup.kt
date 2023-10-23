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
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.jonathanev.review.Activities.Activity_Modificar
import com.jonathanev.review.Activities.Activity_RepasarGuia
import com.jonathanev.review.Clases.Guias
import com.jonathanev.review.Fragments.Adaptadores.AdaptadorPersonalizadoListarGuias
import com.jonathanev.review.Fragments.Fragment_DialogNuevoArchivo_popu.DialogListener
import com.jonathanev.review.R
import com.jonathanev.review.databinding.FragmentListarGuiasBinding
import java.io.File
class Fragment_DialogListarGuias_popup : DialogFragment(), DialogListener {
    private var binding: FragmentListarGuiasBinding? = null

    // Guardar la collection del set en este Arreglo
    private val item = ArrayList<String>()

    // Cargar el ListView
    private val lista = ArrayList<Guias>()
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
        val preferences = requireActivity().getSharedPreferences("nombres_guias", Context.MODE_PRIVATE)
        val set = preferences.getStringSet("guias_estudio", null)

        // Metemos la collection directamente en el arreglo y se ordena automáticamente.
        item.addAll(set!!)

        // Ordena las guías con el método sort FALTA ESTA PARTE
        /*item.sort(object : Comparator<String?> {
            override fun compare(o1: String, o2: String): Int {
                return o1.compareTo(o2)
            }
        })*/

        // Sino tengo ninguna guía de estudio aparece el texto que no tengo guías
        if (item.isEmpty()) {
            binding!!.tvSinGuias.visibility = View.VISIBLE
        } else {
            // Si es lo contrario no aparece el texto
            binding!!.tvSinGuias.visibility = View.INVISIBLE
        }

        // Cargamos la lista de los items
        cargarListaGuiasEstudio(item)
        binding!!.lvGuiasEstudio.onItemClickListener =
            OnItemClickListener { parent, view, posicion, id ->
                val guias = lista[posicion]

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

                        2 ->                                         // Se ejecuta cuando quiere eliminar la guía.
                            AlertDialog.Builder(context)
                                .setTitle("¡Atención!")
                                .setMessage(
                                    "¿Estás seguro que deseas eliminar la" +
                                            " guia?"
                                )
                                .setPositiveButton("Si") { dialogInterface, i ->
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
                                .setNegativeButton("Cancelar") { dialog, i -> dialog.dismiss() }
                                .create().show()

                        3 -> {
                            // Se ejecuta cuando quiere cambiar el nombre de la guía
                            @SuppressLint("SdCardPath") val file =
                                File("/data/data/com.jonathanev.review/files/")
                            if (file.exists()) {
                                // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
                                val dialogo = Fragment_DialogNuevoArchivo_popu()
                                //dialogo.show(childFragmentManager, "Fragment")

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
    }

    private fun cargarListaGuiasEstudio(item: ArrayList<String>) {
        // Se crea un número random para saber cual imagen aparecerá en el listado.
        var numeroRandom = (Math.random() * 6).toInt() + 1
        when (numeroRandom) {
            1 -> numeroRandom = R.drawable.cerebro
            2 -> numeroRandom = R.drawable.cuaderno
            3 -> numeroRandom = R.drawable.estudiar
            4 -> numeroRandom = R.drawable.libro_dual
            5 -> numeroRandom = R.drawable.libro_triple
            6 -> numeroRandom = R.drawable.libros
        }
        var adaptador: AdaptadorPersonalizadoListarGuias? = null
        var guias: Guias

        // La imagen guardada en numeroRandom se pondrá en el objeto guias
        // Después se irá poniendo el nombre de cada una de las guias en el objeto
        for (i in item.indices) {
            guias = Guias()
            guias.imgGuia = numeroRandom
            guias.nombreGuia = item[i]
            lista.add(guias)
        }
        adaptador = AdaptadorPersonalizadoListarGuias(context, lista)
        binding!!.lvGuiasEstudio.adapter = adaptador
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