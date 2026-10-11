package br.com.bonecoliveia

import android.app.Activity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private lateinit var commentInput: EditText
    private lateinit var answerText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 28, 24, 24)
            setBackgroundColor(Color.rgb(15, 18, 32))
        }

        val title = TextView(this).apply {
            text = "🤖 Boneco Live IA"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        root.addView(title, matchWidthWrap())

        val subtitle = TextView(this).apply {
            text = "Protótipo • teste respostas faladas"
            textSize = 15f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 22)
        }
        root.addView(subtitle, matchWidthWrap())

        commentInput = EditText(this).apply {
            hint = "Digite um comentário de teste"
            textSize = 16f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(14, 12, 14, 12)
            setBackgroundColor(Color.rgb(40, 44, 60))
        }
        root.addView(commentInput, matchWidthWrap())

        answerText = TextView(this).apply {
            text = "A resposta aparecerá aqui."
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(0, 20, 0, 18)
        }
        root.addView(answerText, matchWidthWrap())

        val answerButton = Button(this).apply {
            text = "Gerar resposta de teste"
            setOnClickListener {
                val comment = commentInput.text.toString().trim()
                val answer = makeDemoAnswer(comment)
                answerText.text = answer
                speak(answer)
            }
        }
        root.addView(answerButton, matchWidthWrap())

        val note = TextView(this).apply {
            text = "Importante: esta versão é um protótipo. Ainda não recebe comentários do TikTok nem conecta uma IA online."
            textSize = 14f
            setTextColor(Color.rgb(255, 210, 120))
            setPadding(0, 22, 0, 0)
        }
        root.addView(note, matchWidthWrap())

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun makeDemoAnswer(comment: String): String {
        if (comment.isBlank()) return "Digite um comentário primeiro."
        return when {
            comment.contains("oi", ignoreCase = true) ||
                comment.contains("olá", ignoreCase = true) ||
                comment.contains("ola", ignoreCase = true) ->
                "Opa, salve! Seja muito bem-vindo à live!"
            comment.contains("nome", ignoreCase = true) ->
                "Eu sou o Boneco Live IA. Manda mais perguntas aí!"
            comment.contains("jogo", ignoreCase = true) ->
                "Bora acompanhar o jogo! Deixa o like e fica na live!"
            else -> "Valeu pelo comentário! Tamo junto na live!"
        }
    }

    private fun speak(text: String) {
        if (ttsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "boneco-live-answer")
        } else {
            answerText.text = "$text\n\nA voz ainda está iniciando. Tente novamente em alguns segundos."
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("pt", "BR"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }

    private fun matchWidthWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
