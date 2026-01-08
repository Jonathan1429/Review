package com.jonathanev.review.domain.repository

import java.io.File

interface FileExplorerRepository {
    fun listCurrent(): List<File>
}