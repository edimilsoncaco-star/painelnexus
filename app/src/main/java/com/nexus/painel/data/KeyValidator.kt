package com.nexus.painel.data

import java.security.MessageDigest

/**
 * Validação da key de acesso.
 *
 * - Key comum: o nome do aplicativo, "Painel Nexus" (maiúsculas, espaços e símbolos são ignorados).
 * - Key ADM: abre o Painel ADM. Guardada só como hash SHA-256 (não aparece em texto no código).
 */
object KeyValidator {
    private const val APP_KEY = "Painel Nexus"
    private const val ADMIN_KEY_SHA256 =
        "4d479cf15f1894008ddf67d438087a5e827093f28b5328d06fa6dd34de8cb0f2"

    private fun normalize(value: String): String =
        value.lowercase().filter { it.isLetterOrDigit() }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    fun isValid(input: String): Boolean = normalize(input) == normalize(APP_KEY)

    fun isAdmin(input: String): Boolean =
        sha256(input.trim().lowercase()) == ADMIN_KEY_SHA256
}
