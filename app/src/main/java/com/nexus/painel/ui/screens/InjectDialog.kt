package com.nexus.painel.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nexus.painel.admin.FileServiceClient
import com.nexus.painel.data.AdminStore
import com.nexus.painel.overlay.FloatingService
import com.nexus.painel.permissions.ShizukuHelper
import com.nexus.painel.ui.components.NeonButton
import com.nexus.painel.ui.components.NexusLogo
import com.nexus.painel.ui.theme.NeonRed
import com.nexus.painel.ui.theme.NeonRedDark
import com.nexus.painel.ui.theme.NexusGray
import com.nexus.painel.ui.theme.NexusGreen
import com.nexus.painel.ui.theme.NexusSurfaceHigh
import com.nexus.painel.ui.theme.NexusWhite
import kotlin.math.ceil
import kotlinx.coroutines.launch

private enum class InjectPhase { Working, Success, Launch, Error }

/**
 * Fluxo ao tocar em "Abrir flutuante":
 * 1) envia a pasta salva no Painel ADM para Android/data/com.dts.freefireth (com % de progresso);
 * 2) mostra "Painel injetado com sucesso" (OK ou fecha sozinho em 10 s);
 * 3) mostra "Iniciar jogo": abre o Free Fire e o flutuante junto.
 */
