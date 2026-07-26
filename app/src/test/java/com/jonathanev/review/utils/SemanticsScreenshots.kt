package com.jonathanev.review.utils

import androidx.compose.ui.test.SemanticsNodeInteraction
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage

@OptIn(ExperimentalRoborazziApi::class)
fun SemanticsNodeInteraction.captureTestOutput(
    testClassName: String,
    variantName: String
) {
    //val baseDirectory = "build/outputs/roborazzi/success/$testClassName"
    val baseDirectory = "src/test/resources/screenshots/$testClassName"
    val failureDirectory = "build/outputs/roborazzi/failures/$testClassName"

    val roborazziOptions = RoborazziOptions(
        compareOptions = RoborazziOptions.CompareOptions(
            outputDirectoryPath = failureDirectory
        )
    )

    this.captureRoboImage(
        filePath = "$baseDirectory/${variantName}.png",
        roborazziOptions = roborazziOptions
    )
}