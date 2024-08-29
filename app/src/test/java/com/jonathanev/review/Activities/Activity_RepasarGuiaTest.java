package com.jonathanev.review.Activities;

import android.annotation.SuppressLint;

import com.jonathanev.review.UI.View.Activity_RepasarGuia;

import java.io.File;

class Activity_RepasarGuiaTest {
    //@Test
    public void test1(){
        @SuppressLint("SdCardPath") File file = new File("/data/data/com.jonathanev.review/files/A.-C++.xml");

        Activity_RepasarGuia activity_repasarGuia = new Activity_RepasarGuia();
        //activity_repasarGuia.obtenerDatosXML();

        //Assertions.fail();

        /*UsuarioDto esperado = new UsuarioDto(1L, "Nombre");
        final UsuarioDto resultado = usuarioServicio.crearUsuario(1L, "Nombre");
        // Sino agregamos las dos líneas siguientes aparecerán a comparar las direcciones de memoria
        Assertions.assertEquals(esperado.id, resultado.id);
        Assertions.assertEquals(esperado.nombre, resultado.nombre, "Los nombres son diferentes");
        Assertions.assertEquals(esperado, resultado);*/
    }
}