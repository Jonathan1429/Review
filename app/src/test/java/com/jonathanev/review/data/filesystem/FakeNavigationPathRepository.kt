package com.jonathanev.review.data.filesystem

import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.NavigationPathRepository

class FakeNavigationPathRepository(
    private val root: GuidePath
) : NavigationPathRepository {

    override fun getRootGuides(): GuidePath = root

    override fun getRootImages(): GuidePath =
        error("getRootImages no es usado en este test")

    override fun next(
        current: RelativeGuidePath,
        fileName: String
    ): RelativeGuidePath =
        error("next no es usado en este test")

    override fun back(
        current: RelativeGuidePath
    ): RelativeGuidePath =
        error("back no es usado en este test")
}
