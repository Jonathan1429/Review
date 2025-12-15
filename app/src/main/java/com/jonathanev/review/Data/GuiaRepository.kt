package com.jonathanev.review.Data

import com.jonathanev.review.Data.Model.GuideModel
import com.jonathanev.review.Data.Model.prueba.FolderModel
import com.jonathanev.review.Data.Model.prueba.QAItem
import java.io.File

interface GuiaRepository {
    fun getXMLVersion(ruta: String): List<QAItem>
    fun getAttributesGuide(file: File): GuideModel
    fun getGuides(file: File): List<GuideModel>
    fun getFolders(file: File):List<FolderModel>
}