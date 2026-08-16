package com.dc.android.foundation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dc.android.foundation.articles.ArticlesRoute
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.greeting.GreetingRoute
import com.dc.android.foundation.platform.navigation.Destination
import dagger.hilt.android.AndroidEntryPoint

/**
 * App shell: theme + a real `NavHost` switching between the app's two top-level destinations
 * (06-navigation.md NAV-01). `:app` is the composition root, so it is the one place allowed to
 * touch `androidx.navigation.*` directly; feature code (`greeting`/`articles`) never does.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DcTheme {
                DcNavHost()
            }
        }
    }
}

private data class TopLevelDestination(
    val destination: Destination,
    val route: String,
    val label: String,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Destination.Greeting, Destination.Greeting::class.qualifiedName!!, "Greeting"),
    TopLevelDestination(Destination.Articles, Destination.Articles::class.qualifiedName!!, "Articles"),
)

@Composable
private fun DcNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route

            NavigationBar {
                topLevelDestinations.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.destination) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {},
                        label = { Text(item.label) },
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Greeting,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding),
        ) {
            composable<Destination.Greeting> { GreetingRoute() }
            composable<Destination.Articles> { ArticlesRoute() }
        }
    }
}
