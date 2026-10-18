package com.dc.android.foundation.config

import app.cash.turbine.test
import com.dc.android.foundation.designsystem.theme.DcUiScale
import com.dc.android.foundation.testing.InMemoryPreferencesDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AppConfigurationRepositoryTest {

    @Test
    fun `configuration starts with safe defaults`() = runTest {
        val repository = AppConfigurationRepository(InMemoryPreferencesDataSource())

        repository.configuration.test {
            assertEquals(AppConfiguration(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `individual setters publish typed configuration`() = runTest {
        val repository = AppConfigurationRepository(InMemoryPreferencesDataSource())

        repository.setThemeMode(AppThemeMode.Dark)
        repository.setUiScale(DcUiScale.Comfortable)
        repository.setDynamicColor(true)

        repository.configuration.test {
            assertEquals(
                AppConfiguration(
                    themeMode = AppThemeMode.Dark,
                    uiScale = DcUiScale.Comfortable,
                    dynamicColor = true
                ),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `update and reset replace all application configuration`() = runTest {
        val repository = AppConfigurationRepository(InMemoryPreferencesDataSource())
        repository.update(
            AppConfiguration(
                themeMode = AppThemeMode.Light,
                uiScale = DcUiScale.Compact,
                dynamicColor = true
            )
        )

        repository.reset()

        repository.configuration.test {
            assertEquals(AppConfiguration(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `theme mode resolves against system only when requested`() {
        assertEquals(true, AppThemeMode.System.resolveDarkTheme(systemInDarkTheme = true))
        assertEquals(false, AppThemeMode.System.resolveDarkTheme(systemInDarkTheme = false))
        assertEquals(false, AppThemeMode.Light.resolveDarkTheme(systemInDarkTheme = true))
        assertEquals(true, AppThemeMode.Dark.resolveDarkTheme(systemInDarkTheme = false))
    }
}
