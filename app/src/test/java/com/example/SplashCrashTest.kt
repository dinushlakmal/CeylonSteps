package com.example

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32])
class SplashCrashTest {

    @Test
    fun testSplashActivityLaunches() {
        Robolectric.buildActivity(SplashActivity::class.java).create().start().resume()
    }
}