@Composable
fun InjectDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val display = remember { Animatable(0f) }
    val countdown = remember { Animatable(1f) }

    var phase by remember { mutableStateOf(InjectPhase.Working) }
    var status by remember { mutableStateOf("Preparando...") }
    var error by remember { mutableStateOf("") }
    var attempt by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        onDispose { FileServiceClient.unbind() }
    }

    LaunchedEffect(attempt) {
        phase = InjectPhase.Working
        status = "Preparando..."
        display.snapTo(0f)

        val folder = AdminStore.getFolder(context)
        if (folder == null) {
            error = "Nenhuma pasta configurada.\nEntre no Painel ADM e adicione a pasta."
            phase = InjectPhase.Error
            return@LaunchedEffect
        }

        val shizuku = ShizukuHelper.status(context)
        if (shizuku != ShizukuHelper.Status.GRANTED) {
            error = when (shizuku) {
                ShizukuHelper.Status.NOT_INSTALLED -> "O Shizuku não está instalado."
                ShizukuHelper.Status.NOT_RUNNING -> "O Shizuku está parado. Inicie-o e tente de novo."
                ShizukuHelper.Status.DENIED -> "Acesso negado. Libere o Painel Nexus dentro do app Shizuku."
                else -> "Permita o Shizuku na aba Permissões e tente de novo."
            }
            phase = InjectPhase.Error
            return@LaunchedEffect
        }

        FileServiceClient.bind(context)
        if (!FileServiceClient.awaitConnected(8000)) {
            error = "Não consegui conectar ao serviço do Shizuku. Tente de novo."
            phase = InjectPhase.Error
            return@LaunchedEffect
        }

        status = "Injetando painel..."
        var finished = false
        val result = FileServiceClient.transfer(folder.path, FileServiceClient.GAME_DATA_DIR) { percent ->
            val target = percent / 100f
            if (!finished && target > display.targetValue) {
                scope.launch { display.animateTo(target, tween(400)) }
            }
        }
        finished = true

        if (result.startsWith("OK")) {
            status = "Finalizando..."
            display.animateTo(1f, tween(1200, easing = LinearEasing))
            phase = InjectPhase.Success
        } else {
            error = result
            phase = InjectPhase.Error
        }
    }

    // Contagem de 10 s no topo; ao acabar (ou ao tocar em OK) vai para "Iniciar jogo".
    LaunchedEffect(phase) {
        if (phase == InjectPhase.Success) {
            countdown.snapTo(1f)
            countdown.animateTo(0f, tween(10_000, easing = LinearEasing))
            phase = InjectPhase.Launch
        }
    }

    fun startGame() {
        val intent = context.packageManager.getLaunchIntentForPackage(FileServiceClient.GAME_PACKAGE)
        if (intent == null) {
            Toast.makeText(context, "Free Fire não encontrado no aparelho.", Toast.LENGTH_LONG).show()
            return
        }
        FloatingService.start(context)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        onDismiss()
    }

    val canClose = phase != InjectPhase.Working

    Dialog(
        onDismissRequest = { if (canClose) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = canClose,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        val shape = RoundedCornerShape(30.dp)
        Column(
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .fillMaxWidth()
                .shadow(18.dp, shape, ambientColor = NeonRed, spotColor = NeonRed)
                .clip(shape)
                .background(Color(0xFF050505))
                .border(1.5.dp, NeonRed, shape)
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Topo: contagem regressiva (só aparece no sucesso).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                if (phase == InjectPhase.Success) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Fechando em ${ceil(countdown.value * 10f).toInt()}s",
                            color = NexusGray,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        val barShape = RoundedCornerShape(50)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(barShape)
                                .background(NexusSurfaceHigh)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(countdown.value.coerceIn(0f, 1f))
                                    .clip(barShape)
                                    .background(NeonRed)
                            )
                        }
                    }
                }
            }

            NexusLogo(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )

            Spacer(Modifier.height(18.dp))

            Crossfade(targetState = phase, label = "injectPhase") { current ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (current) {
                        InjectPhase.Working -> {
                            ProgressRing(
                                progress = display.value,
                                modifier = Modifier.size(150.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = status,
                                color = NexusWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(6.dp))
                        }

                        InjectPhase.Success -> {
                            SuccessCheck()
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Painel injetado com sucesso",
                                color = NexusWhite,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(20.dp))
                            NeonButton(
                                text = "OK",
                                onClick = { phase = InjectPhase.Launch },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        InjectPhase.Launch -> {
                            Text(
                                text = "Tudo pronto!",
                                color = NexusGreen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Toque para abrir o Free Fire. O flutuante abre junto.",
                                color = NexusGray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(20.dp))
                            NeonButton(
                                text = "INICIAR JOGO",
                                onClick = { startGame() },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "Fechar",
                                color = NexusGray,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onDismiss() }
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        InjectPhase.Error -> {
                            Text(
                                text = "ATENÇÃO",
                                color = NeonRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = error,
                                color = NexusWhite,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(20.dp))
                            NeonButton(
                                text = "TENTAR DE NOVO",
                                onClick = { attempt++ },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "Fechar",
                                color = NexusGray,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onDismiss() }
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Anel de progresso com a porcentagem no centro. */
@Composable
private fun ProgressRing(progress: Float, modifier: Modifier = Modifier) {
    val value = progress.coerceIn(0f, 1f)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            val inset = stroke / 2f
            val topLeft = Offset(inset, inset)
            val arcSize = Size(size.width - stroke, size.height - stroke)

            drawArc(
                color = NeonRedDark.copy(alpha = 0.35f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke)
            )
            // brilho
            drawArc(
                color = NeonRed.copy(alpha = 0.25f),
                startAngle = -90f,
                sweepAngle = 360f * value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke + 10.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = NeonRed,
                startAngle = -90f,
                sweepAngle = 360f * value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${(value * 100).toInt()}%",
            color = NexusWhite,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black
        )
    }
}

/** Símbolo de concluído: círculo que se desenha e o "check" que aparece. */
@Composable
private fun SuccessCheck(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }
    Canvas(modifier.size(92.dp)) {
        val stroke = 6.dp.toPx()
        val inset = stroke / 2f
        drawArc(
            color = NexusGreen.copy(alpha = 0.25f),
            startAngle = -90f,
            sweepAngle = 360f * progress.value,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(size.width - stroke, size.height - stroke),
            style = Stroke(width = stroke + 8.dp.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
            color = NexusGreen,
            startAngle = -90f,
            sweepAngle = 360f * progress.value,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(size.width - stroke, size.height - stroke),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        val check = Path().apply {
            moveTo(size.width * 0.28f, size.height * 0.52f)
            lineTo(size.width * 0.44f, size.height * 0.68f)
            lineTo(size.width * 0.74f, size.height * 0.36f)
        }
        drawPath(
            path = check,
            color = NexusGreen.copy(alpha = progress.value),
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
