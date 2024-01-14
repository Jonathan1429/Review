package com.jonathanev.review.Domain

import com.jonathanev.review.R
import javax.inject.Inject

class getRandomGuiaImage @Inject constructor(){
    operator fun invoke(): Int{
        // Se crea un número random para saber cual imagen aparecerá en el listado.
        var numeroRandom = (1..6).random()
        return when (numeroRandom) {
            1 -> R.drawable.img_estudiante1 //numeroRandom = R.drawable.cerebro
            2 -> R.drawable.img_estudiante2
            3 -> R.drawable.img_estudiante3
            4 -> R.drawable.img_estudiante4
            5 -> R.drawable.img_estudiante5
            else -> R.drawable.img_estudiante6
        }
    }
}