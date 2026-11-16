package com.nexus.painel.permissions

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

/** Centraliza a lógica de permissão do Shizuku. */
object ShizukuHelper {

    const val PACKAGE = "moe.shizuku.privileged.api"
    const val REQUEST_CODE = 4242
    const val DOWNLOAD_URL = "https://shizuku.rikka.app/download/"

    enum class Status {
        NOT_INSTALLED,
        NOT_RUNNING,
        NEEDS_PERMISSION,
        DENIED,
        GRANTED
    }

    private fun isInstalled(context: Context): Boolean = try {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(PACKAGE, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    fun status(context: Context): Status {
        if (!isInstalled(context)) return Status.NOT_INSTALLED
        return try {
            when {
                !Shizuku.pingBinder() -> Status.NOT_RUNNING
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> Status.GRANTED
                Shizuku.shouldShowRequestPermissionRationale() -> Status.DENIED
                else -> Status.NEEDS_PERMISSION
            }
        } catch (e: Throwable) {
            Status.NOT_RUNNING
        }
    }

    fun requestPermission() {
        try {
            Shizuku.requestPermission(REQUEST_CODE)
        } catch (e: Throwable) {
            // Shizuku indisponível no momento; o status será reavaliado ao voltar para o app.
        }
    }
}
