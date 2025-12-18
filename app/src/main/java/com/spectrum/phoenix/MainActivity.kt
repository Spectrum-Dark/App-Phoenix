package com.spectrum.phoenix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.spectrum.phoenix.ui.navigation.AppNavigation
import com.spectrum.phoenix.ui.theme.PhoenixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhoenixTheme {
                AppNavigation()
            }
        }
    }
}