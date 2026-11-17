package com.nexus.painel.permissions

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import rikka.shizuku.Shizuku

/** Status do Shizuku que se atualiza sozinho (resume do app + eventos do Shizuku). */
@Composable
fun rememberShizukuStatus(): ShizukuHelper.Status {
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { tick++ }

    DisposableEffect(Unit) {
        val main = Handler(Looper.getMainLooper())
        val onBinderReceived = Shizuku.OnBinderReceivedListener { main.post { tick++ } }
        val onBinderDead = Shizuku.OnBinderDeadListener { main.post { tick++ } }
        val onResult = Shizuku.OnRequestPermissionResultListener { _, _ -> main.post { tick++ } }

        Shizuku.addBinderReceivedListenerSticky(onBinderReceived)
        Shizuku.addBinderDeadListener(onBinderDead)
        Shizuku.addRequestPermissionResultListener(onResult)

        onDispose {
            Shizuku.removeBinderReceivedListener(onBinderReceived)
            Shizuku.removeBinderDeadListener(onBinderDead)
            Shizuku.removeRequestPermissionResultListener(onResult)
        }
    }

    return remember(tick) { ShizukuHelper.status(context) }
}
