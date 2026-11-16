package com.nexus.painel.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.nexus.painel.permissions.ShizukuHelper
import com.nexus.painel.ui.components.NeonButton
import com.nexus.painel.ui.components.NeonCard
import com.nexus.painel.ui.components.SectionTitle
import com.nexus.painel.ui.components.StatusBadge
import com.nexus.painel.ui.theme.NeonRed
import com.nexus.painel.ui.theme.NeonRedSoft
import com.nexus.painel.ui.theme.NexusGray
import com.nexus.painel.ui.theme.NexusGreen
import com.nexus.painel.ui.theme.NexusWhite
import rikka.shizuku.Shizuku

/** Aba Permissões: Shizuku, sobreposição (flutuante) e notificações. */
@Composable
fun PermissionsScreen() {
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }

    // Reavalia as permissões sempre que o app volta para a tela.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { tick++ }

    // Reage a mudanças do Shizuku (serviço iniciado/parado, permissão concedida/negada).
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

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { tick++ }

    val shizuku = remember(tick) { ShizukuHelper.status(context) }
    val overlayGranted = remember(tick) { Settings.canDrawOverlays(context) }
    val notificationsGranted = remember(tick) {
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun openShizukuApp() {
        val intent = context.packageManager.getLaunchIntentForPackage(ShizukuHelper.PACKAGE)
        if (intent != null) context.startActivity(intent)
    }

    fun openShizukuDownload() {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(ShizukuHelper.DOWNLOAD_URL))
            )
        } catch (_: ActivityNotFoundException) {
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle("Permissões", "Libere o que o Painel Nexus precisa para funcionar.")

        // ---- Shizuku
        val shizukuInfo: Triple<String, Color, String> = when (shizuku) {
            ShizukuHelper.Status.GRANTED ->
                Triple("PERMITIDO", NexusGreen, "O Painel Nexus tem acesso ao Shizuku.")
            ShizukuHelper.Status.NEEDS_PERMISSION ->
                Triple("PENDENTE", NeonRed, "O Shizuku está rodando. Toque em permitir para liberar o acesso.")
            ShizukuHelper.Status.DENIED ->
                Triple("NEGADO", NeonRedSoft, "Acesso negado. Libere o Painel Nexus dentro do app Shizuku.")
            ShizukuHelper.Status.NOT_RUNNING ->
                Triple("PARADO", NeonRedSoft, "O Shizuku está instalado, mas não está em execução. Inicie-o primeiro.")
            ShizukuHelper.Status.NOT_INSTALLED ->
                Triple("NÃO INSTALADO", NeonRedSoft, "Instale e inicie o Shizuku para poder permitir.")
        }

        PermissionCard(
            title = "Shizuku",
            description = shizukuInfo.third,
            badgeText = shizukuInfo.first,
            badgeColor = shizukuInfo.second,
            buttonText = when (shizuku) {
                ShizukuHelper.Status.GRANTED -> "PERMITIDO"
                ShizukuHelper.Status.NEEDS_PERMISSION -> "PERMITIR SHIZUKU"
                ShizukuHelper.Status.DENIED -> "ABRIR SHIZUKU"
                ShizukuHelper.Status.NOT_RUNNING -> "ABRIR SHIZUKU"
                ShizukuHelper.Status.NOT_INSTALLED -> "BAIXAR SHIZUKU"
            },
            buttonEnabled = shizuku != ShizukuHelper.Status.GRANTED,
            onClick = {
                when (shizuku) {
                    ShizukuHelper.Status.NEEDS_PERMISSION -> ShizukuHelper.requestPermission()
                    ShizukuHelper.Status.DENIED,
                    ShizukuHelper.Status.NOT_RUNNING -> openShizukuApp()
                    ShizukuHelper.Status.NOT_INSTALLED -> openShizukuDownload()
                    ShizukuHelper.Status.GRANTED -> Unit
                }
            }
        )

        // ---- Sobreposição
        PermissionCard(
            title = "Exibir sobre outros apps",
            description = "Necessária para mostrar o painel flutuante por cima de outros aplicativos.",
            badgeText = if (overlayGranted) "PERMITIDO" else "PENDENTE",
            badgeColor = if (overlayGranted) NexusGreen else NeonRed,
            buttonText = if (overlayGranted) "PERMITIDO" else "PERMITIR",
            buttonEnabled = !overlayGranted,
            onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                )
            }
        )

        // ---- Notificações (Android 13+)
        if (Build.VERSION.SDK_INT >= 33) {
            PermissionCard(
                title = "Notificações",
                description = "Mostra o aviso fixo enquanto o painel flutuante estiver ativo.",
                badgeText = if (notificationsGranted) "PERMITIDO" else "PENDENTE",
                badgeColor = if (notificationsGranted) NexusGreen else NeonRed,
                buttonText = if (notificationsGranted) "PERMITIDO" else "PERMITIR",
                buttonEnabled = !notificationsGranted,
                onClick = {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    badgeText: String,
    badgeColor: Color,
    buttonText: String,
    buttonEnabled: Boolean,
    onClick: () -> Unit
) {
    NeonCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = NexusWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            StatusBadge(badgeText, badgeColor)
        }
        Text(
            text = description,
            color = NexusGray,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 14.dp)
        )
        NeonButton(
            text = buttonText,
            onClick = onClick,
            enabled = buttonEnabled,
            modifier = Modifier.fillMaxWidth(),
            height = 50.dp
        )
    }
}
