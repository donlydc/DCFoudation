package com.dc.android.foundation.config

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** Common entry point for Android per-app language preferences. */
object AppLanguageController {

    /** Returns the selected BCP-47 tag, or `null` when the app follows the system language. */
    fun currentLanguageTag(): String? =
        AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()

    /**
     * Applies one BCP-47 language tag. Passing `null` or blank restores the system language.
     * This must be called from the main thread because AppCompat may recreate the activity.
     */
    @MainThread
    fun setApplicationLanguage(languageTag: String?) {
        val locales = languageTag
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let(LocaleListCompat::forLanguageTags)
            ?: LocaleListCompat.getEmptyLocaleList()
        AppCompatDelegate.setApplicationLocales(locales)
    }

    @MainThread
    fun useSystemLanguage() {
        setApplicationLanguage(null)
    }
}
