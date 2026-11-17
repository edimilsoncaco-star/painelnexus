package com.nexus.painel

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.nexus.painel.ui.screens.AdminScreen
import com.nexus.painel.ui.screens.KeyScreen
import com.nexus.painel.ui.screens.MainScreen
import com.nexus.painel.ui.screens.SplashScreen
import com.nexus.painel.ui.theme.NexusBlack
import com.nexus.painel.ui.theme.NexusTheme
import com.nexus.painel.ui.theme.NeonRed

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            NexusTheme {
                NexusApp()
            }
        }
    }
}

private enum class Stage { Splash, Key, Main, Admin }

/** Fluxo: Splash (5–7 s) → Key → Tela principal (ou Painel ADM com a key de administrador). */
@Composable
private fun NexusApp() {
    var stage by remember { mutableStateOf(Stage.Splash) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBlack)
            .background(
                Brush.verticalGradient(
                    0f to NeonRed.copy(alpha = 0.16f),
                    0.45f to NexusBlack
                )
            )
    ) {
        AnimatedContent(
            targetState = stage,
            transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(350)) },
            label = "stage"
        ) { target ->
            when (target) {
                Stage.Splash -> SplashScreen(onFinished = { stage = Stage.Key })
                Stage.Key -> KeyScreen(
                    onAuthenticated = { isAdmin -> stage = if (isAdmin) Stage.Admin else Stage.Main }
                )
                Stage.Main -> MainScreen()
                Stage.Admin -> AdminScreen(onExit = { stage = Stage.Key })
            }
        }
    }
}
