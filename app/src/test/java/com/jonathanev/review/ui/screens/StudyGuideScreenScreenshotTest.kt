package com.jonathanev.review.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jonathanev.review.ui.preview.providers.StudyGuideScreenProv
import com.jonathanev.review.ui.preview.providers.StudyGuideScreenProvider
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.utils.captureTestOutput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [34],
    instrumentedPackages = ["androidx.loader.content"],
    qualifiers = RobolectricDeviceQualifiers.Pixel5
)
class StudyGuideScreenScreenshotTest(
    private val variantName: String,
    private val themeQualifier: String,
    private val data: StudyGuideScreenProv
) {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            val provider = StudyGuideScreenProvider()
            val testCases = mutableListOf<Array<Any>>()

            provider.values.forEachIndexed { index, dataState ->
                val nameScreenshot = provider.getDisplayName(index) ?: "item_$index"

                // MODO CLARO
                testCases.add(
                    arrayOf(
                        "${nameScreenshot}_light",
                        "+notnight",
                        dataState
                    )
                )

                // MODO OSCURO
                testCases.add(
                    arrayOf(
                        "${nameScreenshot}_dark",
                        "+night",
                        dataState
                    )
                )
            }

            return testCases
        }
    }

    @Test
    fun captureVariant() {
        RuntimeEnvironment.setQualifiers(themeQualifier)

        composeTestRule.setContent {
            ReviewTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "PREVIEW: $variantName - ${data::class.simpleName}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        FillingGuideScreen(
                            cardType = data.typeSelected,
                            typeForSelected = data.typeForSelected,
                            mediaSelected = data.mediaSelected,
                            mediaForSelected = data.mediaForSelected,
                            actualQuestion = data.actualQuestion,
                            totalQuestions = data.totalQuestions,
                            listTypeMedia = data.listTypeMedia,
                            guideMode = data.guideMode,
                            showDialogDeleteQuestion = data.showDialogDeleteQuestion,
                            showDialogRepeatGuide = data.showDialogRepeatGuide,
                            currentPosContent = 0,
                            onDissmissDialogRepeatGuide = {},
                            onConfirmDialogRepeatGuide = {},
                            onContinueDialogDeleteQuestionClick = {},
                            onBackQuestionClick = {},
                            onNextQuestionClick = {},
                            onDeleteQuestionClick = { },
                            onCardTypeClicked = {},
                            onFilterTypeClicked = {},
                            onOpenAssetClick = {},
                            onDeleteItemClick = { _, _ -> },
                            onAddAssetClick = {},
                            onAddQuestion = {},
                            onCloseGuide = {},
                            onCurrentPosContent = {},
                            onDismissRequest = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onRoot().captureTestOutput(
            testClassName = this::class.java.simpleName,
            variantName = variantName
        )
    }
}