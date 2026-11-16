package com.nexus.painel.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.painel.ui.components.NeonCard
import com.nexus.painel.ui.components.NeonDot
import com.nexus.painel.ui.components.SectionTitle
import com.nexus.painel.ui.components.StatusBadge
import com.nexus.painel.ui.theme.NeonRed
import com.nexus.painel.ui.theme.NexusGray
import com.nexus.painel.ui.theme.NexusWhite

/** Aba Atualizações: novidades em breve + aviso. */
@Composable
fun UpdatesScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle("Atualizações", "Fique de olho nas novidades do Painel Nexus.")

        NeonCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Atualização",
                    color = NexusWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge("EM BREVE", NeonRed)
            }
            Text(
                text = "Atualização em breve. Novos recursos chegam por aqui.",
                color = NexusGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        NeonCard {
            UpdateItem("Discord")
            Spacer(Modifier.height(14.dp))
            UpdateItem("YouTube")
        }

        NeonCard {
            Text(
                text = "AVISO",
                color = NeonRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "Esta é a versão V1 de teste do Painel Nexus. " +
                    "Discord, YouTube e outras novidades serão liberados em breve " +
                    "em atualizações futuras.",
                color = NexusGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun UpdateItem(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeonDot()
        Spacer(Modifier.width(12.dp))
        Text(
            text = name,
            color = NexusWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        StatusBadge("EM BREVE", NeonRed)
    }
}
