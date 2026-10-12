package br.com.bonecoliveia

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class EscadaInfinitaActivity : Activity() {
    private lateinit var gameView: GameView
    private lateinit var scoreLabel: TextView
    private var score = 0
    private var paused = false
    private var gameOver = false
    private var playerLane = 0
    private var playerStep = 0
    private var hazards = mutableListOf<Pair<Int, Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 12, 27)
        window.navigationBarColor = Color.rgb(8, 12, 27)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(12), dp(16), dp(16))
            setBackgroundColor(Color.rgb(8, 12, 27))
        }
        val title = TextView(this).apply {
            text = "🪜 ESCADA INFINITA"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(4))
        }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))
        scoreLabel = TextView(this).apply {
            text = "Degraus: 0"
            textSize = 16f
            setTextColor(Color.rgb(167, 243, 208))
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(8))
        }
        root.addView(scoreLabel, LinearLayout.LayoutParams(-1, -2))
        gameView = GameView()
        root.addView(gameView, LinearLayout.LayoutParams(-1, 0, 1f))
        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER }
        row.addView(button("⬅️ Esquerda") { move(-1) }, weightedButton())
        row.addView(button("⬆️ Subir") { climb() }, weightedButton())
        row.addView(button("Direita ➡️") { move(1) }, weightedButton())
        controls.addView(row, LinearLayout.LayoutParams(-1, -2))
        val row2 = LinearLayout(this).apply { gravity = Gravity.CENTER }
        row2.addView(button(if (paused) "▶ Continuar" else "⏸ Pausar") {
            if (!gameOver) {
                paused = !paused
                Toast.makeText(this@EscadaInfinitaActivity, if (paused) "Jogo pausado" else "Valendo!", Toast.LENGTH_SHORT).show()
                recreateControls()
            }
        }, weightedButton())
        row2.addView(button("🔄 Reiniciar") { resetGame() }, weightedButton())
        row2.addView(button("Voltar") { finish() }, weightedButton())
        controls.addView(row2, LinearLayout.LayoutParams(-1, -2))
        root.addView(controls, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
    }

    private var controlsContainer: LinearLayout? = null
    private fun recreateControls() {
        // Atualiza o rótulo do botão de pausa sem reiniciar a partida.
        val root = (gameView.parent as? LinearLayout) ?: return
        val controls = root.getChildAt(root.childCount - 1) as? LinearLayout ?: return
        val row2 = controls.getChildAt(1) as? LinearLayout ?: return
        val pauseButton = row2.getChildAt(0) as? Button ?: return
        pauseButton.text = if (paused) "▶ Continuar" else "⏸ Pausar"
        gameView.invalidate()
    }

    private fun move(delta: Int) {
        if (paused || gameOver) return
        playerLane = (playerLane + delta).coerceIn(-1, 1)
        gameView.invalidate()
    }

    private fun climb() {
        if (paused || gameOver) {
            if (gameOver) Toast.makeText(this, "Toque em Reiniciar para jogar de novo", Toast.LENGTH_SHORT).show()
            return
        }
        playerStep++
        score++
        if (Random.nextInt(100) < 23) {
            val lane = Random.nextInt(-1, 2)
            hazards.add(lane to (playerStep + Random.nextInt(2, 5)))
        }
        hazards.removeAll { it.second < playerStep - 1 }
        if (hazards.any { it.first == playerLane && it.second == playerStep + 1 }) {
            gameOver = true
            Toast.makeText(this, "Fim de jogo! Você subiu $score degraus.", Toast.LENGTH_LONG).show()
        }
        scoreLabel.text = "Degraus: $score"
        gameView.invalidate()
    }

    private fun resetGame() {
        score = 0
        playerLane = 0
        playerStep = 0
        paused = false
        gameOver = false
        hazards.clear()
        scoreLabel.text = "Degraus: 0"
        recreateControls()
        gameView.invalidate()
    }

    private fun button(textValue: String, action: () -> Unit) = Button(this).apply {
        text = textValue
        isAllCaps = false
        textSize = 12f
        setTextColor(Color.WHITE)
        setBackgroundColor(Color.rgb(79, 70, 229))
        setOnClickListener { action() }
    }

    private fun weightedButton() = LinearLayout.LayoutParams(0, dp(52), 1f).apply {
        setMargins(dp(3), dp(3), dp(3), dp(3))
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private inner class GameView : View(this@EscadaInfinitaActivity) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            canvas.drawColor(Color.rgb(13, 20, 43))
            // Fundo em perspectiva
            paint.color = Color.rgb(25, 40, 74)
            val skyline = Path().apply {
                moveTo(0f, h * .48f)
                lineTo(w * .15f, h * .32f); lineTo(w * .28f, h * .47f)
                lineTo(w * .45f, h * .27f); lineTo(w * .62f, h * .46f)
                lineTo(w * .8f, h * .34f); lineTo(w, h * .45f)
                lineTo(w, h); lineTo(0f, h); close()
            }
            canvas.drawPath(skyline, paint)
            val cx = w / 2f
            val topY = h * .15f
            val stepGap = min(w * .11f, h * .09f)
            for (i in 0..8) {
                val y = topY + i * stepGap
                val widthAtY = w * (.22f + i * .045f)
                val xOffset = (i % 2) * w * .025f
                paint.color = if (i % 2 == 0) Color.rgb(99, 102, 241) else Color.rgb(129, 140, 248)
                canvas.drawRoundRect(cx - widthAtY / 2f + xOffset, y, cx + widthAtY / 2f + xOffset, y + dp(10), dp(5).toFloat(), dp(5).toFloat(), paint)
                paint.color = Color.rgb(49, 46, 129)
                paint.strokeWidth = dp(3).toFloat()
                canvas.drawLine(cx - widthAtY / 2f + xOffset, y + dp(12), cx - widthAtY / 2f + xOffset - dp(10), y + stepGap, paint)
                canvas.drawLine(cx + widthAtY / 2f + xOffset, y + dp(12), cx + widthAtY / 2f + xOffset + dp(10), y + stepGap, paint)
            }
            // Obstáculos visíveis à frente
            hazards.filter { it.second >= playerStep && it.second <= playerStep + 5 }.forEach { hazard ->
                val y = topY + (hazard.second - playerStep + 1) * stepGap
                val x = cx + hazard.first * w * .19f
                paint.color = Color.rgb(251, 113, 133)
                canvas.drawRoundRect(x - dp(12), y - dp(16), x + dp(12), y + dp(5), dp(5).toFloat(), dp(5).toFloat(), paint)
            }
            // Personagem original em estilo arcade
            val px = cx + playerLane * w * .19f
            val py = h * .72f
            paint.color = Color.rgb(34, 211, 238)
            canvas.drawRoundRect(px - dp(17), py - dp(24), px + dp(17), py + dp(17), dp(10).toFloat(), dp(10).toFloat(), paint)
            paint.color = Color.WHITE
            canvas.drawCircle(px - dp(6), py - dp(10), dp(4).toFloat(), paint)
            canvas.drawCircle(px + dp(6), py - dp(10), dp(4).toFloat(), paint)
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawCircle(px - dp(6), py - dp(10), dp(2).toFloat(), paint)
            canvas.drawCircle(px + dp(6), py - dp(10), dp(2).toFloat(), paint)
            paint.color = Color.rgb(250, 204, 21)
            canvas.drawRoundRect(px - dp(20), py + dp(15), px - dp(2), py + dp(23), dp(3).toFloat(), dp(3).toFloat(), paint)
            canvas.drawRoundRect(px + dp(2), py + dp(15), px + dp(20), py + dp(23), dp(3).toFloat(), dp(3).toFloat(), paint)

            if (paused || gameOver) {
                paint.color = 0xB3000000.toInt()
                canvas.drawRect(0f, 0f, w, h, paint)
                paint.color = Color.WHITE
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = dp(25).toFloat()
                canvas.drawText(if (paused) "PAUSADO" else "FIM DE JOGO", cx, h * .48f, paint)
                paint.textSize = dp(14).toFloat()
                canvas.drawText(if (paused) "Toque em Continuar" else "Toque em Reiniciar", cx, h * .55f, paint)
            } else {
                paint.color = Color.WHITE
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = dp(13).toFloat()
                canvas.drawText("Use SUBIR e desvie dos obstáculos", cx, h * .93f, paint)
            }
        }
    }
}
