package com.nexus.painel.admin

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.DocumentsContract
import com.nexus.painel.IFileService
import com.nexus.painel.IProgressCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku

/** Liga o app ao [FileService] que roda com permissão do Shizuku. */
object FileServiceClient {

    const val DATA_ROOT = "/storage/emulated/0/Android/data"
    const val GAME_PACKAGE = "com.dts.freefireth"
    const val GAME_DATA_DIR = "$DATA_ROOT/$GAME_PACKAGE"

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    @Volatile
    private var service: IFileService? = null
    private var args: Shizuku.UserServiceArgs? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            if (binder != null && binder.pingBinder()) {
                service = IFileService.Stub.asInterface(binder)
                _connected.value = true
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            _connected.value = false
        }
    }

    fun bind(context: Context) {
        if (args != null) return
        val userServiceArgs = Shizuku.UserServiceArgs(
            ComponentName(context.packageName, FileService::class.java.name)
        )
            .processNameSuffix("files")
            .daemon(false)
            .version(2)
        args = userServiceArgs
        try {
            Shizuku.bindUserService(userServiceArgs, connection)
        } catch (e: Throwable) {
            args = null
        }
    }

    fun unbind() {
        val current = args ?: return
        try {
            Shizuku.unbindUserService(current, connection, true)
        } catch (_: Throwable) {
        }
        args = null
        service = null
        _connected.value = false
    }

    /** Espera o serviço do Shizuku conectar (true se conectou dentro do tempo). */
    suspend fun awaitConnected(timeoutMillis: Long): Boolean =
        withTimeoutOrNull(timeoutMillis) { connected.first { it } } != null

    /** Copia [source] para dentro de [destDir]. [report] recebe 0..100 na thread principal. */
    suspend fun transfer(source: String, destDir: String, report: (Int) -> Unit): String =
        withContext(Dispatchers.IO) {
            val current = service ?: return@withContext "ERRO: serviço do Shizuku não conectado."
            val main = Handler(Looper.getMainLooper())
            val callback = object : IProgressCallback.Stub() {
                override fun onProgress(percent: Int) {
                    main.post { report(percent) }
                }
            }
            try {
                current.transfer(source, destDir, callback)
            } catch (e: Throwable) {
                "ERRO: ${e.message}"
            }
        }

    /** Converte a pasta escolhida no seletor (content://...) em caminho real. */
    fun treeUriToPath(uri: Uri): String? {
        val documentId = try {
            DocumentsContract.getTreeDocumentId(uri)
        } catch (e: Exception) {
            return null
        }
        val parts = documentId.split(":", limit = 2)
        val volume = parts[0]
        val relative = parts.getOrElse(1) { "" }
        val base = if (volume.equals("primary", ignoreCase = true)) {
            "/storage/emulated/0"
        } else {
            "/storage/$volume"
        }
        return if (relative.isEmpty()) base else "$base/$relative"
    }
}
