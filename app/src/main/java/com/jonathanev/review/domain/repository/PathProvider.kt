package com.jonathanev.review.domain.repository

import java.io.File

interface PathProvider {
    val guidesRoot: File
    //fun resolveGuidePath(context: GuidePathContext): File
    fun resolveFoldersPath(folderId: String): File
}