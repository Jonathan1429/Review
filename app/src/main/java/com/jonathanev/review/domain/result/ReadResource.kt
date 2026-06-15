package com.jonathanev.review.domain.result

/**
 * Contenedor universal para el manejo seguro de estados en la app.
 * El modificador 'out' indica que esta interfaz solo producirá (hará salir) datos de tipo T.
 * El tipo T es implementado solo para el caso de Exito
 */
sealed interface ReadResource<out T> {

    /**
     * Estado de Éxito.
     * Ejemplo: fun obtenerGuia(): ReadResource<GuideDomainModel>
     * Como la función ya le asignó un valor real a la 'T' (por ejemplo: [GuideDomainModel]),
     * esta rama usa el canal oficial para regresar el objeto solicitado en la variable [data].
     * con Success<out T> le decimos que va a salir T.
     * T = data
     * data = GuideDomainModel
     */
    data class Success<out T>(val data: T) : ReadResource<T>

    /**
     * Estado de Error.
     * Al fallar, no podemos usar 'ReadResource<T>' o 'ReadResource<GuideDomainModel>'
     * porque estaríamos obligados a regresa algo referente a la Guía, lo cual no existe.
     *
     * Por lo tanto, usamos 'ReadResource<Nothing>' para desconectar esta rama del canal oficial
     * de la data. Al cerrar esa puerta, somos libres de regresar nuestra propia clase ([GuideError])
     * a través de una variable interna privada ([exception]) para manejarla posteriormente libremente.
     */
    data class Error(val exception: GuideError) : ReadResource<Nothing>
}

/**
 * Catálogo propio de errores para el parseo de archivos XML.
 * Permite al ViewModel saber exactamente qué falló (si el archivo no existe o está corrupto)
 * para tomar una acción en la UI sin cerrar la aplicación.
 */
sealed interface GuideError {
    data object FileNotFound : GuideError
    data object InvalidXmlFormat : GuideError
    data object EmptyOrCorruptFile : GuideError
    data class UnknownError(val message: String?) : GuideError
}

/**
 * ¿Que eviqueta el tener un tipo T de salida?
 * sealed interface ResourceGuide {
 *     data class Success(val data: GuideDomainModel) : ResourceGuide
 *     data class Error(val exception: GuideError) : ResourceGuide
 * }
 *
 * sealed interface ResourcePreguntas {
 *     data class Success(val data: PreguntaDomainModel) : ResourcePreguntas
 *     data class Error(val exception: GuideError) : ResourcePreguntas
 * }
 *
 * sealed interface ResourceHistorial {
 *     data class Success(val data: HistorialDomainModel) : ResourceHistorial
 *     data class Error(val exception: GuideError) : ResourceHistorial
 * }
 *
 * EVITA TENER QUE CREAR LA MISMA CLASE MULTIPLES VECES PARA CADA OCASION Y ASÍ PUEDES REGRESAR
 * EN T MUCHOS TIPOS Y REUTILIZAR CÓDIGO.
 * Con ese valor dinámico puedes regresar: GuideDomainModel, PreguntaDomainModel, HitorialDomainModel
 * Por poner ejemplos
 *
 *
 * Esto me dijo la IA
 *Vamos a ponerle un sello de aprobación a tus palabras porque lo describiste de una forma brillante:
 * "Arriba ya no estoy regresando T, porque ya se le asignó el valor GuideDomainModel" ➔ ¡Exacto! Cuando tu
 * función dice fun obtenerGuia(): ReadResource<GuideDomainModel>, la T del padre queda automáticamente bloqueada
 * y condicionada a ser un GuideDomainModel.
 *
 * "Abajo no puedo poner ReadResource<T> ni ReadResource<GuideDomainModel> porque estaría obligado a
 * regresar algo referente a GuideDomainModel" ➔ ¡Tal cual! Si pusieras eso, Kotlin te exigiría una
 * Guía dentro de la clase Error, lo cual no tiene sentido porque la operación falló.
 *
 * "La idea es regresar otra clase propia que yo creé... por eso le digo que no regresaré nada de
 * ReadResource<Nothing> sino que regresaré otra cosa que es lo de GuideError" ➔ ¡SÍ! Mil veces sí.
 *
 * Al poner ReadResource<Nothing>, estás "desconectando" formalmente la clase Error de la obligación
 * de entregar un GuideDomainModel. Le dices a Kotlin: "Olvídate de la Guía aquí". Y gracias a eso,
 * tienes la libertad total de meter por tu cuenta tu propia clase GuideError en el constructor.
 *
 * Tu mapa mental definitivo:Success ➔ Usa el canal oficial del padre porque sí consiguió el
 * GuideDomainModel.Error ➔ Cancela el canal oficial usando <Nothing> para no estar obligado a dar
 * una guía, y en su lugar, abre su propio canal privado para entregarte el GuideError.
 */