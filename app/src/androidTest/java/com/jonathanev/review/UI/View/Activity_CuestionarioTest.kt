package com.jonathanev.review.UI.View

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jonathanev.review.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class Activity_CuestionarioTest{
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var mainActivityRule: ActivityScenarioRule<Activity_Cuestionario> = ActivityScenarioRule(
        Intent(ApplicationProvider.getApplicationContext(), Activity_Cuestionario::class.java).apply {
            putExtra("nombre_archivo", "z.xml")  // Poner el extra necesario aquí
        }
    )

    @Before
    fun setUp() {
        hiltRule.inject()
        Intents.init()
    }

    @After
    fun tearDown(){
        Intents.release()
    }

    @Test
    fun darle_click_al_botón_de_atras_al_crear_una_guia(){
        // Asegurarse de que la actividad está en estado de creación y visible
        onView(withId(R.id.imgvPrevious)).perform(click())

        // assertTrue(true)
        // Verificar si el método handlePreviousButtonClick() devuelve true
        /*mainActivityRule.scenario.onActivity { activity ->
            val result = activity.handlePreviousButtonClick()
            assertFalse(result)
        }*/
    }
}