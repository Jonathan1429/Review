package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath

interface PathResolve {
    fun currentPathResolve(guideDomainModel: GuideDomainModel): GuidePath
    fun newPathResolve(newFolder: String): GuidePath
}