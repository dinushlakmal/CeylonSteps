package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.components.StatsHeaderCard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TripStats
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun stats_header_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        StatsHeaderCard(
          stats = TripStats(
            totalDistanceKm = 480.0,
            visitedCount = 4,
            upcomingCount = 2,
            totalFootprints = 6,
            uniqueProvinces = 4
          )
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/stats_header.png")
  }
}
