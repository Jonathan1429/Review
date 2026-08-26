package com.jonathanev.review.domain.model

enum class SavingStatus {
    IDLE,       // Sin cambios pendientes
    SAVING,     // Procesando el archivo XML en segundo plano
    SAVED,      // Guardado con éxito (para mostrar temporalmente "Guardado")
    ERROR       // Falló la persistencia
}