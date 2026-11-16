package com.nexus.painel.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.nexus.painel.MainActivity
import com.nexus.painel.R
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Overlay flutuante do Painel Nexus.
 *
 * 1) Começa só com a imagem (bolha arrastável).
 * 2) Ao tocar na imagem, abre o painel flutuante "PAINEL NEXUS V1" (sem funções, só teste).
 * 3) O botão "✕" do painel fecha o painel e volta a bolha.
 *
 * Usa Views nativas (não Compose) para evitar a necessidade de LifecycleOwner no WindowManager.
 */
class FloatingService : Service() {

    companion object {
        private const val CHANNEL_ID = "nexus_floating"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.nexus.painel.action.STOP_FLOATING"

        private val _running = MutableStateFlow(false)
        val running: StateFlow<Boolean> = _running.asStateFlow()

        fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, FloatingService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingService::class.java))
        }
    }

    private lateinit var windowManager: WindowManager

    private var bubbleView: View? = null
    private var panelView: View? = null
    private var bubbleAttached = false
    private var panelAttached = false

    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var panelParams: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        createNotificationChannel()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        )

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        buildBubble()
        buildPanel()
        showBubble()
        _running.value = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        hideBubble()
        hidePanelView()
        _running.value = false
        super.onDestroy()
    }

    // ---------------------------------------------------------------- notificação

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Painel Nexus flutuante",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Mantém o painel flutuante ativo" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            1,
            Intent(this, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            ),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stop = PendingIntent.getService(
            this,
            2,
            Intent(this, FloatingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Painel Nexus")
            .setContentText("Flutuante ativo")
            .setOngoing(true)
            .setContentIntent(openApp)
            .addAction(0, "Fechar", stop)
            .build()
    }

    // ---------------------------------------------------------------- bolha

    private fun buildBubble() {
        val size = dp(72)
        val image = ImageView(this).apply {
            setImageResource(R.drawable.nexus_logo_small)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Painel Nexus"
        }
        bubbleView = image

        bubbleParams = WindowManager.LayoutParams(
            size,
            size,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(16)
            y = dp(200)
        }

        attachDrag(image, bubbleParams) { openPanel() }
    }

    private fun showBubble() {
        val view = bubbleView ?: return
        if (!bubbleAttached) {
            windowManager.addView(view, bubbleParams)
            bubbleAttached = true
        }
    }

    private fun hideBubble() {
        val view = bubbleView ?: return
        if (bubbleAttached) {
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {
            }
            bubbleAttached = false
        }
    }

    // ---------------------------------------------------------------- painel

    private fun buildPanel() {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val panelWidth = minOf(dp(310), screenWidth - dp(32))

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = neonBackground(radius = 26, fill = 0xFF050505.toInt())
            setPadding(dp(18), dp(16), dp(18), dp(18))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "PAINEL NEXUS V1"
            setTextColor(0xFFFF1A1A.toInt())
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }
        header.addView(
            title,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )

        val close = TextView(this).apply {
            text = "✕"
            gravity = Gravity.CENTER
            setTextColor(0xFFFF1A1A.toInt())
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(12).toFloat()
                setColor(0xFF0D0D0D.toInt())
                setStroke(dp(1), 0xFFFF1A1A.toInt())
            }
            setOnClickListener { closePanel() }
        }
        header.addView(close, LinearLayout.LayoutParams(dp(36), dp(36)))
        root.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val divider = View(this).apply { setBackgroundColor(0x66FF1A1A) }
        root.addView(
            divider,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)).apply {
                topMargin = dp(12)
            }
        )

        // Corpo vazio (sem funções, apenas teste visual).
        val body = View(this)
        root.addView(
            body,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(180))
        )

        panelView = root

        panelParams = WindowManager.LayoutParams(
            panelWidth,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (screenWidth - panelWidth) / 2
            y = (screenHeight * 0.22f).toInt()
        }

        // O painel é arrastado pelo cabeçalho.
        attachDrag(header, panelParams, targetView = root) { }
    }

    private fun openPanel() {
        val view = panelView ?: return
        hideBubble()
        if (!panelAttached) {
            windowManager.addView(view, panelParams)
            panelAttached = true
        }
    }

    private fun closePanel() {
        hidePanelView()
        showBubble()
    }

    private fun hidePanelView() {
        val view = panelView ?: return
        if (panelAttached) {
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {
            }
            panelAttached = false
        }
    }

    // ---------------------------------------------------------------- utilitários

    private fun overlayType(): Int = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()

    /** Fundo preto com cantos redondos e borda vermelha neon (com halo externo). */
    private fun neonBackground(radius: Int, fill: Int): LayerDrawable {
        val glow = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radius + 3).toFloat()
            setColor(0x00000000)
            setStroke(dp(4), 0x33FF1A1A)
        }
        val main = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radius).toFloat()
            setColor(fill)
            setStroke(dp(2), 0xFFFF1A1A.toInt())
        }
        return LayerDrawable(arrayOf<Drawable>(glow, main)).apply {
            setLayerInset(1, dp(3), dp(3), dp(3), dp(3))
        }
    }

    /**
     * Torna [handle] um ponto de arraste para a janela [params].
     * Um toque sem movimento dispara [onTap].
     */
    private fun attachDrag(
        handle: View,
        params: WindowManager.LayoutParams,
        targetView: View = handle,
        onTap: () -> Unit
    ) {
        val slop = ViewConfiguration.get(this).scaledTouchSlop
        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        handle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    moved = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt()
                    val dy = (event.rawY - touchY).toInt()
                    if (!moved && (abs(dx) > slop || abs(dy) > slop)) moved = true
                    if (moved) {
                        params.x = startX + dx
                        params.y = startY + dy
                        try {
                            windowManager.updateViewLayout(targetView, params)
                        } catch (_: Exception) {
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!moved) onTap()
                    true
                }

                else -> false
            }
        }
    }
}
