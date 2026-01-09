package com.jonathanev.review.presentation.folders.model

import com.jonathanev.review.domain.model.FolderDomainModel


sealed class FolderResultDomain {
    data class Success(val folderDomain: FolderDomainModel) : FolderResultDomain()
    data class Error(val message: String) : FolderResultDomain()
}