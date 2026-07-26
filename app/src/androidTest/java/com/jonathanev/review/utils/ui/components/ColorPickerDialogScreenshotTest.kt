package com.jonathanev.review.utils.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.jonathanev.review.ui.components.ColorPickerDialogContent
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
    instrumentedPackages = ["androidx.loader.content"]
)
class ColorPickerDialogScreenshotTest(
    private val variantName: String,
    private val themeQualifier: String
) {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            val testCases = mutableListOf<Array<Any>>()

            // MODO CLARO
            testCases.add(
                arrayOf(
                    "light",
                    "notnight"
                )
            )

            // MODO OSCURO
            testCases.add(
                arrayOf(
                    "dark",
                    "night"
                )
            )

            return testCases
        }
    }

    @Test
    fun captureVariant() {
        RuntimeEnvironment.setQualifiers(themeQualifier)

        composeTestRule.setContent {
            ReviewTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val controller = rememberColorPickerController()

                    Column {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "PREVIEW: $variantName",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        ColorPickerDialogContent(
                            controller = controller,
                            colorInitial = MaterialTheme.colorScheme.onSurface,
                            selectedColor = MaterialTheme.colorScheme.onSurface,
                            onDismissRequest = {},
                            onColorSelected = {},
                            onDefaultClick = {}
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