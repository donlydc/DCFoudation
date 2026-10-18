package com.dc.android.foundation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dc.android.foundation.articles.ArticlesRoute
import com.dc.android.foundation.config.AppConfiguration
import com.dc.android.foundation.config.AppConfigurationAction
import com.dc.android.foundation.config.AppConfigurationScreen
import com.dc.android.foundation.config.AppConfigurationViewModel
import com.dc.android.foundation.config.AppEnvironment
import com.dc.android.foundation.config.AppEnvironmentProvider
import com.dc.android.foundation.designsystem.catalog.DesignSystemCatalog
import com.dc.android.foundation.designsystem.components.DcNavigationItem
import com.dc.android.foundation.designsystem.components.DcNavigationSuite
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.greeting.GreetingRoute
import com.dc.android.foundation.platform.navigation.Destination
import com.dc.android.foundation.showcase.trip.TripShowcaseHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * App shell: theme + a real `NavHost` switching between the app's two top-level destinations
 * (06-navigation.md NAV-01). `:app` is the composition root, so it is the one place allowed to
 * touch `androidx.navigation.*` directly; feature code (`greeting`/`articles`) never does.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DcAppRoot()
        }
    }
}

private const val DESIGN_SYSTEM_CATALOG_ROUTE = "design-system-catalog"
private const val TRIP_SHOWCASE_ROUTE = "trip-showcase"
private const val MORE_ROUTE = "more"

@Composable
private fun DcAppRoot(
    viewModel: AppConfigurationViewModel = hiltViewModel()
) {
    val configuration by viewModel.configuration.collectAsStateWithLifecycle()
    DcTheme(
        darkTheme = configuration.themeMode.resolveDarkTheme(isSystemInDarkTheme()),
        dynamicColor = configuration.dynamicColor,
        uiScale = configuration.uiScale
    ) {
        DcNavHost(configuration, viewModel::onAction)
    }
}

@Composable
private fun DcNavHost(
    configuration: AppConfiguration,
    onConfigurationAction: (AppConfigurationAction) -> Unit
) {
    val navController = rememberNavController()
    val isDebug = AppEnvironmentProvider.current == AppEnvironment.DEBUG
    val navigationItems = buildList {
        add(DcNavigationItem("greeting", stringResource(R.string.nav_greeting), Icons.Filled.Home))
        add(DcNavigationItem("articles", stringResource(R.string.nav_articles), Icons.Filled.Check))
        add(DcNavigationItem("more", stringResource(R.string.nav_settings), Icons.Filled.Settings))
        if (isDebug) {
            add(DcNavigationItem("catalog", stringResource(R.string.nav_catalog), Icons.Filled.Search))
            add(DcNavigationItem("showcase", stringResource(R.string.nav_showcase), Icons.Filled.Warning))
        }
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedId = when (currentRoute) {
        Destination.Greeting::class.qualifiedName -> "greeting"
        Destination.Articles::class.qualifiedName -> "articles"
        DESIGN_SYSTEM_CATALOG_ROUTE -> "catalog"
        TRIP_SHOWCASE_ROUTE -> "showcase"
        else -> "more"
    }
    val selectedItem = navigationItems.first { it.id == selectedId }

    DcNavigationSuite(
        items = navigationItems,
        selected = selectedItem,
        onSelect = { item ->
            val options: androidx.navigation.NavOptionsBuilder.() -> Unit = {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            when (item.id) {
                "greeting" -> navController.navigate(Destination.Greeting, options)
                "articles" -> navController.navigate(Destination.Articles, options)
                "catalog" -> navController.navigate(DESIGN_SYSTEM_CATALOG_ROUTE, options)
                "showcase" -> navController.navigate(TRIP_SHOWCASE_ROUTE, options)
                else -> navController.navigate(MORE_ROUTE, options)
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Destination.Greeting,
        ) {
            composable<Destination.Greeting> { GreetingRoute() }
            composable<Destination.Articles> { ArticlesRoute() }
            composable(MORE_ROUTE) {
                AppConfigurationScreen(configuration, onConfigurationAction)
            }
            if (isDebug) {
                composable(DESIGN_SYSTEM_CATALOG_ROUTE) { DesignSystemCatalog() }
                composable(TRIP_SHOWCASE_ROUTE) { TripShowcaseHost() }
            }
        }
    }
}
