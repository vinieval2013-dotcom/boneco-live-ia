package br.com.bonecoliveia

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private var lulaScore = 0
    private var bolsonaroScore = 0
    private lateinit var lulaScoreView: TextView
    private lateinit var bolsonaroScoreView: TextView
    private lateinit var totalView: TextView
    private lateinit var lulaBar: View
    private lateinit var bolsonaroBar: View
    private lateinit var barRow: LinearLayout

    private val backgroundColor = Color.rgb(10, 15, 28)
    private val cardColor = Color.rgb(22, 30, 48)
    private val textColor = Color.rgb(245, 247, 252)
    private val mutedColor = Color.rgb(160, 172, 193)
    private val lulaColor = Color.rgb(34, 166, 106)
    private val bolsonaroColor = Color.rgb(53, 116, 230)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor
        window.decorView.systemUiVisibility = 0

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(24))
            setBackgroundColor(backgroundColor)
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(page)
        }

        page.addView(text("LULA  ×  BOLSONARO", 25f, textColor, true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.04f
        }, matchWrap())
        page.addView(text("COMPETIÇÃO • ESCOLHA SEU LADO", 11f, mutedColor, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(7), 0, dp(22))
            letterSpacing = 0.12f
        }, matchWrap())

        val contestCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(20), dp(16), dp(20))
            background = rounded(cardColor, 22)
        }

        val candidates = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        candidates.addView(candidatePanel("L", "LULA", "PT", lulaColor), LinearLayout.LayoutParams(0, dp(164), 1f))
        candidates.addView(LinearLayout(this).apply {
            gravity = Gravity.CENTER
            addView(text("VS", 18f, mutedColor, true))
        }, LinearLayout.LayoutParams(dp(48), dp(164)))
        candidates.addView(candidatePanel("B", "BOLSONARO", "PL", bolsonaroColor), LinearLayout.LayoutParams(0, dp(164), 1f))
        contestCard.addView(candidates, matchWrap())

        val scoreRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(22), 0, dp(4))
        }
        val leftScore = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        lulaScoreView = text("0", 34f, textColor, true).apply { gravity = Gravity.CENTER }
        leftScore.addView(lulaScoreView, matchWrap())
        leftScore.addView(text("PONTOS", 10f, mutedColor, true).apply { gravity = Gravity.CENTER }, matchWrap())
        val rightScore = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        bolsonaroScoreView = text("0", 34f, textColor, true).apply { gravity = Gravity.CENTER }
        rightScore.addView(bolsonaroScoreView, matchWrap())
        rightScore.addView(text("PONTOS", 10f, mutedColor, true).apply { gravity = Gravity.CENTER }, matchWrap())
        scoreRow.addView(leftScore, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        scoreRow.addView(rightScore, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        contestCard.addView(scoreRow, matchWrap())

        totalView = text("0 escolhas nesta rodada", 12f, mutedColor, false).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(16))
        }
        contestCard.addView(totalView, matchWrap())

        barRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = rounded(Color.rgb(45, 54, 73), 10)
            clipToOutline = true
        }
        lulaBar = View(this).apply { setBackgroundColor(lulaColor) }
        bolsonaroBar = View(this).apply { setBackgroundColor(bolsonaroColor) }
        barRow.addView(lulaBar, LinearLayout.LayoutParams(1, dp(10), 1f))
        barRow.addView(bolsonaroBar, LinearLayout.LayoutParams(1, dp(10), 1f))
        contestCard.addView(barRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(10)))
        page.addView(contestCard, withBottom(matchWrap(), 18))

        page.addView(text("QUEM VOCÊ ESCOLHE?", 15f, textColor, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(12))
        }, matchWrap())

        val lulaButton = button("ESCOLHER LULA", lulaColor)
        lulaButton.setOnClickListener {
            lulaScore++
            refreshScores()
        }
        page.addView(lulaButton, withBottom(matchWrap(), 10))

        val bolsonaroButton = button("ESCOLHER BOLSONARO", bolsonaroColor)
        bolsonaroButton.setOnClickListener {
            bolsonaroScore++
            refreshScores()
        }
        page.addView(bolsonaroButton, withBottom(matchWrap(), 18))

        val note = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(13), dp(14), dp(13))
            background = rounded(Color.rgb(17, 24, 39), 14)
        }
        note.addView(text("PLACAR DA BRINCADEIRA", 12f, textColor, true), matchWrap())
        note.addView(text("Cada toque soma um ponto neste aparelho. O placar é local e não representa pesquisa eleitoral nem resultado oficial.", 12f, mutedColor, false).apply {
            setPadding(0, dp(6), 0, 0)
        }, matchWrap())
        page.addView(note, withBottom(matchWrap(), 18))

        val reset = button("ZERAR PLACAR", Color.rgb(65, 76, 97))
        reset.setOnClickListener {
            lulaScore = 0
            bolsonaroScore = 0
            refreshScores()
        }
        page.addView(reset, matchWrap())
        setContentView(scroll)
        refreshScores()
    }

    private fun candidatePanel(initial: String, name: String, party: String, color: Int): LinearLayout {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(5), dp(12), dp(5), dp(12))
            background = rounded(Color.rgb(30, 39, 59), 16)
        }
        val avatar = TextView(this).apply {
            text = initial
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = rounded(color, 50)
        }
        panel.addView(avatar, LinearLayout.LayoutParams(dp(58), dp(58)).apply { bottomMargin = dp(10) })
        panel.addView(text(name, 13f, textColor, true).apply {
            gravity = Gravity.CENTER
            maxLines = 1
        }, matchWrap())
        panel.addView(text(party, 11f, mutedColor, false).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, 0)
        }, matchWrap())
        return panel
    }

    private fun refreshScores() {
        lulaScoreView.text = lulaScore.toString()
        bolsonaroScoreView.text = bolsonaroScore.toString()
        val total = lulaScore + bolsonaroScore
        totalView.text = "$total escolhas nesta rodada"
        val lulaWeight = if (total == 0) 1f else lulaScore.toFloat() / total
        val bolsonaroWeight = if (total == 0) 1f else bolsonaroScore.toFloat() / total
        barRow.removeAllViews()
        barRow.addView(lulaBar, LinearLayout.LayoutParams(1, dp(10), lulaWeight))
        barRow.addView(bolsonaroBar, LinearLayout.LayoutParams(1, dp(10), bolsonaroWeight))
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

    private fun button(title: String, color: Int): Button = Button(this).apply {
        text = title
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.WHITE)
        isAllCaps = false
        background = rounded(color, 14)
        minHeight = dp(54)
        setPadding(dp(14), dp(10), dp(14), dp(10))
    }

    private fun rounded(color: Int, radiusDp: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )

    private fun withBottom(params: LinearLayout.LayoutParams, marginDp: Int) = params.apply {
        bottomMargin = dp(marginDp)
    }
}
