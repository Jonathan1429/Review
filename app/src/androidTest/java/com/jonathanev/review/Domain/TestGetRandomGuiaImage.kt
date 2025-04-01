package com.jonathanev.review.Domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jonathanev.review.R
import io.mockk.every
import io.mockk.mockkObject
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class TestGetRandomGuiaImage {
    private val getRandomGuiaImageUseCase = GetRandomGuiaImageUseCase()

    @Test
    fun should_return_correct_drawable_based_on_random_number() {
        mockkObject(Random)  // Mockeamos el objeto Random

        every { Random.nextInt(1, 7) } returns 1  // Forzamos que devuelva 3

        val result = getRandomGuiaImageUseCase.invoke()

        assertEquals(R.drawable.img_estudiante1, result)  // Verificamos que devuelve la imagen esperada
    }
}