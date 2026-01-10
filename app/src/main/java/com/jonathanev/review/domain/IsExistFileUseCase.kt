package com.jonathanev.review.domain

import android.icu.text.CaseMap.Fold
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.presentation.folders.model.FolderAction
import javax.inject.Inject

class IsExistFileUseCase @Inject constructor(
    private val getFoldersWithNumGuidesUseCase: GetFoldersWithNumGuidesUseCase
) {
    operator fun invoke(
        mode: FolderAction,
        cachedGuides: List<GuideDomainModel>,
        name: String
    ): Boolean {
        return if (mode is FolderAction.RenamingFile || mode == FolderAction.CreatingFile) {
            val guideDomainModel = cachedGuides.find { it.nameGuide == name }
            guideDomainModel != null
        } else {
            val foldersDomain =
                getFoldersWithNumGuidesUseCase.invoke().sortedBy { it.folder.name }
            val folderDomainModel = foldersDomain.find { it.folder.name == name }
            folderDomainModel != null
        }
    }
}