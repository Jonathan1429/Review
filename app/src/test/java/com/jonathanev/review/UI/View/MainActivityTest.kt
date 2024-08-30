package com.jonathanev.review.UI.View

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MainActivityTest{
    private lateinit var mainActivity: MainActivity

    @Before
    fun setUp() {
        // Crear una instancia de la actividad que queremos probar
        mainActivity = MainActivity()
    }

    @Test
    fun create_folders(){
        // Llamar al método y verificar el resultado
        val result = mainActivity.createFolders()
        assertTrue("La operación debería ser exitosa", result)
    }
}