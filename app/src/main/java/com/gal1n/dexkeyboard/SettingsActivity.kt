package com.gal1n.dexkeyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        root.addView(TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 26f
        })

        root.addView(TextView(this).apply {
            text = getString(R.string.note)
            textSize = 15f
            setPadding(0, 24, 0, 24)
        })

        root.addView(Button(this).apply {
            text = getString(R.string.enable_keyboard)
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        })

        root.addView(Button(this).apply {
            text = getString(R.string.select_keyboard)
            setOnClickListener {
                getSystemService(InputMethodManager::class.java).showInputMethodPicker()
            }
        })

        setContentView(root)
    }
}
