package com.nexus.painel.data

import android.content.Context

/** Guarda (para sempre, até remover) a pasta configurada no Painel ADM. */
object AdminStore {

    private const val PREFS = "nexus_admin"
    private const val KEY_PATH = "folder_path"

    data class SavedFolder(val path: String) {
        val name: String get() = path.trimEnd('/').substringAfterLast('/')
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getFolder(context: Context): SavedFolder? =
        prefs(context).getString(KEY_PATH, null)
            ?.takeIf { it.isNotBlank() }
            ?.let { SavedFolder(it) }

    fun saveFolder(context: Context, path: String) {
        prefs(context).edit().putString(KEY_PATH, path).apply()
    }

    fun clearFolder(context: Context) {
        prefs(context).edit().remove(KEY_PATH).apply()
    }
}
