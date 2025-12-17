package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants.GUIAS
import com.jonathanev.review.Core.Constants.IMAGENES
import com.jonathanev.review.Data.Model.prueba.UIStopEvent
import java.io.File
import javax.inject.Inject

class DeleteFolderUseCase @Inject constructor() {
    operator fun invoke(currentPath: File): UIStopEvent {
        val pathImages = File(currentPath.path.replace(GUIAS, IMAGENES))

        return if (currentPath.deleteRecursively()){
            pathImages.deleteRecursively()
            UIStopEvent.DeleteFolderSuccess("Se ha borrado la carpeta correctamente")
        } else{
            UIStopEvent.ShowMessage("No se pudo borrar la carpeta correctamente")
        }
    }
}