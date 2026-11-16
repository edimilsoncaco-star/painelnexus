package com.nexus.painel.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.painel.data.KeyValidator
import com.nexus.painel.ui.components.NeonButton
import com.nexus.painel.ui.components.NeonTextField
import com.nexus.painel.ui.components.NexusLogo
import com.nexus.painel.ui.theme.NeonRed
import com.nexus.painel.ui.theme.NeonRedSoft
import com.nexus.painel.ui.theme.NexusGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Tela de acesso: logo no topo, caixa de texto e botão "Prosseguir". */
@Composable
fun KeyScreen(onAuthenticated: () -> Unit) {
    var key by remember { mutableStateOf("") }
    var verifying by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager: FocusManager = LocalFocusManager.current
    val shake = remember { Animatable(0f) }

    fun submit() {
        if (verifying) return
        focusManager.clearFocus()
        error = false
        verifying = true
        scope.launch {
            delay(1600) // "verificando a key..."
            if (KeyValidator.isValid(key)) {
                onAuthenticated()
            } else {
                verifying = false
                error = true
                // animação de tremida na caixa
                repeat(3) {
                    shake.animateTo(14f, tween(45))
                    shake.animateTo(-14f, tween(45))
                }
                shake.animateTo(0f, tween(45))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 28.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NexusLogo(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .heightIn(min = 90.dp, max = 280.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Digite a sua key para prosseguir",
            color = NeonRed,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        NeonTextField(
            value = key,
            onValueChange = {
                key = it
                if (error) error = false
            },
            modifier = Modifier.offset(x = shake.value.dp),
            placeholder = "Digite sua key",
            enabled = !verifying,
            isError = error,
            onDone = { submit() }
        )

        AnimatedVisibility(visible = error) {
            Text(
                text = "Key inválida. Tente novamente.",
                color = NeonRedSoft,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(18.dp))

        NeonButton(
            text = "PROSSEGUIR",
            onClick = { submit() },
            modifier = Modifier.fillMaxWidth(),
            loading = verifying
        )

        AnimatedVisibility(visible = verifying) {
            Text(
                text = "Verificando key...",
                color = NexusGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
