package com.dc.android.foundation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.greeting.GreetingRoute
import dagger.hilt.android.AndroidEntryPoint

/** App shell: theme + the single current destination. See 06-navigation.md for why there's no NavHost yet. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DcTheme {
                GreetingRoute()
            }
        }
    }
}