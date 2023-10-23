package com.jonathanev.review.Clases

import java.util.Objects

class Guias {
    var nombreGuia: String? = null
    var imgGuia = 0

    constructor()
    constructor(nombreGuia: String?, imgGuia: Int) {
        this.nombreGuia = nombreGuia
        this.imgGuia = imgGuia
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val guias = o as Guias
        return nombreGuia == guias.nombreGuia
    }

    override fun hashCode(): Int {
        return Objects.hash(nombreGuia)
    }
}