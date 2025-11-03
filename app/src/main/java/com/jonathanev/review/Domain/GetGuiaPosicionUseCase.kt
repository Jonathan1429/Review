package com.jonathanev.review.Domain

import androidx.lifecycle.MutableLiveData
import com.jonathanev.review.Data.GuiaResult
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.provider.GuiaProvider
import javax.inject.Inject

class GetGuiaPosicionUseCase @Inject constructor(){
    operator fun invoke(position: Int, guias: MutableLiveData<List<GuiaModel>>): GuiaResult {
        val lista = guias.value ?: return GuiaResult.Empty
        return lista.getOrNull(position)?.let { GuiaResult.Success(it) }
            ?: GuiaResult.Error("No se encontró la guía en la posición $position")
    }
}