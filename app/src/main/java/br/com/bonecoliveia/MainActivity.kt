package br.com.bonecoliveia

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
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
    private lateinit var usernameInput: EditText
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
        tts = TextToSpeech(this, this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 24, 20, 28)
            setBackgroundColor(bg)
        }

        root.addView(label("BONECO LIVE IA", 26f, Color.WHITE, true))
        root.addView(label("REAÇÕES AO VIVO • VERSÃO 0.5.0", 12f, muted, true).apply {
            setPadding(0, 4, 0, 18)
        })

        val hero = panelBox()
        characterFace = label("🧽😎", 62f, Color.WHITE, true).apply { gravity = Gravity.CENTER }
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

        val connectPanel = panelBox()
        connectPanel.addView(label("01  •  CONECTAR AO CHAT REAL", 16f, Color.WHITE, true))
        connectPanel.addView(label("Digite o @ de uma conta que esteja ao vivo.", 13f, muted, false).apply {
            setPadding(0, 5, 0, 10)
        })
        usernameInput = editField("@seuusuario")
        connectPanel.addView(usernameInput, withBottom(matchWidthWrap(), 10))

        connectButton = actionButton("🔴  CONECTAR AO CHAT DA LIVE")
        connectButton.setOnClickListener { connectToLive() }
        connectPanel.addView(connectButton, withBottom(matchWidthWrap(), 8))

        disconnectButton = actionButton("DESCONECTAR")
        disconnectButton.setOnClickListener { disconnectFromLive() }
        disconnectButton.isEnabled = false
        connectPanel.addView(disconnectButton, matchWidthWrap())

        connectionStatus = label("Desconectado. Digite o @ e toque em conectar.", 13f, Color.rgb(250, 204, 120), false).apply {
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
    }

    private fun connectToLive() {
        val username = usernameInput.text.toString().trim().removePrefix("@").trim()
        if (username.isBlank() || !username.matches(Regex("[A-Za-z0-9._]{2,24}"))) {
            usernameInput.error = "Digite um @ válido do TikTok"
            return
        }
        if (!connecting.compareAndSet(false, true)) {
            connectionStatus.text = "Já estou tentando conectar..."
            return
        }
        connectButton.isEnabled = false
        connectionStatus.text = "Conectando a @$username... aguarde."
        characterFace.text = "🧽🤔"
        characterMood.text = "Deixa comigo! Tô entrando no chat..."
        try {
            TikTokLive.newClient(username)
                .configure { settings ->
                    settings.setClientLanguage("pt")
                    settings.setRetryOnConnectionFailure(true)
                    settings.setPrintToConsole(false)
                }
                .onConnected { _, _ ->
                    runOnUiThread {
                        connectionStatus.text = "CONECTADO ao chat de @$username. Esperando comentários..."
                        connectionStatus.setTextColor(Color.rgb(134, 239, 172))
                        characterFace.text = "🧽😎"
                        characterMood.text = "Aêêê! O chat tá aberto, manda ver!"
                        connectButton.isEnabled = false
                        disconnectButton.isEnabled = true
                    }
                }
                .onComment { _, event ->
                    val nickname = event.user.name ?: "alguém"
                    val comment = event.text ?: ""
                    if (comment.isNotBlank()) {
                        runOnUiThread { reactToComment(nickname, comment) }
                    }
                }
                .onGift { _, event ->
                    val nickname = event.user.name ?: "amigo"
                    val giftName = event.gift.name ?: "presente"
                    runOnUiThread { reactToGift(nickname, giftName) }
                }
                .onDisconnected { _, event ->
                    runOnUiThread {
                        connectionStatus.text = "A conexão foi encerrada. Tente conectar novamente."
                        connectionStatus.setTextColor(Color.rgb(250, 204, 120))
                        characterFace.text = "🧽😕"
                        characterMood.text = "Opa, caí do chat! Tenta de novo."
                        connectButton.isEnabled = true
                        disconnectButton.isEnabled = false
                        connecting.set(false)
                        liveClient = null
                    }
                }
                .onError { _, event ->
                    val message = event.exception?.message ?: "Não foi possível conectar."
                    runOnUiThread {
                        connectionStatus.text = "Falha na conexão: " + message.take(120)
                        connectionStatus.setTextColor(Color.rgb(252, 165, 165))
                        characterFace.text = "🧽😵"
                        characterMood.text = "Eita! Deu ruim na conexão. Tenta de novo."
                        connectButton.isEnabled = true
                        disconnectButton.isEnabled = false
                        connecting.set(false)
                    }
                }
                .buildAndConnectAsync()
                .thenAccept { client ->
                    liveClient = client
                    runOnUiThread {
                        connectionStatus.text = "Conexão iniciada. Verificando o chat de @$username..."
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
                    }
                    null
                }
        } catch (e: Exception) {
            connectionStatus.text = "Erro ao iniciar: " + (e.message ?: "tente novamente").take(120)
            connectButton.isEnabled = true
            connecting.set(false)
        }
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

    private fun editField(hintText: String): EditText = EditText(this).apply {
        hint = hintText
        textSize = 15f
        setTextColor(Color.WHITE)
        setHintTextColor(muted)
        setPadding(14, 10, 14, 10)
        setBackgroundColor(Color.rgb(39, 47, 72))
        singleLine = true
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
}
