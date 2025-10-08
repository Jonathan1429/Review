package com.jonathanev.review.UI.View

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.jonathanev.review.R
import org.junit.Rule
import org.junit.Test

class MainActivityEspressoTest {
    /*
        // 🔑 Regla para lanzar la actividad en el emulador
        @get:Rule var activityRule: ActivityScenarioRule<MainActivity> =
            ActivityScenarioRule(MainActivity::class.java)

        @Test
        fun test_btnNuevaGuiaEstudio_click_opensNextScreen() {
            // 1. Verificar que el botón existe y es visible.
            onView(withId(R.id.btnNuevaGuiaEstudio))
                .check(matches(isDisplayed()))

            // 2. Ejecutar el clic.
            onView(withId(R.id.btnNuevaGuiaEstudio))
                .perform(click())

            // 3. Verificar el resultado esperado, por ejemplo, que aparezca un TextView en la siguiente pantalla.
            // on View(withId(R.id.titulo_nueva_guia)).check(matches(withText("Crear Guía")))
        }*/
}