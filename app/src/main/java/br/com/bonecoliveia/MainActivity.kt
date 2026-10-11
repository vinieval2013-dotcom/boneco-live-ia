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
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingSpeech: String? = null
    private lateinit var usernameInput: EditText
    private lateinit var commentInput: EditText
    private lateinit var answerText: TextView
    private lateinit var voiceStatus: TextView
    private lateinit var connectionStatus: TextView
    private lateinit var characterFace: TextView
    private lateinit var characterMood: TextView

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
        root.addView(label("SEU COMPANHEIRO VIRTUAL DE LIVE", 12f, muted, true).apply {
            setPadding(0, 4, 0, 18)
        })

        val hero = panelBox()
        characterFace = label("🤖", 66f, Color.WHITE, true).apply { gravity = Gravity.CENTER }
        hero.addView(characterFace, matchWidthWrap())
        characterMood = label("Pronto para entrar em cena!", 17f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 8)
        }
        hero.addView(characterMood, matchWidthWrap())
        hero.addView(label("O boneco reage ao texto de teste com expressões e respostas faladas.", 13f, muted, false).apply {
            gravity = Gravity.CENTER
        }, matchWidthWrap())
        root.addView(hero, withBottom(matchWidthWrap(), 16))

        val connectPanel = panelBox()
        connectPanel.addView(label("01  •  CONECTAR AO TIKTOK", 16f, Color.WHITE, true))
        connectPanel.addView(label("Digite o @ da conta que está ao vivo.", 13f, muted, false).apply {
            setPadding(0, 5, 0, 10)
        })
        usernameInput = editField("@seuusuario", false)
        connectPanel.addView(usernameInput, withBottom(matchWidthWrap(), 10))
        val connectButton = actionButton("ABRIR LIVE DO TIKTOK")
        connectButton.setOnClickListener { openTikTokLive() }
        connectPanel.addView(connectButton, matchWidthWrap())
        connectionStatus = label("Ainda não abriu nenhuma LIVE.", 13f, Color.rgb(250, 204, 120), false).apply {
            setPadding(0, 10, 0, 0)
        }
        connectPanel.addView(connectionStatus, matchWidthWrap())
        root.addView(connectPanel, withBottom(matchWidthWrap(), 16))

        val interactionPanel = panelBox()
        interactionPanel.addView(label("02  •  TESTAR INTERAÇÃO", 16f, Color.WHITE, true))
        interactionPanel.addView(label("Escreva como se fosse um comentário da live.", 13f, muted, false).apply {
            setPadding(0, 5, 0, 10)
        })
        commentInput = editField("Ex.: Oi boneco! Você é muito engraçado", true)
        interactionPanel.addView(commentInput, withBottom(matchWidthWrap(), 10))
        val answerButton = actionButton("✨  RESPONDER COM EMOÇÃO")
        answerButton.setOnClickListener {
            val comment = commentInput.text.toString().trim()
            if (comment.isBlank()) {
                commentInput.error = "Digite um comentário para testar"
                return@setOnClickListener
            }
            val result = makeResponse(comment)
            characterFace.text = result.face
            characterMood.text = result.mood
            answerText.text = result.answer
            speak(result.answer)
        }
        interactionPanel.addView(answerButton, matchWidthWrap())
        answerText = label("A resposta do boneco aparecerá aqui.", 16f, Color.WHITE, false).apply {
            setPadding(0, 16, 0, 4)
        }
        interactionPanel.addView(answerText, matchWidthWrap())
        root.addView(interactionPanel, withBottom(matchWidthWrap(), 16))

        val voicePanel = panelBox()
        voicePanel.addView(label("03  •  VOZ DO BONECO", 16f, Color.WHITE, true))
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

        root.addView(label("IMPORTANTE", 12f, Color.rgb(250, 204, 120), true))
        root.addView(label("Esta versão abre a página pública da LIVE pelo @ informado e demonstra as emoções com comentários digitados. Ela ainda não captura comentários ou presentes automaticamente. Para isso é necessária uma integração compatível com o TikTok; inserir só o nome de usuário não libera esses dados por si só.", 12f, muted, false).apply {
            setPadding(0, 6, 0, 0)
        }, matchWidthWrap())

        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(root)
            setBackgroundColor(bg)
        })
    }

    private data class CharacterResponse(val face: String, val mood: String, val answer: String)

    private fun makeResponse(comment: String): CharacterResponse {
        val text = comment.lowercase(Locale.ROOT)
        return when {
            listOf("presente", "rosa", "gift", "🎁", "🌹").any { text.contains(it) } ->
                CharacterResponse("🥹", "EMOCIONADO COM O PRESENTE!", "Eita, que carinho! Muito obrigado pelo presente, você é fera! 💜")
            listOf("kkkk", "haha", "😂", "engraçado", "piada").any { text.contains(it) } ->
                CharacterResponse("😂", "CAINDO NA RISADA!", "KKKKK vocês não prestam! Manda outra aí que eu tô gostando!")
            listOf("oi", "olá", "ola", "salve", "cheguei", "boa noite", "bom dia").any { text.contains(it) } ->
                CharacterResponse("😎", "FELIZ COM A SUA CHEGADA!", "Opa, salve! Seja muito bem-vindo à live! Já deixa o like e fica com a gente!")
            listOf("amor", "te amo", "❤️", "💖", "coração").any { text.contains(it) } ->
                CharacterResponse("🥰", "MANDANDO CARINHO!", "Aí meu coração! Receba um abraço virtual, tamo junto demais! ❤️")
            listOf("perdeu", "errou", "não", "nao", "triste", "😢").any { text.contains(it) } ->
                CharacterResponse("😮", "SURPRESO COM O COMENTÁRIO!", "Opa, calma aí! Aqui a gente tenta de novo e não desiste nunca!")
            listOf("jogo", "subway", "recorde", "pontos", "game").any { text.contains(it) } ->
                CharacterResponse("🤩", "NO CLIMA DO JOGO!", "Bora buscar esse recorde! Comenta aí quantos pontos você acha que eu faço!")
            else ->
                CharacterResponse("🤖", "ATENTO AO CHAT!", "Valeu pelo comentário! Tamo junto na live! Manda mais uma mensagem aí!")
        }
    }

    private fun openTikTokLive() {
        val raw = usernameInput.text.toString().trim().removePrefix("@").trim()
        if (raw.isBlank()) {
            usernameInput.error = "Digite o nome de usuário do TikTok"
            return
        }
        if (!raw.matches(Regex("[A-Za-z0-9._]{2,24}"))) {
            usernameInput.error = "Confira o @ informado"
            return
        }
        val url = "https://www.tiktok.com/@$raw/live"
        connectionStatus.text = "Abrindo a página da LIVE de @$raw..."
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            connectionStatus.text = "Não foi possível abrir o TikTok. Confira se há um navegador instalado."
            Toast.makeText(this, "Não consegui abrir a LIVE.", Toast.LENGTH_LONG).show()
        }
    }

    private fun speak(text: String) {
        if (ttsReady && tts != null) {
            voiceStatus.text = "Enviando resposta para a voz..."
            val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "boneco-live-answer")
            if (result == TextToSpeech.ERROR) {
                voiceStatus.text = "A voz falhou. Tente iniciar novamente."
            } else {
                voiceStatus.text = "Resposta enviada para a voz do Android."
            }
        } else {
            pendingSpeech = text
            voiceStatus.text = "A voz ainda está iniciando. Vou tentar falar quando ficar pronta."
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

    private fun editField(hintText: String, multiline: Boolean): EditText = EditText(this).apply {
        hint = hintText
        textSize = 15f
        setTextColor(Color.WHITE)
        setHintTextColor(muted)
        setPadding(14, 10, 14, 10)
        setBackgroundColor(Color.rgb(39, 47, 72))
        if (multiline) {
            minLines = 2
            gravity = Gravity.TOP
        } else {
            singleLine = true
        }
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

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}
