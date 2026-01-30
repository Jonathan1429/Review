package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.result.MigrationResult

interface GuiaMigrationRepository {
    fun moveGuides(): MigrationResult
}