package com.jonathanev.review.domain.model

data class GuidePathContext(
    val guideName: String,
    val target: GuidePathTarget
)


enum class GuidePathTarget {
    GUIDE_FILE,
    IMAGES_FOLDER,
}
