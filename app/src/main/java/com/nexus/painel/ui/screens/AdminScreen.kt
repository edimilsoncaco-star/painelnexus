package com.nexus.painel.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.painel.admin.FileServiceClient
import com.nexus.painel.data.AdminStore
import com.nexus.painel.permissions.ShizukuHelper
import com.nexus.painel.permissions.rememberShizukuStatus
import com.nexus.painel.ui.components.NeonButton
import com.nexus.painel.ui.components.NeonCard
import com.nexus.painel.ui.components.StatusBadge
import com.nexus.painel.ui.theme.NeonRed
import com.nexus.painel.ui.theme.NeonRedSoft
import com.nexus.painel.ui.theme.NexusGray
import com.nexus.painel.ui.theme.NexusGreen
import com.nexus.painel.ui.theme.NexusWhite

/**
 * Painel ADM: define a pasta que será enviada para Android/data/com.dts.freefireth
 * sempre que o usuário tocar em "Abrir flutuante". A pasta fica salva até ser removida.
 */
@Composable
fun AdminScreen(onExit: () -> Unit) {
    val context = LocalContext.current
    val shizuku = rememberShizukuStatus()
    var saved by remember { mutableStateOf(AdminStore.getFolder(context)) }
    var notice by remember { mutableStateOf<String?>(null) }

    BackHandler(onBack = onExit)

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val path = FileServiceClient.treeUriToPath(uri)
            if (path == null) {
                notice = "Não consegui ler essa pasta. Escolha outra."
            } else {
                AdminStore.saveFolder(context, path)
                saved = AdminStore.getFolder(context)
                notice = "Pasta salva. Ela fica guardada até você remover."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "PAINEL ADM",
                    color = NeonRed,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(text = "Painel Nexus V1", color = NexusGray, fontSize = 12.sp)
            }
            NeonButton(
                text = "SAIR",
                onClick = onExit,
                height = 44.dp,
                modifier = Modifier.width(96.dp)
            )
        }

        // ---- Pasta do jogo
        NeonCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pasta do Free Fire",
                    color = NexusWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (saved != null) {
                    StatusBadge("SALVA", NexusGreen)
                } else {
                    StatusBadge("VAZIO", NeonRed)
                }
            }

            Text(
                text = "Destino: Android/data/${FileServiceClient.GAME_PACKAGE}",
                color = NexusGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            val current = saved
            if (current != null) {
                Text(
                    text = current.name,
                    color = NexusWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 14.dp)
                )
                Text(
                    text = current.path,
                    color = NexusGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                Text(
                    text = "Nenhuma pasta adicionada ainda.",
                    color = NexusGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            NeonButton(
                text = if (current == null) "ADICIONAR PASTA" else "TROCAR PASTA",
                onClick = { picker.launch(null) },
                modifier = Modifier.fillMaxWidth(),
                height = 50.dp
            )
            if (current != null) {
                Spacer(Modifier.height(10.dp))
                NeonButton(
                    text = "REMOVER PASTA",
                    onClick = {
                        AdminStore.clearFolder(context)
                        saved = null
                        notice = "Pasta removida. Nenhum arquivo foi apagado."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    height = 50.dp
                )
            }

            notice?.let {
                Text(
                    text = it,
                    color = NeonRedSoft,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        // ---- Como funciona
        NeonCard {
            Text(
                text = "COMO FUNCIONA",
                color = NeonRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "Sempre que tocar em Abrir flutuante na Home, o app copia esta pasta " +
                    "para o Free Fire, mostra o progresso e depois libera o botão Iniciar jogo. " +
                    "A pasta original nunca é apagada.",
                color = NexusGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // ---- Status do Shizuku
        NeonCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Shizuku",
                    color = NexusWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                if (shizuku == ShizukuHelper.Status.GRANTED) {
                    StatusBadge("PERMITIDO", NexusGreen)
                } else {
                    StatusBadge("PENDENTE", NeonRed)
                }
            }
            val info = when (shizuku) {
                ShizukuHelper.Status.GRANTED -> "Pronto para enviar a pasta."
                ShizukuHelper.Status.NEEDS_PERMISSION -> "Permita o Shizuku para liberar o acesso a Android/data."
                ShizukuHelper.Status.DENIED -> "Acesso negado. Libere o Painel Nexus dentro do app Shizuku."
                ShizukuHelper.Status.NOT_RUNNING -> "Inicie o Shizuku e volte aqui."
                ShizukuHelper.Status.NOT_INSTALLED -> "Instale e inicie o Shizuku."
            }
            Text(
                text = info,
                color = NexusGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (shizuku == ShizukuHelper.Status.NEEDS_PERMISSION) {
                Spacer(Modifier.height(14.dp))
                NeonButton(
                    text = "PERMITIR SHIZUKU",
                    onClick = { ShizukuHelper.requestPermission() },
                    modifier = Modifier.fillMaxWidth(),
                    height = 50.dp
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
