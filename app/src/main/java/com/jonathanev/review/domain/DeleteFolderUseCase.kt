package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.presentation.folders.model.FolderUiModel
import javax.inject.Inject

class DeleteFolderUseCase @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val pathProvider: PathProvider
) {
    operator fun invoke(folderUiModel: FolderUiModel) {
        /*val folderDomainModel = folderUiModel.toDomain()
        val currentPath =
            filePathsProvider.buildFolder(File(pathProvider.getCurrentPath()), folderDomainModel.name)

        val pathImages = File(currentPath.path.replace(StorageFolders.GUIAS, StorageFolders.IMAGENES))

        return if (currentPath.deleteRecursively()){
            pathImages.deleteRecursively()
            UIStopEvent.DeleteFolderSuccess("Se ha borrado la carpeta correctamente")
        } else{
            UIStopEvent.ShowMessage("No se pudo borrar la carpeta correctamente")
        }*/
    }
}