package com.dc.android.foundation

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Hilt entry point for the whole app (INV-04: constructor injection is the default DI mechanism). */
@HiltAndroidApp
class DcApplication : Application()
