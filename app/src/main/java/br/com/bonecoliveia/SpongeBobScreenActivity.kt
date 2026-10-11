package br.com.bonecoliveia

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class SpongeBobScreenActivity : Activity(), TextToSpeech.OnInitListener {
    private var player: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private lateinit var musicStatus: TextView
    private lateinit var reactionText: TextView
    private val pickAudio = 4201

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(0, 76, 120)
        window.navigationBarColor = Color.rgb(0, 38, 70)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(8), dp(12), dp(10))
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.rgb(0, 190, 218), Color.rgb(0, 105, 160), Color.rgb(0, 44, 91))
            )
        }
        tts = TextToSpeech(this, this)
        val title = TextView(this).apply {
            text = "BEM-VINDO À FENDA DO BIQUÍNI!"
            textSize = 20f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setShadowLayer(5f, 2f, 3f, Color.rgb(0, 35, 70))
            setPadding(0, dp(4), 0, dp(4))
        }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))
        root.addView(UnderwaterSpongeView(), LinearLayout.LayoutParams(-1, 0, 1f))
        reactionText = TextView(this).apply {
            text = "Oi, pessoal! Eu sou o Bob Esponja! Vamos nos divertir!"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(dp(8), dp(6), dp(8), dp(6))
            setShadowLayer(4f, 1f, 2f, Color.rgb(0, 35, 70))
        }
        root.addView(reactionText, LinearLayout.LayoutParams(-1, -2))
        val testVoice = Button(this).apply {
            text = "🔊  TESTAR VOZ DO PERSONAGEM"
            isAllCaps = false
            setOnClickListener { speakCharacter("Oi, pessoal! Eu sou uma esponja muito divertida! Manda um comentário aí!") }
        }
        root.addView(testVoice, LinearLayout.LayoutParams(-1, -2))
        musicStatus = TextView(this).apply {
            text = "Escolha uma música de áudio salva no celular."
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        root.addView(musicStatus, LinearLayout.LayoutParams(-1, -2))
        val music = Button(this).apply {
            text = "♫  ESCOLHER MÚSICA DO CELULAR"
            isAllCaps = false
            setOnClickListener { chooseMusic() }
        }
        root.addView(music, LinearLayout.LayoutParams(-1, -2))
        val back = Button(this).apply {
            text = "VOLTAR AO BONECO LIVE IA"
            isAllCaps = false
            setOnClickListener { finish() }
        }
        root.addView(back, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        SpongeBobEvents.screenOpen = true
        SpongeBobEvents.listener = { mood, line ->
            runOnUiThread {
                reactionText.text = "$mood\n$line"
                speakCharacter(line)
            }
        }
    }

    override fun onPause() {
        SpongeBobEvents.listener = null
        SpongeBobEvents.screenOpen = false
        tts?.stop()
        super.onPause()
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            musicStatus.text = "A voz do Android não iniciou. Confira as opções de Texto para voz."
            return
        }
        val engine = tts ?: return
        val languageResult = engine.setLanguage(Locale("pt", "BR"))
        ttsReady = languageResult != TextToSpeech.LANG_MISSING_DATA && languageResult != TextToSpeech.LANG_NOT_SUPPORTED
        if (ttsReady) {
            engine.setPitch(1.35f)
            engine.setSpeechRate(1.08f)
            musicStatus.text = "Voz cartoon em português pronta. A voz exata do desenho não está incluída."
        } else {
            musicStatus.text = "Português não disponível no mecanismo de voz deste celular."
        }
    }

    private fun speakCharacter(line: String) {
        if (ttsReady) {
            tts?.setPitch(1.35f)
            tts?.setSpeechRate(1.08f)
            tts?.speak(line, TextToSpeech.QUEUE_FLUSH, null, "spongebob-reaction")
        }
    }

    private fun chooseMusic() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }
        try {
            startActivityForResult(intent, pickAudio)
        } catch (_: Exception) {
            musicStatus.text = "Não consegui abrir os arquivos de áudio deste aparelho."
        }
    }

    @Deprecated("Uses the Android document picker for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != pickAudio || resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return
        try {
            player?.release()
            player = MediaPlayer().apply {
                setDataSource(this@SpongeBobScreenActivity, uri)
                setOnPreparedListener {
                    it.isLooping = true
                    it.start()
                    musicStatus.text = "♫ Música tocando em repetição. Use Voltar para sair."
                }
                setOnErrorListener { _, _, _ ->
                    musicStatus.text = "Esse arquivo não pôde tocar. Escolha outro áudio."
                    true
                }
                prepareAsync()
            }
            musicStatus.text = "Carregando música..."
        } catch (_: Exception) {
            musicStatus.text = "Não consegui tocar esse arquivo. Tente outro áudio."
        }
    }

    override fun onDestroy() {
        try { player?.stop() } catch (_: Exception) {}
        player?.release()
        player = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private inner class UnderwaterSpongeView : View(this@SpongeBobScreenActivity) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        private fun color(c: Int) { p.color = c; p.style = Paint.Style.FILL; p.strokeWidth = 1f }
        private fun oval(canvas: Canvas, r: RectF, c: Int) { color(c); canvas.drawOval(r, p) }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat(); val h = height.toFloat()
            // Fundo submarino: raios de luz, bolhas, areia e corais
            color(Color.argb(35, 255, 255, 255))
            for (i in 0..5) {
                val x = w * (i + 0.5f) / 6f
                val beam = Path().apply {
                    moveTo(x - w * .035f, 0f); lineTo(x + w * .035f, 0f)
                    lineTo(x + w * .16f, h * .48f); lineTo(x - w * .12f, h * .48f); close()
                }
                canvas.drawPath(beam, p)
            }
            color(Color.rgb(243, 206, 117)); canvas.drawRect(0f, h*.91f, w, h, p)
            color(Color.rgb(218, 166, 85))
            for (i in 0..18) canvas.drawCircle((i*73f)%w, h*.94f + (i%3)*h*.015f, 2.5f, p)
            // Plantas/corais nas laterais
            color(Color.rgb(242, 89, 112)); p.strokeWidth = w*.018f; p.strokeCap = Paint.Cap.ROUND
            canvas.drawLine(w*.06f,h*.92f,w*.08f,h*.67f,p)
            canvas.drawLine(w*.08f,h*.78f,w*.15f,h*.70f,p)
            canvas.drawLine(w*.08f,h*.82f,w*.035f,h*.75f,p)
            color(Color.rgb(255, 141, 76)); p.strokeWidth = w*.022f
            canvas.drawLine(w*.92f,h*.92f,w*.9f,h*.69f,p)
            canvas.drawLine(w*.9f,h*.79f,w*.84f,h*.73f,p)
            canvas.drawLine(w*.9f,h*.83f,w*.96f,h*.75f,p)
            color(Color.argb(160,255,255,255))
            val bubbles = listOf(floatArrayOf(.12f,.22f,.018f),floatArrayOf(.83f,.18f,.025f),floatArrayOf(.72f,.34f,.012f),floatArrayOf(.22f,.43f,.013f),floatArrayOf(.9f,.48f,.016f))
            bubbles.forEach { canvas.drawCircle(w*it[0],h*it[1],w*it[2],p) }

            // Personagem grande, centralizado, com volumes, sombras e detalhes.
            val unit = minOf(w*.78f, h*.72f)
            val cx = w/2f
            val top = h*.07f
            val left = cx-unit*.43f; val right=cx+unit*.43f
            val bodyBottom=top+unit*.72f
            // Sombra
            color(Color.argb(75,0,30,55)); canvas.drawOval(cx-unit*.48f,h*.86f,cx+unit*.48f,h*.92f,p)
            // Pernas e sapatos
            color(Color.rgb(250,204,70)); p.strokeWidth=unit*.045f
            canvas.drawLine(cx-unit*.20f,bodyBottom+unit*.19f,cx-unit*.20f,h*.86f,p)
            canvas.drawLine(cx+unit*.20f,bodyBottom+unit*.19f,cx+unit*.20f,h*.86f,p)
            color(Color.rgb(35,30,35)); canvas.drawRoundRect(cx-unit*.38f,h*.83f,cx-unit*.06f,h*.90f,unit*.035f,unit*.035f,p)
            canvas.drawRoundRect(cx+unit*.06f,h*.83f,cx+unit*.38f,h*.90f,unit*.035f,unit*.035f,p)
            // Braços
            color(Color.rgb(255,218,48)); p.strokeWidth=unit*.055f
            canvas.drawLine(left+unit*.02f,top+unit*.43f,left-unit*.15f,top+unit*.60f,p)
            canvas.drawLine(right-unit*.02f,top+unit*.43f,right+unit*.15f,top+unit*.60f,p)
            // Corpo esponjoso com contorno e gradiente simples
            p.shader=android.graphics.LinearGradient(left,top,right,bodyBottom,Color.rgb(255,244,88),Color.rgb(237,174,19),android.graphics.Shader.TileMode.CLAMP)
            p.style=Paint.Style.FILL
            canvas.drawRoundRect(left,top,right,bodyBottom,unit*.10f,unit*.10f,p)
            p.shader=null
            p.style=Paint.Style.STROKE; p.strokeWidth=unit*.012f; p.color=Color.rgb(184,132,15)
            canvas.drawRoundRect(left,top,right,bodyBottom,unit*.10f,unit*.10f,p); p.style=Paint.Style.FILL
            // Poros, cada um com sombra suave
            val pores=listOf(floatArrayOf(.12f,.17f),floatArrayOf(.83f,.14f),floatArrayOf(.08f,.43f),floatArrayOf(.91f,.38f),floatArrayOf(.20f,.62f),floatArrayOf(.76f,.59f),floatArrayOf(.35f,.11f),floatArrayOf(.64f,.68f))
            pores.forEach {
                color(Color.rgb(221,160,22)); canvas.drawOval(left+unit*it[0]-unit*.027f,top+unit*it[1]-unit*.018f,left+unit*it[0]+unit*.027f,top+unit*it[1]+unit*.018f,p)
            }
            // Olhos brancos, íris azuis e reflexos
            oval(canvas,RectF(cx-unit*.31f,top+unit*.13f,cx+unit*.015f,top+unit*.47f),Color.WHITE)
            oval(canvas,RectF(cx-unit*.015f,top+unit*.13f,cx+unit*.31f,top+unit*.47f),Color.WHITE)
            oval(canvas,RectF(cx-unit*.205f,top+unit*.22f,cx-unit*.075f,top+unit*.37f),Color.rgb(34,145,230))
            oval(canvas,RectF(cx+unit*.075f,top+unit*.22f,cx+unit*.205f,top+unit*.37f),Color.rgb(34,145,230))
            oval(canvas,RectF(cx-unit*.17f,top+unit*.245f,cx-unit*.10f,top+unit*.33f),Color.rgb(10,25,42))
            oval(canvas,RectF(cx+unit*.10f,top+unit*.245f,cx+unit*.17f,top+unit*.33f),Color.rgb(10,25,42))
            oval(canvas,RectF(cx-unit*.16f,top+unit*.25f,cx-unit*.125f,top+unit*.29f),Color.WHITE)
            oval(canvas,RectF(cx+unit*.125f,top+unit*.25f,cx+unit*.16f,top+unit*.29f),Color.WHITE)
            // Nariz, bochechas e sorriso
            color(Color.rgb(255,207,45)); canvas.drawOval(cx-unit*.055f,top+unit*.36f,cx+unit*.055f,top+unit*.52f,p)
            oval(canvas,RectF(cx-unit*.35f,top+unit*.43f,cx-unit*.17f,top+unit*.55f),Color.rgb(246,136,125))
            oval(canvas,RectF(cx+unit*.17f,top+unit*.43f,cx+unit*.35f,top+unit*.55f),Color.rgb(246,136,125))
            p.color=Color.rgb(112,48,25); p.style=Paint.Style.STROKE; p.strokeWidth=unit*.018f
            canvas.drawArc(cx-unit*.24f,top+unit*.40f,cx+unit*.24f,top+unit*.67f,10f,160f,false,p)
            p.style=Paint.Style.FILL
            color(Color.WHITE); canvas.drawRoundRect(cx-unit*.13f,top+unit*.54f,cx+unit*.01f,top+unit*.61f,unit*.012f,unit*.012f,p)
            canvas.drawRoundRect(cx+unit*.01f,top+unit*.54f,cx+unit*.13f,top+unit*.61f,unit*.012f,unit*.012f,p)
            // Camisa, gola, gravata e calça
            color(Color.WHITE); canvas.drawRect(left+unit*.025f,bodyBottom-unit*.03f,right-unit*.025f,bodyBottom+unit*.12f,p)
            color(Color.rgb(238,54,57))
            val tie=Path().apply { moveTo(cx-unit*.06f,bodyBottom); lineTo(cx+unit*.06f,bodyBottom); lineTo(cx+unit*.035f,bodyBottom+unit*.14f); lineTo(cx,bodyBottom+unit*.19f); lineTo(cx-unit*.035f,bodyBottom+unit*.14f); close() }
            canvas.drawPath(tie,p)
            color(Color.rgb(133,72,39)); canvas.drawRect(left+unit*.025f,bodyBottom+unit*.12f,right-unit*.025f,bodyBottom+unit*.22f,p)
            color(Color.WHITE); canvas.drawRect(left+unit*.10f,bodyBottom+unit*.02f,left+unit*.22f,bodyBottom+unit*.05f,p)
            canvas.drawRect(right-unit*.22f,bodyBottom+unit*.02f,right-unit*.10f,bodyBottom+unit*.05f,p)
        }
    }
}
