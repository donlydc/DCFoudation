package com.dc.android.foundation.greeting

import javax.inject.Inject

/**
 * Trivial constructor-injected collaborator — proves Hilt wiring (Phase D) without inventing
 * a repository/network/database this project doesn't have yet (that's Phase G's job).
 */
interface GreetingProvider {
    fun greetingFor(name: String): String
}

class DefaultGreetingProvider @Inject constructor() : GreetingProvider {
    override fun greetingFor(name: String): String = "Hello $name!"
}
