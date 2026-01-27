package com.jonathanev.review.domain

import javax.inject.Inject

class IsExistFolderUseCase @Inject constructor(
    private val getFoldersWithNumGuidesUseCase: GetFoldersWithNumGuidesUseCase
) {
    operator fun invoke(name: String): Boolean {
        val foldersDomain = getFoldersWithNumGuidesUseCase.invoke()
        val folderDomainModel = foldersDomain.find { it.folder.name == name }
        return folderDomainModel != null
    }
}