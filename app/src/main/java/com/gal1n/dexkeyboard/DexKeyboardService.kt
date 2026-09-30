package com.gal1n.dexkeyboard

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

class DexKeyboardService : InputMethodService() {
    enum class Layout { ENGLISH, BULGARIAN }

    private lateinit var prefs: SharedPreferences
    private lateinit var root: LinearLayout
    private var layoutMode = Layout.ENGLISH
    private var shift = false

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("geometry", Context.MODE_PRIVATE)
        layoutMode = if (prefs.getBoolean(KEY_BG, false)) Layout.BULGARIAN else Layout.ENGLISH
    }

    // DeX/landscape editors can otherwise trigger Android's fullscreen extract UI.
    // Keep the real editor visible behind the IME so the user always sees the text field.
    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onEvaluateInputViewShown(): Boolean = true

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(6), dp(4), dp(6), dp(4))
            setBackgroundColor(Color.rgb(32, 33, 36))
        }
        rebuild()
        applyWindowGeometry()
        return root
    }

    override fun onStartInput(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        shift = false
        rebuild()
    }

    private fun rebuild() {
        if (!::root.isInitialized) return
        root.removeAllViews()

        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(button(if (layoutMode == Layout.ENGLISH) "EN" else "БГ", true) {
            setLayout(if (layoutMode == Layout.ENGLISH) Layout.BULGARIAN else Layout.ENGLISH)
        }, LinearLayout.LayoutParams(dp(52), dp(30)))

        header.addView(TextView(this).apply {
            text = "DEX Keyboard  •  drag = move  •  right handle = resize"
            textSize = 10f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(0, dp(30), 1f))

        header.addView(button("✕", true) { requestHideSelf(0) }, LinearLayout.LayoutParams(dp(42), dp(30)))
        root.addView(header)

        addRow(listOf("1","2","3","4","5","6","7","8","9","0"))

        val rows = if (layoutMode == Layout.ENGLISH) listOf(
            "qwertyuiop".map(Char::toString),
            "asdfghjkl".map(Char::toString),
            "zxcvbnm".map(Char::toString)
        ) else listOf(
            listOf("ч","ш","е","р","т","ъ","у","и","о","п"),
            listOf("а","с","д","ф","г","х","й","к","л"),
            listOf("з","ж","ц","в","б","н","м","ь")
        )
        rows.forEach(::addRow)

        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        bottom.addView(button("⇧", true) { shift = !shift; rebuild() }, params())
        bottom.addView(button("⌫", true) { currentInputConnection?.deleteSurroundingText(1, 0) }, params())
        bottom.addView(button("space", true) { currentInputConnection?.commitText(" ", 1) }, params(2.3f))
        bottom.addView(button(if (layoutMode == Layout.ENGLISH) "БГ" else "EN", true) {
            setLayout(if (layoutMode == Layout.ENGLISH) Layout.BULGARIAN else Layout.ENGLISH)
        }, params())
        bottom.addView(button("↵", true) {
            currentInputConnection?.performEditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_DONE)
        }, params())
        root.addView(bottom)

        root.addView(DragResizeBar(this))
    }

    private fun setLayout(newLayout: Layout) {
        layoutMode = newLayout
        prefs.edit().putBoolean(KEY_BG, newLayout == Layout.BULGARIAN).apply()
        shift = false
        rebuild()
    }

    private fun addRow(labels: List<String>) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        labels.forEach { row.addView(button(if (shift) it.uppercase(Locale.ROOT) else it), params()) }
        root.addView(row)
    }

    private fun button(label: String, special: Boolean = false, action: (() -> Unit)? = null): Button {
        return Button(this).apply {
            text = label
            isAllCaps = false
            textSize = if (special) 12f else 18f
            setTextColor(Color.WHITE)
            minHeight = 0; minimumHeight = 0; minWidth = 0; minimumWidth = 0
            setPadding(0, 0, 0, 0)
            background = rounded(if (special) Color.rgb(95,99,104) else Color.rgb(60,64,67))
            setOnClickListener {
                if (action != null) action()
                else {
                    currentInputConnection?.commitText(label, 1)
                    if (shift) { shift = false; rebuild() }
                }
            }
        }
    }

    private fun rounded(color: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(6).toFloat()
        setStroke(dp(1), Color.rgb(32,33,36))
    }

    private fun params(weight: Float = 1f) = LinearLayout.LayoutParams(0, dp(42), weight).apply {
        setMargins(dp(2), dp(2), dp(2), dp(2))
    }

    private fun applyWindowGeometry() {
        val w: Window = window?.window ?: return
        val lp = w.attributes
        val width = max(420, prefs.getInt(KEY_WIDTH, 760))
        val height = max(190, prefs.getInt(KEY_HEIGHT, 330))
        val hasPosition = prefs.contains(KEY_X) && prefs.contains(KEY_Y)
        lp.width = dp(width)
        lp.height = dp(height)
        lp.gravity = if (hasPosition) Gravity.TOP or Gravity.START else Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        if (hasPosition) {
            lp.x = dp(prefs.getInt(KEY_X, 20))
            lp.y = dp(prefs.getInt(KEY_Y, 20))
        }
        w.attributes = lp
    }

    private inner class DragResizeBar(context: Context) : TextView(context) {
        private var lastX = 0f
        private var lastY = 0f
        private var resize = false

        init {
            text = "···"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(-1, dp(18))
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX
                    lastY = event.rawY
                    resize = event.x > width * 0.72f
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val w = window?.window ?: return true
                    val lp = w.attributes
                    val density = resources.displayMetrics.density
                    val dx = ((event.rawX - lastX) / density).roundToInt()
                    val dy = ((event.rawY - lastY) / density).roundToInt()
                    if (resize) {
                        val currentW = lp.width / density
                        val currentH = lp.height / density
                        lp.width = dp(max(420, (currentW + dx).roundToInt()))
                        lp.height = dp(max(190, (currentH + dy).roundToInt()))
                        prefs.edit()
                            .putInt(KEY_WIDTH, (lp.width / density).roundToInt())
                            .putInt(KEY_HEIGHT, (lp.height / density).roundToInt())
                            .apply()
                    } else {
                        lp.gravity = Gravity.TOP or Gravity.START
                        lp.x += dp(dx)
                        lp.y += dp(dy)
                        prefs.edit()
                            .putInt(KEY_X, max(0, lp.x / density.roundToIntSafe()))
                            .putInt(KEY_Y, max(0, lp.y / density.roundToIntSafe()))
                            .apply()
                    }
                    w.attributes = lp
                    lastX = event.rawX
                    lastY = event.rawY
                    return true
                }
            }
            return true
        }
    }

    private fun Float.roundToIntSafe(): Int = max(1, roundToInt())

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).roundToInt()

    companion object {
        const val KEY_WIDTH = "width"
        const val KEY_HEIGHT = "height"
        const val KEY_X = "x"
        const val KEY_Y = "y"
        const val KEY_BG = "bg"
    }
}
