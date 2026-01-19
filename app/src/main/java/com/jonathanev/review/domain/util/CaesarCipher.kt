package com.jonathanev.review.domain.util

import javax.inject.Inject

class CaesarCipher @Inject constructor() {
    fun decrypt(texto: String, desplazamiento: Int): String {
        val resultado = StringBuilder()

        for (caracter in texto) {
            if (caracter.isLetter()) {
                val base = if (caracter.isUpperCase()) 'A' else 'a'
                val letraCifrada =
                    ((caracter - base + desplazamiento) % 26 + base.code).toChar()
                resultado.append(letraCifrada)
            } else {
                resultado.append(caracter)
            }
        }

        return resultado.toString()
    }
}