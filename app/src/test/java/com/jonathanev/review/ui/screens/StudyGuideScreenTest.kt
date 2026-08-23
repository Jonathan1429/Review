package com.jonathanev.review.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.input.TextFieldValue
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.ui.preview.providers.StudyGuideScreenProvider
import com.jonathanev.review.ui.theme.ReviewTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [34],
    instrumentedPackages = ["androidx.loader.content"],
    qualifiers = RobolectricDeviceQualifiers.Pixel5
)
class StudyGuideScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun onAddAssetClick_triggersCreateTextWithEmptyValues() {
        // Tomamos un proveedor de datos para inicializar FillingGuideScreen
        val sampleData = StudyGuideScreenProvider().values.first()
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Guía de Prueba", "id_123")

        // 1. ARRANGE: Montamos la jerarquía con un estado local para conmutar la pantalla
        composeTestRule.setContent {
            ReviewTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    var isNavigatedToCreateText by remember { mutableStateOf(false) }
                    var textValueState by remember { mutableStateOf(TextFieldValue("")) }

                    if (!isNavigatedToCreateText) {
                        // PANTALLA A: FillingGuideScreen con sus callbacks
                        FillingGuideScreen(
                            cardType = sampleData.typeSelected,
                            typeForSelected = sampleData.typeForSelected,
                            mediaSelected = sampleData.mediaSelected,
                            mediaForSelected = sampleData.mediaForSelected,
                            actualQuestion = sampleData.actualQuestion,
                            totalQuestions = sampleData.totalQuestions,
                            listTypeMedia = sampleData.listTypeMedia,
                            guideContext = sampleData.guideContext,
                            showDialogDeleteQuestion = sampleData.showDialogDeleteQuestion,
                            showDialogRepeatGuide = sampleData.showDialogRepeatGuide,
                            currentPosContent = 0,
                            onDissmissDialogRepeatGuide = {},
                            onConfirmDialogRepeatGuide = {},
                            onContinueDialogDeleteQuestionClick = {},
                            onBackQuestionClick = {},
                            onNextQuestionClick = {},
                            onDeleteQuestionClick = {},
                            onCardTypeClicked = {},
                            onFilterTypeClicked = {},
                            onOpenAssetClick = { _, _ -> },
                            onDeleteItemClick = { _, _ -> },

                            // AQUÍ: Al hacer clic en agregar, cambiamos el estado para navegar a CreateText
                            onAddAssetClick = {
                                isNavigatedToCreateText = true
                            },

                            onAddQuestion = {},
                            onCloseGuide = {},
                            onCurrentPosContent = {},
                            onDismissRequest = {}
                        )
                    } else {
                        CreateTextScreen(
                            guideContext = GuideContext.Creating(guideDomainModel),
                            onSaveText = { _, _ -> },
                            colorInitial = MaterialTheme.colorScheme.onSurface,
                            selectedColor = MaterialTheme.colorScheme.onSurface,
                            textValue = textValueState,
                            showDialog = false,
                            onClearColorClick = {},
                            onShowColorDialog = {},
                            onChangeTextValue = { updated -> textValueState = updated },
                            onDissmissDialog = {},
                            onColorSelected = {},
                            onDefaultColor = {},
                            onBackNav = { isNavigatedToCreateText = false }
                        )
                    }
                }
            }
        }

        // 2. WAIT: Esperar a que el botón "my_add_button" se renderice
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag("my_add_button", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 3. ACT: Ejecutar clic en el botón de la primera pantalla
        composeTestRule
            .onNodeWithTag("my_add_button", useUnmergedTree = true)
            .performClick()

        // 4. WAIT: Esperar a que la pantalla con el tag 'create_text_input' se monte tras la recomposición
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag("create_text_input", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 5. ASSERT: Verificar que el campo existe, es visible y su valor inicial es vacío
        composeTestRule
            .onNodeWithTag("create_text_input")
            .onChildren()
            .filterToOne(hasSetTextAction())
            .assertIsDisplayed()
            .assertTextEquals("")
    }

    @Test
    fun onAddAssetClick_whenMediaListIsEmpty_triggersCreateTextWithEmptyValue() {
        // Tomamos específicamente el escenario del provider donde listTypeMedia está vacía
        val emptySampleData = StudyGuideScreenProvider().values.first { it.listTypeMedia.isEmpty() }
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Guía de Prueba", "id_123")

        composeTestRule.setContent {
            ReviewTheme {
                Surface {
                    var isNavigatedToCreateText by remember { mutableStateOf(false) }
                    var textValueState by remember { mutableStateOf(TextFieldValue("")) }

                    if (!isNavigatedToCreateText) {
                        FillingGuideScreen(
                            cardType = emptySampleData.typeSelected,
                            typeForSelected = emptySampleData.typeForSelected,
                            mediaSelected = emptySampleData.mediaSelected,
                            mediaForSelected = emptySampleData.mediaForSelected,
                            actualQuestion = emptySampleData.actualQuestion,
                            totalQuestions = emptySampleData.totalQuestions,
                            listTypeMedia = emptySampleData.listTypeMedia, // <--- Pasa la lista vacía aquí
                            guideContext = emptySampleData.guideContext,
                            showDialogDeleteQuestion = emptySampleData.showDialogDeleteQuestion,
                            showDialogRepeatGuide = emptySampleData.showDialogRepeatGuide,
                            currentPosContent = 0,
                            onDissmissDialogRepeatGuide = {},
                            onConfirmDialogRepeatGuide = {},
                            onContinueDialogDeleteQuestionClick = {},
                            onBackQuestionClick = {},
                            onNextQuestionClick = {},
                            onDeleteQuestionClick = {},
                            onCardTypeClicked = {},
                            onFilterTypeClicked = {},
                            onOpenAssetClick = { _, _ -> },
                            onDeleteItemClick = { _, _ -> },
                            onAddAssetClick = {
                                textValueState = TextFieldValue("")
                                isNavigatedToCreateText = true
                            },
                            onAddQuestion = {},
                            onCloseGuide = {},
                            onCurrentPosContent = {},
                            onDismissRequest = {}
                        )
                    } else {
                        CreateTextScreen(
                            guideContext = GuideContext.Creating(guideDomainModel),
                            onSaveText = { _, _ -> },
                            colorInitial = MaterialTheme.colorScheme.onSurface,
                            selectedColor = MaterialTheme.colorScheme.onSurface,
                            textValue = textValueState,
                            showDialog = false,
                            onClearColorClick = {},
                            onShowColorDialog = {},
                            onChangeTextValue = { textValueState = it },
                            onDissmissDialog = {},
                            onColorSelected = {},
                            onDefaultColor = {},
                            onBackNav = { isNavigatedToCreateText = false }
                        )
                    }
                }
            }
        }

        // ACT: Clic en agregar asset sobre la pantalla vacía
        composeTestRule
            .onNodeWithTag("my_add_button", useUnmergedTree = true)
            .performClick()

        // ASSERT: Verifica que la pantalla CreateText se muestre en blanco
        composeTestRule
            .onNodeWithTag("create_text_input")
            .onChildren()
            .filterToOne(hasSetTextAction())
            .assertIsDisplayed()
            .assertTextEquals("")
    }
}