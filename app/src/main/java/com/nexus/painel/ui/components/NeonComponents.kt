package com.nexus.painel.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.painel.R
import com.nexus.painel.ui.theme.NeonRed
import com.nexus.painel.ui.theme.NeonRedDark
import com.nexus.painel.ui.theme.NeonRedSoft
import com.nexus.painel.ui.theme.NexusGray
import com.nexus.painel.ui.theme.NexusSurface
import com.nexus.painel.ui.theme.NexusWhite

/** Logo do aplicativo (imagem Painel Nexus com a seringa). */
@Composable
fun NexusLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.nexus_logo),
        contentDescription = "Painel Nexus",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

/** Botão vermelho neon com animação de clique (escala + brilho), sem som. */
@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 56.dp
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )
    val glow by animateDpAsState(
        targetValue = if (pressed) 4.dp else 18.dp,
        label = "buttonGlow"
    )
    val flash by animateFloatAsState(
        targetValue = if (pressed) 0.18f else 0f,
        label = "buttonFlash"
    )

    val shape = RoundedCornerShape(18.dp)
    val alpha = if (enabled) 1f else 0.45f

    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .shadow(glow, shape, ambientColor = NeonRed, spotColor = NeonRed)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(NeonRed, NeonRedDark)))
            .border(1.dp, NeonRedSoft, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled && !loading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .matchParentSize()
                .background(Color.White.copy(alpha = flash))
        )
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = NexusWhite,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                color = NexusWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
        }
    }
}

/** Caixa de texto com cantos bem arredondados, estilo vermelho neon. */
@Composable
fun NeonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    isError: Boolean = false,
    onDone: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        isError = isError,
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        textStyle = TextStyle(
            color = NexusWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        ),
        placeholder = {
            Text(text = placeholder, color = NexusGray, fontSize = 16.sp)
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = NexusWhite,
            unfocusedTextColor = NexusWhite,
            disabledTextColor = NexusGray,
            errorTextColor = NexusWhite,
            focusedContainerColor = Color(0xFF0D0D0D),
            unfocusedContainerColor = Color(0xFF0D0D0D),
            disabledContainerColor = Color(0xFF0D0D0D),
            errorContainerColor = Color(0xFF0D0D0D),
            cursorColor = NeonRed,
            errorCursorColor = NeonRed,
            focusedBorderColor = NeonRed,
            unfocusedBorderColor = NeonRedDark,
            disabledBorderColor = NeonRedDark.copy(alpha = 0.5f),
            errorBorderColor = NeonRedSoft,
            focusedPlaceholderColor = NexusGray,
            unfocusedPlaceholderColor = NexusGray
        )
    )
}

/** Cartão escuro com borda e brilho vermelho. */
@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                10.dp,
                shape,
                ambientColor = NeonRed.copy(alpha = 0.5f),
                spotColor = NeonRed.copy(alpha = 0.5f)
            )
            .clip(shape)
            .background(NexusSurface)
            .border(1.dp, NeonRedDark, shape)
            .padding(18.dp),
        content = content
    )
}

/** Selo arredondado (ex.: "EM BREVE", "PERMITIDO"). */
@Composable
fun StatusBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.8f), shape)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

/** Botão quadrado de pontas redondas com 4 barras (abre o menu lateral). */
@Composable
fun MenuButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "menuScale"
    )
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                if (pressed) 3.dp else 12.dp,
                shape,
                ambientColor = NeonRed,
                spotColor = NeonRed
            )
            .clip(shape)
            .background(Color(0xFF0D0D0D))
            .border(1.5.dp, NeonRed, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(24.dp)) {
            val barWidth = size.width
            val gap = size.height / 3f
            val stroke = 2.4.dp.toPx()
            for (i in 0..3) {
                val y = i * gap
                drawLine(
                    color = NeonRed,
                    start = Offset(0f, y),
                    end = Offset(barWidth, y),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/** Pequeno ponto indicador usado nas listas. */
@Composable
fun NeonDot(modifier: Modifier = Modifier, color: Color = NeonRed) {
    Box(
        modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(bottom = 4.dp)) {
        Text(
            text = title,
            color = NexusWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = NexusGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
