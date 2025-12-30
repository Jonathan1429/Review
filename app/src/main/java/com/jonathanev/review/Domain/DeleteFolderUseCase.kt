package com.jonathanev.review.Domain

import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.data.storage.StorageFolders
import java.io.File
import javax.inject.Inject

class DeleteFolderUseCase @Inject constructor() {
    operator fun invoke(currentPath: File): UIStopEvent {
        val pathImages = File(currentPath.path.replace(StorageFolders.GUIAS, StorageFolders.IMAGENES))

        return if (currentPath.deleteRecursively()){
            pathImages.deleteRecursively()
            UIStopEvent.DeleteFolderSuccess("Se ha borrado la carpeta correctamente")
        } else{
            UIStopEvent.ShowMessage("No se pudo borrar la carpeta correctamente")
        }
    }
}