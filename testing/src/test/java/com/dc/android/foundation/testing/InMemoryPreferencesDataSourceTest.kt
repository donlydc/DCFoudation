package com.dc.android.foundation.testing

import com.dc.android.foundation.data.preferences.PreferenceKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryPreferencesDataSourceTest {
    private val theme = PreferenceKey.StringKey("theme", "system")
    private val onboarding = PreferenceKey.BooleanKey("onboarding", false)

    @Test
    fun `typed keys expose defaults and edit commits related values together`() = runTest {
        val source = InMemoryPreferencesDataSource()

        assertEquals("system", source.observe(theme).first())
        source.edit {
            set(theme, "dark")
            set(onboarding, true)
        }

        assertEquals("dark", source.get(theme))
        assertEquals(true, source.get(onboarding))
    }
}
