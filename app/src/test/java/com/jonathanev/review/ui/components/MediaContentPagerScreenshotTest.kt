package com.jonathanev.review.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.jonathanev.review.ui.components.MediaContentPagerScreenshotTest.Companion.data
import com.jonathanev.review.ui.preview.providers.DataMediaContentPagerProvider
import com.jonathanev.review.ui.preview.providers.MediaContentPagerProvider
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.utils.captureTestOutput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// =========================================================================================
// 1. CONFIGURACIÓN DEL ENTORNO DE PRUEBAS (ANOTACIONES DE CLASE)
// =========================================================================================

/**
 * Indica a JUnit que use el runner parametrizado de Robolectric.
 * Esto permite re-ejecutar esta misma clase de prueba múltiples veces,
 * una por cada elemento retornado en la función static [data].
 */
@RunWith(ParameterizedRobolectricTestRunner::class)

/**
 * Activa el motor de renderizado gráfico nativo de Robolectric (Hardware Accelerated / Native Graphics).
 * Es REQUISITO OBLIGATORIO para que Roborazzi pueda tomar capturas reales de pantallas compuestas en Compose.
 */
@GraphicsMode(GraphicsMode.Mode.NATIVE)

/**
 * Configuración específica para el simulador de Android en Robolectric:
 * - sdk = [34]: Emula Android 14 (API 34).
 * - instrumentedPackages: Soluciona conflictos internos con cargadores de clases al monitorear actividades.
 */
@Config(
    sdk = [34],
    instrumentedPackages = ["androidx.loader.content"]
)
class MediaContentPagerScreenshotTest(
    // -------------------------------------------------------------------------------------
    // CONSTRUCTOR: JUnit inyecta aquí exactamente los 3 elementos de cada arreglo generado
    // por la función data() en el companion object (en el mismo orden e iguales tipos).
    // -------------------------------------------------------------------------------------
    private val variantName: String,     // Nombre legible para el archivo de salida PNG
    private val themeQualifier: String,  // Calificador de recursos de Android ("notnight" o "night")
    private val dataState: DataMediaContentPagerProvider // Datos de prueba para el Composable
) {

    // =========================================================================================
    // 2. REGLA DE COMPOSE Y CICLO DE VIDA DE ACTIVIDAD
    // =========================================================================================

    /**
     * Regla oficial de Compose para pruebas UI.
     * Al usar <ComponentActivity>, le pedimos a Robolectric que cree e inicie una Activity Android real.
     * Esto proporciona un ciclo de vida limpio (onCreate, onDestroy) e independiente por cada variante,
     * permitiendo llamar a `setContent` en cada ejecución sin lanzar IllegalStateException.
     */
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // =========================================================================================
    // 3. MATRIZ DE DATOS Y SUMINISTRO DE PARÁMETROS (COMPANION OBJECT)
    // =========================================================================================

    companion object {
        /**
         * Método estático exigido por ParameterizedRobolectricTestRunner.
         *
         * FUNCIONAMIENTO INTERNO:
         * 1. Se ejecuta automáticamente ANTES de instanciar la clase de test.
         * 2. Devuelve una lista de arreglos `Array<Any>`.
         * 3. JUnit lee el tamaño de esta lista y decide cuántas pruebas se van a crear.
         * 4. En cada iteración, desempaqueta los objetos del arreglo y los pasa como argumentos
         *    al constructor de esta clase.
         *
         * @JvmStatic Genera el método como un 'static method' de Java para que JUnit lo encuentre vía Reflection.
         * @Parameters(name = "{0}") Formatea el nombre en el reporte de pruebas usando el primer parámetro ("variantName").
         */
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            val provider = MediaContentPagerProvider()
            val testCases = mutableListOf<Array<Any>>()

            // Iteramos sobre todos los casos de prueba provistos por tu PreviewParameterProvider
            provider.values.forEachIndexed { index, dataState ->

                // MODO CLARO
                testCases.add(
                    arrayOf(
                        "variant_${index}_${dataState}_light",            // -> Asignado a 'variantName'
                        "notnight",                                       // -> Asignado a 'themeQualifier'
                        dataState                                         // -> Asignado a 'dataState'
                    )
                )

                // MODO OSCURO
                testCases.add(
                    arrayOf(
                        "variant_${index}_${dataState}_dark",             // -> Asignado a 'variantName'
                        "night",                                          // -> Asignado a 'themeQualifier'
                        dataState                                         // -> Asignado a 'dataState'
                    )
                )
            }

            return testCases
        }
    }

    // =========================================================================================
    // 4. EJECUCIÓN DE LA PRUEBA Y CAPTURA DE PANTALLA
    // =========================================================================================

    @Test
    fun captureVariant() {
        // PASO 1: Inyectamos el calificador del sistema a Robolectric en tiempo de ejecución.
        // "notnight" -> Fuerza la UI a Modo Claro.
        // "night"    -> Fuerza la UI a Modo Oscuro.
        // Tu Theme en Compose (`ReviewTheme`) detectará este cambio mediante `isSystemInDarkTheme()`.
        RuntimeEnvironment.setQualifiers(themeQualifier)

        // Hay que aprender eso del coerceAtLeast...
        //val pageCount = dataState.sizeList.coerceAtLeast(1)

        // PASO 2: Montamos la jerarquía de UI de Compose en la Activity de pruebas.
        composeTestRule.setContent {
            ReviewTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val pagerState = rememberPagerState(pageCount = { dataState.listType.size })

                    Column() {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "PREVIEW: $dataState - ${dataState::class.simpleName}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        MediaContentPager(
                            pagerState = pagerState,
                            assets = dataState.listType,
                            mediaForSelected = dataState.mediaForSelected,
                            guideContext = dataState.guideContext,
                            onOpenAssetClick = { _, _ -> },
                            onDeleteAssetClick = { _, _ -> }
                        )
                    }
                }
            }
        }

        // PASO 3: Tomamos la captura del árbol de vistas.
        // - onRoot(): Captura todo el contenido visible en la pantalla.
        // - captureRoboImage(): Mide, renderiza el mapa de bits (Bitmap) y lo guarda en disco.
        composeTestRule.onRoot().captureTestOutput(
            testClassName = this::class.java.simpleName,
            variantName = variantName
        )
    }
}