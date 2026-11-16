package com.nexus.painel.data

/**
 * Validação da key de acesso.
 * A key é o nome do aplicativo: "Painel Nexus".
 * Maiúsculas, minúsculas, espaços e símbolos são ignorados
 * (ex.: "painel nexus", "PAINELNEXUS" e "Painel Nexus" funcionam).
 */
object KeyValidator {
    private const val APP_KEY = "Painel Nexus"

    private fun normalize(value: String): String =
        value.lowercase().filter { it.isLetterOrDigit() }

    fun isValid(input: String): Boolean = normalize(input) == normalize(APP_KEY)
}
