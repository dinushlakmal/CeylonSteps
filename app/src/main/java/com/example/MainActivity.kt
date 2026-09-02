package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.ThemeMode
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TripViewModel

class MainActivity : ComponentActivity() {

    private val tripViewModel: TripViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by tripViewModel.themeMode.collectAsStateWithLifecycle()
            val appThemeType by tripViewModel.appThemeType.collectAsStateWithLifecycle()
            val isUserLoggedIn by tripViewModel.isUserLoggedIn.collectAsStateWithLifecycle()

            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MyApplicationTheme(darkTheme = isDark, appThemeType = appThemeType) {
                Crossfade(targetState = isUserLoggedIn, label = "auth_crossfade") { loggedIn ->
                    if (loggedIn) {
                        MainScreen(viewModel = tripViewModel)
                    } else {
                        LoginScreen(
                            viewModel = tripViewModel,
                            onLoginSuccess = {
                                // User logged in, StateFlow will auto update
                            }
                        )
                    }
                }
            }
        }
    }
}
