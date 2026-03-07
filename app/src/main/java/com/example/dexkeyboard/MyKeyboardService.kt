package com.example.dexkeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.View
import android.view.inputmethod.InputConnection
import android.view.KeyEvent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class MyKeyboardService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var englishKeyboard: Keyboard
    private lateinit var bulgarianKeyboard: Keyboard
    private var isEnglish = true

    override fun onCreateInputView(): View {
        englishKeyboard = Keyboard(this, R.xml.qwerty)
        bulgarianKeyboard = Keyboard(this, R.xml.bulgarian_phonetic)

        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        setKeyboard(englishKeyboard)
        keyboardView.setOnKeyboardActionListener(this)

        // Прилагаме запазения размер
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val keyHeight = prefs.getInt("keyboard_height", 60)
        keyboardView.layoutParams.height = dpToPx(keyHeight)

        return keyboardView
    }

    private fun setKeyboard(keyboard: Keyboard) {
        keyboardView.keyboard = keyboard
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val inputConnection: InputConnection? = currentInputConnection
        inputConnection ?: return

        when (primaryCode) {
            -101 -> { // Смяна на езика
                isEnglish = !isEnglish
                if (isEnglish) setKeyboard(englishKeyboard)
                else setKeyboard(bulgarianKeyboard)
            }
            -102 -> { // Отваряне на настройките
                val intent = android.content.Intent(this, SettingsActivity::class.java)
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            Keyboard.KEYCODE_DELETE -> {
                inputConnection.deleteSurroundingText(1, 0)
            }
            Keyboard.KEYCODE_DONE -> {
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            }
            else -> {
                inputConnection.commitText(primaryCode.toChar().toString(), 1)
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeDown() {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeUp() {}
}
