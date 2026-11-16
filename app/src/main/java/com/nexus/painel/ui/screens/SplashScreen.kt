package com.nexus.painel.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.painel.ui.components.NexusLogo
import com.nexus.painel.ui.theme.NeonRed
import com.nexus.painel.ui.theme.NeonRedDark
import com.nexus.painel.ui.theme.NeonRedSoft
import com.nexus.painel.ui.theme.NexusGray
import com.nexus.painel.ui.theme.NexusSurfaceHigh
import com.nexus.painel.ui.theme.NexusWhite
import kotlin.random.Random

/** Tela de abertura: logo + "carregando o painel" por 5 a 7 segundos (aleatório). */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val durationMillis = remember { Random.nextInt(5000, 7001) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing)
        )
        onFinished()
    }

    val pulse = rememberInfiniteTransition(label = "logoPulse")
    val logoScale = pulse.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "logoScale"
    )

    val message = when {
        progress.value < 0.35f -> "Carregando o painel..."
        progress.value < 0.70f -> "Iniciando módulos..."
        progress.value < 0.95f -> "Preparando a interface..."
        else -> "Pronto!"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            NexusLogo(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
            )
        }

        Text(
            text = message,
            color = NexusWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        val barShape = RoundedCornerShape(50)
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .height(14.dp)
                .clip(barShape)
                .background(NexusSurfaceHigh)
                .border(1.dp, NeonRedDark, barShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.value.coerceIn(0.02f, 1f))
                    .shadow(8.dp, barShape, ambientColor = NeonRed, spotColor = NeonRed)
                    .clip(barShape)
                    .background(
                        Brush.horizontalGradient(listOf(NeonRedDark, NeonRed, NeonRedSoft))
                    )
            )
        }

        Text(
            text = "${(progress.value * 100).toInt()}%",
            color = NexusGray,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 10.dp, bottom = 40.dp)
        )
    }
}
