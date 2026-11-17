package com.nexus.painel.admin

import com.nexus.painel.IFileService
import com.nexus.painel.IProgressCallback
import java.io.File
import kotlin.system.exitProcess

/**
 * Roda dentro do processo do Shizuku (uid shell), por isso consegue
 * acessar /storage/emulated/0/Android/data. NÃO usar Context aqui.
 */
class FileService() : IFileService.Stub() {

    override fun destroy() {
        exitProcess(0)
    }

    override fun transfer(source: String, destDir: String, callback: IProgressCallback?): String {
        return try {
            val src = File(source)
            if (!src.exists() || !src.isDirectory) {
                return "ERRO: pasta de origem não encontrada:\n$source\nAdicione a pasta de novo no Painel ADM."
            }

            val target = File(destDir, src.name)
            if (target.canonicalPath == src.canonicalPath ||
                target.canonicalPath.startsWith(src.canonicalPath + File.separator)
            ) {
                return "ERRO: origem e destino são a mesma pasta."
            }

            if (!target.exists() && !target.mkdirs()) {
                return "ERRO: não consegui criar o destino:\n${target.absolutePath}"
            }

            val files = src.walkTopDown().filter { it.isFile }.toList()
            val total = files.sumOf { it.length() }.coerceAtLeast(1L)

            // Estrutura de pastas (inclui pastas vazias).
            src.walkTopDown().filter { it.isDirectory }.forEach {
                File(target, it.relativeTo(src).path).mkdirs()
            }

            var done = 0L
            var last = -1
            var copied = 0
            var failed = 0
            val buffer = ByteArray(64 * 1024)

            for (file in files) {
                val out = File(target, file.relativeTo(src).path)
                try {
                    out.parentFile?.mkdirs()
                    file.inputStream().use { input ->
                        out.outputStream().use { output ->
                            while (true) {
                                val read = input.read(buffer)
                                if (read < 0) break
                                output.write(buffer, 0, read)
                                done += read
                                val percent = (done * 100 / total).toInt().coerceIn(0, 100)
                                if (percent != last) {
                                    last = percent
                                    try {
                                        callback?.onProgress(percent)
                                    } catch (_: Throwable) {
                                    }
                                }
                            }
                        }
                    }
                    copied++
                } catch (e: Exception) {
                    failed++
                }
            }

            if (failed == 0) {
                "OK: $copied arquivo(s) enviados para\n${target.absolutePath}"
            } else {
                "PARCIAL: $copied arquivo(s) enviados e $failed falharam em\n${target.absolutePath}"
            }
        } catch (e: Throwable) {
            "ERRO: ${e.message}"
        }
    }
}
