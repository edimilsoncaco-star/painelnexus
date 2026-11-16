package com.nexus.painel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.painel.ui.components.MenuButton
import com.nexus.painel.ui.components.NexusLogo
import com.nexus.painel.ui.theme.NeonRed
import com.nexus.painel.ui.theme.NeonRedDark
import com.nexus.painel.ui.theme.NexusBlack
import com.nexus.painel.ui.theme.NexusGray
import com.nexus.painel.ui.theme.NexusWhite
import kotlinx.coroutines.launch

private enum class Destination(val label: String) {
    Home("Home"),
    Permissions("Permissões"),
    Updates("Atualizações")
}

/** Tela principal: barra superior com botão de menu + menu lateral + abas. */
@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var current by remember { mutableStateOf(Destination.Home) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color(0xAA000000),
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
                drawerContainerColor = Color(0xFF070707),
                drawerContentColor = NexusWhite
            ) {
                DrawerContent(
                    current = current,
                    onSelect = {
                        current = it
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuButton(onClick = { scope.launch { drawerState.open() } })
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "PAINEL NEXUS",
                        color = NeonRed,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = current.label,
                        color = NexusGray,
                        fontSize = 12.sp
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (current) {
                    Destination.Home -> HomeScreen()
                    Destination.Permissions -> PermissionsScreen()
                    Destination.Updates -> UpdatesScreen()
                }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    current: Destination,
    onSelect: (Destination) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 24.dp)
        ) {
            NexusLogo(Modifier.size(64.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "PAINEL NEXUS",
                    color = NeonRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(text = "Versão V1", color = NexusGray, fontSize = 12.sp)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Destination.entries.forEach { destination ->
                DrawerItem(
                    label = destination.label,
                    selected = destination == current,
                    onClick = { onSelect(destination) }
                )
            }
        }
    }
}

@Composable
private fun DrawerItem(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(if (selected) NeonRed.copy(alpha = 0.16f) else NexusBlack)
            .border(1.dp, if (selected) NeonRed else NeonRedDark.copy(alpha = 0.55f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (selected) NeonRed else NeonRedDark)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            color = if (selected) NexusWhite else NexusGray,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
