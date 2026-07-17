package com.jonathanev.review.ui.components

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
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.roborazziSystemPropertyOutputDirectory
import com.jonathanev.review.ui.preview.providers.FilterChipItemProv
import com.jonathanev.review.ui.preview.providers.FilterChipItemProvider
import com.jonathanev.review.ui.theme.ReviewTheme
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
class FilterChipItemScreenshotTest(
    private val variantName: String,
    private val themeQualifier: String,
    private val data: FilterChipItemProv
) {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            val provider = FilterChipItemProvider()
            val testCases = mutableListOf<Array<Any>>()

            provider.values.forEach { dataState ->
                // MODO CLARO
                testCases.add(
                    arrayOf(
                        "${dataState}_light",
                        "notnight",
                        dataState
                    )
                )

                // MODO OSCURO
                testCases.add(
                    arrayOf(
                        "${dataState}_dark",
                        "night",
                        dataState
                    )
                )
            }

            return testCases
        }
    }

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun captureFilterChipItemVariant() {
        RuntimeEnvironment.setQualifiers(themeQualifier)

        val basePath = "build/outputs/roborazzi/success/FilterChipItemScreenshotTest"
        val failurePath = "build/outputs/roborazzi/failures/FilterChipItemScreenshotTest"

        val roborazziOptions = RoborazziOptions(
            compareOptions = RoborazziOptions.CompareOptions(
                outputDirectoryPath = failurePath
            )
        )

        composeTestRule.setContent {
            ReviewTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "PREVIEW: $data - ${data::class.simpleName}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        FilterChipItem(
                            itemContentType = data.itemContentType,
                            iconRes = data.iconRes,
                            contentTypeSelected = data.contentTypeSelected,
                            onFilterClicked = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "$basePath/${variantName}.png",
            roborazziOptions = roborazziOptions
        )
    }
}