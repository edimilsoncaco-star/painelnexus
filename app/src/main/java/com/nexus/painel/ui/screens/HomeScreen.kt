package com.nexus.painel.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.painel.overlay.FloatingService
import com.nexus.painel.ui.components.NeonButton
import com.nexus.painel.ui.components.NeonCard
import com.nexus.painel.ui.components.NexusLogo
import com.nexus.painel.ui.components.StatusBadge
import com.nexus.painel.ui.theme.NexusGray
import com.nexus.painel.ui.theme.NexusGreen
import com.nexus.painel.ui.theme.NexusWhite
import com.nexus.painel.ui.theme.NeonRed

/** Aba Home: logo no topo e botão para abrir o painel flutuante. */
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val running by FloatingService.running.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NexusLogo(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
        )

        Spacer(Modifier.height(16.dp))

        NeonCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Painel flutuante",
                    color = NexusWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                if (running) {
                    StatusBadge("ATIVO", NexusGreen)
                } else {
                    StatusBadge("DESLIGADO", NeonRed)
                }
            }
            Text(
                text = "Abre a imagem do Painel Nexus flutuando sobre outros apps. " +
                    "Toque na imagem para abrir o painel.",
                color = NexusGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
            NeonButton(
                text = if (running) "FECHAR FLUTUANTE" else "ABRIR FLUTUANTE",
                onClick = {
                    when {
                        running -> FloatingService.stop(context)

                        !FloatingService.canDrawOverlays(context) -> {
                            Toast.makeText(
                                context,
                                "Permita \"Exibir sobre outros apps\" para o Painel Nexus.",
                                Toast.LENGTH_LONG
                            ).show()
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                        }

                        else -> FloatingService.start(context)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
