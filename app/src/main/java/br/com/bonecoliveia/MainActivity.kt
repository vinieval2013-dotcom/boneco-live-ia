package br.com.bonecoliveia

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import io.github.jwdeveloper.tiktok.client.TikTokLive
import io.github.jwdeveloper.tiktok.api.live.LiveClient
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingSpeech: String? = null
    private var liveClient: LiveClient? = null
    private val connecting = AtomicBoolean(false)
    private var connectionAttempt = 0
    @Volatile private var connectionConfirmed = false
    private lateinit var answerText: TextView
    private lateinit var voiceStatus: TextView
    private lateinit var connectionStatus: TextView
    private lateinit var characterFace: TextView
    private lateinit var characterMood: TextView
    private lateinit var connectButton: Button
    private lateinit var disconnectButton: Button
    private lateinit var statsText: TextView
    private lateinit var voiceToggle: CheckBox
    private lateinit var modeToggle: CheckBox
    private var voiceEnabled = true
    private var gamerMode = false
    private var commentsRead = 0
    private var giftsReceived = 0
    private var lastSpokenAt = 0L
    private val seenViewers = linkedSetOf<String>()

    private val bg = Color.rgb(10, 14, 28)
    private val panel = Color.rgb(23, 30, 51)
    private val accent = Color.rgb(99, 102, 241)
    private val muted = Color.rgb(178, 188, 210)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 24, 20, 28)
            setBackgroundColor(bg)
        }

        root.addView(label("BONECO LIVE IA", 26f, Color.WHITE, true))
        root.addView(label("REAÇÕES AO VIVO • VERSÃO 0.6.0", 12f, muted, true).apply {
            setPadding(0, 4, 0, 18)
        })

        val hero = panelBox()
        hero.addView(SpongeBobView(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(190)
        ))
        characterFace = label("😎", 30f, Color.WHITE, true).apply { gravity = Gravity.CENTER }
        hero.addView(characterFace, matchWidthWrap())
        characterMood = label("E aí, meu chapa! Bora pra LIVE!", 17f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 8)
        }
        hero.addView(characterMood, matchWidthWrap())
        hero.addView(label("Personagem brincalhão com expressões, respostas e voz.", 13f, muted, false).apply {
            gravity = Gravity.CENTER
        }, matchWidthWrap())
        root.addView(hero, withBottom(matchWidthWrap(), 16))

        val characterScreenButton = actionButton("🧽  ABRIR TELA GRANDE DO BOB ESPONJA")
        characterScreenButton.setOnClickListener {
            startActivity(Intent(this, SpongeBobScreenActivity::class.java))
        }
        root.addView(characterScreenButton, withBottom(matchWidthWrap(), 16))

        val connectPanel = panelBox()
        connectPanel.addView(label("01  •  CONECTAR AO CHAT REAL", 16f, Color.WHITE, true))
        connectPanel.addView(label("Conta configurada exclusivamente: @vini66158", 13f, muted, false).apply {
            setPadding(0, 5, 0, 10)
        })

        connectButton = actionButton("🔴  CONECTAR AO @VINI66158")
        connectButton.setOnClickListener { connectToLive() }
        connectPanel.addView(connectButton, withBottom(matchWidthWrap(), 8))

        disconnectButton = actionButton("DESCONECTAR")
        disconnectButton.setOnClickListener { disconnectFromLive() }
        disconnectButton.isEnabled = false
        connectPanel.addView(disconnectButton, matchWidthWrap())

        connectionStatus = label("Desconectado. Toque em conectar para tentar abrir o chat.", 13f, Color.rgb(250, 204, 120), false).apply {
            setPadding(0, 10, 0, 0)
        }
        connectPanel.addView(connectionStatus, matchWidthWrap())
        root.addView(connectPanel, withBottom(matchWidthWrap(), 16))

        val interactionPanel = panelBox()
        interactionPanel.addView(label("02  •  ÚLTIMA REAÇÃO", 16f, Color.WHITE, true))
        answerText = label("Quando chegar um comentário real, a resposta aparecerá aqui.", 16f, Color.WHITE, false).apply {
            setPadding(0, 10, 0, 4)
        }
        interactionPanel.addView(answerText, matchWidthWrap())
        root.addView(interactionPanel, withBottom(matchWidthWrap(), 16))

        val voicePanel = panelBox()
        voicePanel.addView(label("03  •  VOZ DO PERSONAGEM", 16f, Color.WHITE, true))
        voiceStatus = label("Preparando a voz em português...", 13f, Color.rgb(134, 239, 172), false).apply {
            setPadding(0, 8, 0, 10)
        }
        voicePanel.addView(voiceStatus, matchWidthWrap())
        val retryButton = actionButton("TENTAR INICIAR A VOZ NOVAMENTE")
        retryButton.setOnClickListener {
            voiceStatus.text = "Reiniciando a voz..."
            ttsReady = false
            tts?.stop()
            tts?.shutdown()
            tts = TextToSpeech(this@MainActivity, this@MainActivity)
        }
        voicePanel.addView(retryButton, matchWidthWrap())
        root.addView(voicePanel, withBottom(matchWidthWrap(), 16))

        val settingsPanel = panelBox()
        settingsPanel.addView(label("04  •  PERSONALIZAR A LIVE", 16f, Color.WHITE, true))
        voiceToggle = CheckBox(this).apply {
            text = "Falar as respostas em voz alta"
            isChecked = true
            setTextColor(Color.WHITE)
            buttonTintList = android.content.res.ColorStateList.valueOf(accent)
            setOnCheckedChangeListener { _, checked ->
                voiceEnabled = checked
                if (!checked) tts?.stop()
                voiceStatus.text = if (checked) "Voz ativada." else "Voz desativada; respostas ficam na tela."
            }
        }
        settingsPanel.addView(voiceToggle, matchWidthWrap())
        modeToggle = CheckBox(this).apply {
            text = "Modo gamer (Subway Surfers)"
            isChecked = false
            setTextColor(Color.WHITE)
            buttonTintList = android.content.res.ColorStateList.valueOf(accent)
            setOnCheckedChangeListener { _, checked ->
                gamerMode = checked
                characterMood.text = if (checked) "Modo gamer ativado! Bora buscar recorde!" else "Modo engraçado ativado! Bora brincar!"
            }
        }
        settingsPanel.addView(modeToggle, matchWidthWrap())
        statsText = label("Comentários lidos: 0  •  Presentes: 0  •  Pessoas vistas: 0", 13f, muted, false).apply {
            setPadding(0, 10, 0, 0)
        }
        settingsPanel.addView(statsText, matchWidthWrap())
        root.addView(settingsPanel, withBottom(matchWidthWrap(), 16))

        root.addView(label("AVISO DE CONEXÃO", 12f, Color.rgb(250, 204, 120), true))
        root.addView(label("A conexão usa uma biblioteca comunitária não oficial. O TikTok pode mudar o sistema e interromper a conexão. O app só reage quando receber eventos; ele não envia mensagens no chat nem controla presentes.", 12f, muted, false).apply {
            setPadding(0, 6, 0, 0)
        }, matchWidthWrap())

        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(root)
            setBackgroundColor(bg)
        })
        // Inicializa a voz só depois de criar os componentes usados pelo callback onInit.
        tts = TextToSpeech(this, this)
    }

    private fun connectToLive() {
        val username = "vini66158"
        if (!connecting.compareAndSet(false, true)) {
            connectionStatus.text = "Já estou tentando conectar..."
            return
        }
        connectionAttempt++
        val thisAttempt = connectionAttempt
        connectionConfirmed = false
        connectButton.isEnabled = false
        disconnectButton.isEnabled = false
        connectionStatus.text = "Tentando conectar ao chat público de @${username}..."
        characterFace.text = "🤔"
        characterMood.text = "Um instante! Estou tentando abrir o chat..."
        Handler(Looper.getMainLooper()).postDelayed({
            if (thisAttempt == connectionAttempt && !connectionConfirmed && connecting.get()) {
                try { liveClient?.disconnect() } catch (_: Exception) {}
                liveClient = null
                connecting.set(false)
                connectButton.isEnabled = true
                disconnectButton.isEnabled = false
                connectionStatus.text = "Não houve confirmação em 20 s. Confirme que @vini66158 está AO VIVO e que a LIVE é pública. A conexão usa integração não oficial."
                connectionStatus.setTextColor(Color.rgb(252, 165, 165))
                characterFace.text = "😵"
                characterMood.text = "Não consegui confirmar a conexão com o chat."
            }
        }, 20_000L)

        // A biblioteca de terceiros pode bloquear ou lançar erros durante a inicialização.
        // Mantemos essa etapa fora da thread da interface e mostramos falhas em vez de fechar a tela.
        Thread({
            try {
                TikTokLive.newClient(username)
                    .configure { settings ->
                        settings.setClientLanguage("pt")
                        settings.setRetryOnConnectionFailure(true)
                        settings.setPrintToConsole(false)
                    }
                    .onConnected { _, _ ->
                        runOnUiThread {
                            connectionConfirmed = true
                            connectionStatus.text = "CONECTADO ao chat de @${username}. Esperando comentários..."
                            connectionStatus.setTextColor(Color.rgb(134, 239, 172))
                            characterFace.text = "😎"
                            characterMood.text = "Aêêê! O chat tá aberto, manda ver!"
                            connectButton.isEnabled = false
                            disconnectButton.isEnabled = true
                        }
                    }
                    .onComment { _, event ->
                        val nickname = event.user.name ?: "alguém"
                        val comment = event.text ?: ""
                        if (comment.isNotBlank()) runOnUiThread { reactToComment(nickname, comment) }
                    }
                    .onGift { _, event ->
                        val nickname = event.user.name ?: "amigo"
                        val giftName = event.gift.name ?: "presente"
                        runOnUiThread { reactToGift(nickname, giftName) }
                    }
                    .onDisconnected { _, _ ->
                        runOnUiThread {
                            connectionStatus.text = "A conexão foi encerrada. Confira se @${username} está ao vivo e tente novamente."
                            connectionStatus.setTextColor(Color.rgb(250, 204, 120))
                            characterFace.text = "😕"
                            characterMood.text = "Opa, perdi a conexão com o chat."
                            connectButton.isEnabled = true
                            disconnectButton.isEnabled = false
                            connecting.set(false)
                            connectionConfirmed = false
                            liveClient = null
                        }
                    }
                    .onError { _, event ->
                        val message = event.exception?.message ?: "A biblioteca não conseguiu conectar."
                        runOnUiThread {
                            connectionStatus.text = "Falha ao conectar: " + message.take(120)
                            connectionStatus.setTextColor(Color.rgb(252, 165, 165))
                            characterFace.text = "😵"
                            characterMood.text = "Não consegui entrar no chat. Tente novamente."
                            connectButton.isEnabled = true
                            disconnectButton.isEnabled = false
                            connecting.set(false)
                        }
                    }
                    .buildAndConnectAsync()
                    .thenAccept { client ->
                        liveClient = client
                        runOnUiThread {
                            connectionStatus.text = "Pedido de conexão enviado para @${username}; aguardando confirmação..."
                            disconnectButton.isEnabled = true
                        }
                    }
                    .exceptionally { error ->
                        runOnUiThread {
                            connectionStatus.text = "Não conectou: " + (error.cause?.message ?: error.message ?: "erro desconhecido").take(120)
                            connectionStatus.setTextColor(Color.rgb(252, 165, 165))
                            connectButton.isEnabled = true
                            disconnectButton.isEnabled = false
                            connecting.set(false)
                            connectionConfirmed = false
                        }
                        null
                    }
            } catch (error: Throwable) {
                runOnUiThread {
                    connectionStatus.text = "Erro ao iniciar a conexão: " + (error.message ?: error.javaClass.simpleName).take(120)
                    connectionStatus.setTextColor(Color.rgb(252, 165, 165))
                    characterFace.text = "😵"
                    characterMood.text = "A conexão falhou, mas o app continua aberto."
                    connectButton.isEnabled = true
                    disconnectButton.isEnabled = false
                    connecting.set(false)
                }
            }
        }, "boneco-tiktok-connect").start()
    }

    private fun reactToComment(nickname: String, comment: String) {
        commentsRead++
        seenViewers.add(nickname.lowercase(Locale.ROOT))
        statsText.text = "Comentários lidos: $commentsRead  •  Presentes: $giftsReceived  •  Pessoas vistas: ${seenViewers.size}"
        val result = makeResponse(nickname, comment)
        characterFace.text = result.first
        characterMood.text = result.second
        answerText.text = "$nickname comentou: $comment\n\nBoneco: ${result.third}"
        val now = System.currentTimeMillis()
        if (now - lastSpokenAt >= 1400L) {
            speak(result.third)
            lastSpokenAt = now
        }
    }

    private fun reactToGift(nickname: String, gift: String) {
        giftsReceived++
        seenViewers.add(nickname.lowercase(Locale.ROOT))
        statsText.text = "Comentários lidos: $commentsRead  •  Presentes: $giftsReceived  •  Pessoas vistas: ${seenViewers.size}"
        characterFace.text = "🧽🥹"
        characterMood.text = "UM PRESENTE?! TÔ TODO EMOCIONADO!"
        val response = "Aí sim, $nickname! Valeu demais pelo presente $gift! Você é brabo!"
        answerText.text = "🎁 $nickname enviou $gift\n\nBoneco: $response"
        speak(response)
    }

    private fun makeResponse(nickname: String, comment: String): Triple<String, String, String> {
        val text = comment.lowercase(Locale.ROOT)
        if (gamerMode && listOf("subway", "jogo", "recorde", "pontos", "game", "perdeu", "errou").any { text.contains(it) }) {
            return Triple("🧽🤩", "MODO GAMER ATIVADO!", "Bora buscar esse recorde, $nickname! Se eu bater no trem, foi culpa do lag!")
        }
        return when {
            listOf("kkkk", "haha", "😂", "engraçado", "piada").any { text.contains(it) } ->
                Triple("🧽😂", "TÔ RINDO ATÉ AS ESPONJAS!", "KKKKKK! Para que eu vou cair da cadeira! Manda outra!")
            listOf("oi", "olá", "ola", "salve", "cheguei", "bom dia", "boa noite").any { text.contains(it) } ->
                Triple("🧽😎", "CHEGOU MAIS UM PARÇA!", "Opa, salve, $nickname! Bem-vindo à bagunça! Deixa o like!")
            listOf("amor", "te amo", "❤️", "💖", "coração").any { text.contains(it) } ->
                Triple("🧽🥰", "AI, MEU CORAÇÃO DE ESPONJA!", "Awnnn! Receba um abraço molhadinho do fundo do mar! ❤️")
            listOf("presente", "rosa", "🎁", "🌹").any { text.contains(it) } ->
                Triple("🧽🥹", "VALEU PELO CARINHO!", "Eita! Obrigado pelo carinho, meu povo! Vocês são demais!")
            listOf("jogo", "subway", "recorde", "pontos", "game").any { text.contains(it) } ->
                Triple("🧽🤩", "BORA BATER RECORDE!", "Foco no recorde! Se eu perder, finjo que foi lag!")
            listOf("burro", "errou", "perdeu", "😢").any { text.contains(it) } ->
                Triple("🧽😵", "OPA, FUI DE BASE!", "Foi mal, meu chapa! Até esponja escorrega. Bora de novo!")
            else ->
                Triple("🧽😏", "O BONECO TÁ DE OLHO NO CHAT!", "Boa, meu consagrado! Manda outra que eu tô inspirado!")
        }
    }

    private fun disconnectFromLive() {
        try {
            liveClient?.disconnect()
            liveClient = null
            connecting.set(false)
            connectButton.isEnabled = true
            disconnectButton.isEnabled = false
            connectionStatus.text = "Desconectado."
            connectionStatus.setTextColor(Color.rgb(250, 204, 120))
            characterFace.text = "🧽😴"
            characterMood.text = "Vou tirar uma soneca no abacaxi..."
        } catch (e: Exception) {
            connectionStatus.text = "Não consegui encerrar normalmente. Feche o app e tente novamente."
        }
    }

    private fun panelBox(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(16, 16, 16, 16)
        setBackgroundColor(panel)
    }

    private fun label(textValue: String, size: Float, color: Int, bold: Boolean): TextView =
        TextView(this).apply {
            text = textValue
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    private fun actionButton(textValue: String): Button = Button(this).apply {
        text = textValue
        isAllCaps = false
        setTextColor(Color.WHITE)
        setBackgroundColor(accent)
        textSize = 14f
    }

    private fun withBottom(params: LinearLayout.LayoutParams, bottom: Int): LinearLayout.LayoutParams {
        params.bottomMargin = bottom
        return params
    }

    private fun matchWidthWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    private fun speak(text: String) {
        if (!voiceEnabled) return
        if (ttsReady && tts != null) {
            tts?.setSpeechRate(1.05f)
            val options = android.os.Bundle().apply { putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.85f) }
            val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, options, "boneco-live-answer")
            if (result == TextToSpeech.ERROR) voiceStatus.text = "Falha ao falar. Confira a voz do Android."
        } else {
            pendingSpeech = text
            voiceStatus.text = "A voz ainda está iniciando; a resposta ficou na fila."
        }
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            ttsReady = false
            voiceStatus.text = "O Android não iniciou a voz. Confira o mecanismo de texto para voz."
            return
        }
        val engine = tts ?: return
        var result = engine.setLanguage(Locale("pt", "BR"))
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            result = engine.setLanguage(Locale("pt"))
        }
        ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        if (ttsReady) {
            voiceStatus.text = "Voz em português pronta."
            pendingSpeech?.let {
                pendingSpeech = null
                speak(it)
            }
        } else {
            voiceStatus.text = "Voz em português indisponível no aparelho."
        }
    }

    override fun onDestroy() {
        try { liveClient?.disconnect() } catch (_: Exception) {}
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    // Personagem desenhado no próprio app: não depende de emoji para mostrar o boneco.
    private inner class SpongeBobView : android.view.View(this@MainActivity) {
        private val p = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(canvas: android.graphics.Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            val cx = w / 2f
            val unit = minOf(w * 0.52f, h * 0.72f)
            val left = cx - unit * 0.43f
            val right = cx + unit * 0.43f
            val top = h * 0.04f
            val bottom = top + unit * 0.67f

            fun paint(color: Int, style: android.graphics.Paint.Style = android.graphics.Paint.Style.FILL, stroke: Float = 1f) {
                p.color = color
                p.style = style
                p.strokeWidth = stroke
            }
            fun oval(x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
                paint(color); canvas.drawOval(x1, y1, x2, y2, p)
            }

            // Pernas e sapatos
            paint(Color.rgb(245, 202, 115), android.graphics.Paint.Style.STROKE, unit * 0.035f)
            canvas.drawLine(cx - unit * 0.2f, bottom + unit * 0.2f, cx - unit * 0.2f, h * 0.88f, p)
            canvas.drawLine(cx + unit * 0.2f, bottom + unit * 0.2f, cx + unit * 0.2f, h * 0.88f, p)
            oval(cx - unit * 0.38f, h * 0.84f, cx - unit * 0.08f, h * 0.96f, Color.BLACK)
            oval(cx + unit * 0.08f, h * 0.84f, cx + unit * 0.38f, h * 0.96f, Color.BLACK)

            // Braços
            paint(Color.rgb(255, 220, 55), android.graphics.Paint.Style.STROKE, unit * 0.045f)
            canvas.drawLine(left, top + unit * 0.36f, left - unit * 0.16f, top + unit * 0.55f, p)
            canvas.drawLine(right, top + unit * 0.36f, right + unit * 0.16f, top + unit * 0.55f, p)
            oval(left - unit * 0.22f, top + unit * 0.49f, left - unit * 0.1f, top + unit * 0.61f, Color.rgb(255, 220, 55))
            oval(right + unit * 0.1f, top + unit * 0.49f, right + unit * 0.22f, top + unit * 0.61f, Color.rgb(255, 220, 55))

            // Corpo quadrado amarelo com poros
            paint(Color.rgb(255, 220, 55))
            canvas.drawRoundRect(left, top, right, bottom, unit * 0.09f, unit * 0.09f, p)
            val pores = listOf(
                floatArrayOf(0.13f,0.18f), floatArrayOf(0.82f,0.16f),
                floatArrayOf(0.08f,0.47f), floatArrayOf(0.9f,0.44f),
                floatArrayOf(0.18f,0.61f), floatArrayOf(0.77f,0.6f)
            )
            pores.forEach {
                oval(left + unit * it[0] - unit * .025f, top + unit * it[1] - unit * .025f,
                    left + unit * it[0] + unit * .025f, top + unit * it[1] + unit * .025f, Color.rgb(220, 166, 31))
            }

            // Olhos grandes azuis
            oval(cx - unit * .3f, top + unit * .12f, cx + unit * .02f, top + unit * .43f, Color.WHITE)
            oval(cx - unit * .02f, top + unit * .12f, cx + unit * .3f, top + unit * .43f, Color.WHITE)
            oval(cx - unit * .205f, top + unit * .2f, cx - unit * .075f, top + unit * .34f, Color.rgb(48, 139, 220))
            oval(cx + unit * .075f, top + unit * .2f, cx + unit * .205f, top + unit * .34f, Color.rgb(48, 139, 220))
            oval(cx - unit * .16f, top + unit * .225f, cx - unit * .095f, top + unit * .305f, Color.BLACK)
            oval(cx + unit * .095f, top + unit * .225f, cx + unit * .16f, top + unit * .305f, Color.BLACK)

            // Bochechas e sorriso
            oval(cx - unit * .34f, top + unit * .42f, cx - unit * .17f, top + unit * .53f, Color.rgb(248, 142, 125))
            oval(cx + unit * .17f, top + unit * .42f, cx + unit * .34f, top + unit * .53f, Color.rgb(248, 142, 125))
            paint(Color.rgb(125, 54, 20), android.graphics.Paint.Style.STROKE, unit * .018f)
            canvas.drawArc(cx - unit * .22f, top + unit * .38f, cx + unit * .22f, top + unit * .62f, 10f, 160f, false, p)
            paint(Color.WHITE)
            canvas.drawRoundRect(cx - unit * .13f, top + unit * .49f, cx + unit * .13f, top + unit * .55f, unit * .01f, unit * .01f, p)

            // Camisa branca, gravata vermelha e calça marrom
            paint(Color.WHITE)
            canvas.drawRect(left + unit * .03f, bottom - unit * .03f, right - unit * .03f, bottom + unit * .13f, p)
            paint(Color.rgb(220, 45, 55))
            val tie = android.graphics.Path().apply {
                moveTo(cx - unit * .055f, bottom)
                lineTo(cx + unit * .055f, bottom)
                lineTo(cx + unit * .035f, bottom + unit * .14f)
                lineTo(cx, bottom + unit * .2f)
                lineTo(cx - unit * .035f, bottom + unit * .14f)
                close()
            }
            canvas.drawPath(tie, p)
            paint(Color.rgb(132, 73, 39))
            canvas.drawRect(left + unit * .03f, bottom + unit * .13f, right - unit * .03f, bottom + unit * .23f, p)
            paint(Color.WHITE)
            canvas.drawCircle(cx - unit * .22f, bottom + unit * .025f, unit * .018f, p)
            canvas.drawCircle(cx + unit * .22f, bottom + unit * .025f, unit * .018f, p)
        }
    }

}
