package com.jonathanev.review.UI.View.Fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.jonathanev.review.Core.Constants.DATASTORE
import com.jonathanev.review.Core.Constants.GUIAS
import com.jonathanev.review.Core.Constants.IMAGENES
import com.jonathanev.review.Core.Constants.IMAGENESPIVOTE
import com.jonathanev.review.Core.Constants.PRINCIPAL
import com.jonathanev.review.Data.FileAction
import com.jonathanev.review.Data.FolderAction
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.R
import com.jonathanev.review.UI.View.ActivityCuestionario
import com.jonathanev.review.UI.ViewModel.Fragments.FragDialNuevoArchViewModel
import com.jonathanev.review.databinding.FragmentNuevoArchivoBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FragmentDialogNuevoArchivoPopu() : DialogFragment() {
    /*companion object {
        fun newInstance(mode: FolderAction): FragmentDialogNuevoArchivoPopu {
            val args = Bundle().apply {
                putString("dialog_mode", mode)
            }
            return FragmentDialogNuevoArchivoPopu().apply {
                arguments = args
            }
        }
    }*/

    /*private val action: FolderAction by lazy {
        val value = arguments?.getString("dialog_mode") ?: FolderAction.None.name
        FolderAction.valueOf(value)
    }*/

    private lateinit var binding: FragmentNuevoArchivoBinding
    private val viewModel by viewModels<FragDialNuevoArchViewModel>()

    @Inject
    lateinit var filePathsProvider: FilePathsProvider

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
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNuevoArchivoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*when (action) {
            FolderAction.CreatingFolder -> {
                binding.tilNombreArchivo.hint = getString(R.string.etNombreCarpeta)
                binding.btnGuardarGuiaEstudio.text = getString(R.string.btnCrearCarpeta)
            }

            FolderAction.RenamingFile -> {
                binding.tilNombreArchivo.hint = getString(R.string.etNombreArchivo)
                binding.btnGuardarGuiaEstudio.text = getString(R.string.btnCrearArchivo)
            }

            FolderAction.RenamingFolder -> {
                binding.tilNombreArchivo.hint = getString(R.string.etNombreCarpeta)
                binding.btnGuardarGuiaEstudio.text = getString(R.string.btnCrearCarpeta)
            }

            FolderAction.CreatingFile -> {
                binding.tilNombreArchivo.hint = getString(R.string.etNombreArchivo)
                binding.btnGuardarGuiaEstudio.text = getString(R.string.btnCrearArchivo)
            }

            FolderAction.None -> {
                Toast.makeText(
                    context, "No es posible crear archivos",
                    Toast.LENGTH_SHORT
                ).show()
                cerrarDialogo()
            }
        }*/

        binding.btnGuardarGuiaEstudio.setOnClickListener {
            if (!validateName()) return@setOnClickListener

            val fileName = binding.etNombreArchivo.text.toString().trim()
            /*when (action) {
                FolderAction.CreatingFolder -> creatingFolder(fileName)
                FolderAction.RenamingFile -> renamingFile(fileName)
                FolderAction.RenamingFolder -> TODO()
                FolderAction.CreatingFile -> creatingGuide(fileName)
                FolderAction.None -> Unit
            }*/
        }
    }

    private fun creatingGuide(fileName: String) {
        var creatingFile = true
        if (viewModel.exist(fileName)) {
            alertDialog { creatingFile = it }
        }

        if (creatingFile){
            val intent = Intent(activity, ActivityCuestionario::class.java)
            startActivity(intent)
        }
    }

    private fun creatingFolder(fileName: String) {
        val response = viewModel.creatingFolder(fileName)

        val message = when(response){
            FileAction.SUCCESS -> "Carpeta creada exitosamente"
            FileAction.EXIST -> "Ya tienes una carpeta con el mismo nombre"
            FileAction.ERROR -> "No se pudo crear la carpeta"
        }

        Toast.makeText(context, message,Toast.LENGTH_SHORT).show()
        cerrarTodosDialogos()
    }

    private fun renamingFile(fileName: String) {
        val response = viewModel.renamingFile(fileName)

        val message = when (response) {
            FileAction.SUCCESS -> "Se renombró exitosamente"
            FileAction.EXIST -> "Ya tienes una guia con el mismo nombre"
            FileAction.ERROR -> "No se pudo renombrar el archivo"
        }

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        cerrarTodosDialogos()
    }

    private fun alertDialog(onResult:(Boolean) -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("¡Atención!")
            .setMessage(
                ("Ya tienes una guia con el mismo nombre, " +
                        "si continúas se va a sobreescribir el archivo, " +
                        "¿seguro deseas continuar?")
            )
            .setPositiveButton("Continuar") { _, _ ->
                onResult(true)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                onResult(false)
            }
            .setOnCancelListener {
                onResult(false)
            }
            .create()
            .show()

        /*AlertDialog.Builder(context)
            .setTitle("¡Atención!")
            .setMessage(
                ("Ya tienes una guia con el mismo nombre, " +
                        "si continuas se va a sobreescribir el archivo, " +
                        "¿seguro deseas continuar?")
            )
            .setPositiveButton(
                "Continuar"
            ) { _, _ -> // Si el usuario quiere continuar reemplazamos el archivo.
                onResult(true)
                /*val response = viewModel.renamingFile(fileName)
                if (response == FileAction.SUCCESS){
                    viewModel.getAllUpdatedGuides(filePathsProvider.fileGuides)
                }

                val message = when (response) {
                    FileAction.SUCCESS -> "Se renombró exitosamente"
                    FileAction.ERROR -> "No se pudo renombrar el archivo"
                }

                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                cerrarTodosDialogos()*/
            }
            .setNegativeButton(
                "Cancelar"
            )
            { dialog, _ ->
                dialog.dismiss()
                onResult(false)
            }.create().show()

        onResult(false)*/
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

    private fun validateName(): Boolean {
        val text = binding.etNombreArchivo.text.toString()
        val invalidChars = listOf("/", ".")
        val invalidNames = listOf(DATASTORE, GUIAS, IMAGENES, IMAGENESPIVOTE, PRINCIPAL)

        val message = when {
            text.isBlank() -> "Ingresa un nombre"

            invalidChars.any { char -> text.contains(char) } ->
                "No puede haber caracteres como / o . en el nombre"

            invalidNames.any { name -> text.equals(name, ignoreCase = true) } ->
                "Ese nombre no está permitido"

            else -> null
        }

        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}