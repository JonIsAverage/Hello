package com.hello

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.*
import android.view.View
import android.app.Dialog

data class ButtonProperties(var displayName: String, var soundName: String)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var isEditMode = false
    private var selectedButton: Button? = null

    /* Declare Button Properties Below */
    private var button1Properties = ButtonProperties("Button 1", "Sound1")
    private var button2Properties = ButtonProperties("Button 2", "Sound2")
    /* Declare Button Properties Above */

    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        textToSpeech = TextToSpeech(this, this)

        /* Declare Buttons Below */
        val button1 = findViewById<Button>(R.id.button1)
        val button2 = findViewById<Button>(R.id.button2)
        /* Declare Buttons Above */

        val editModeButton = findViewById<Button>(R.id.editModeButton)

        val buttons = arrayOf("Button 1", "Button 3")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, buttons)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        button1.setOnClickListener {
            if (isEditMode) {
                selectedButton = button1
                showEditDialog(button1Properties)
                Toast.makeText(this, "Button 1 selected for editing", Toast.LENGTH_SHORT).show()
            } else {
                speakOut(button1Properties.soundName)
            }
        }

        button2.setOnClickListener {
            if (isEditMode) {
                selectedButton = button2
                showEditDialog(button2Properties)
                Toast.makeText(this, "Button 2 selected for editing", Toast.LENGTH_SHORT).show()
            } else {
                speakOut(button2Properties.soundName)
            }
        }

        editModeButton.setOnClickListener {
            isEditMode = !isEditMode
            editModeButton.text = if (isEditMode) "Cancel Edit Mode" else "Edit"
            selectedButton = null
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle the error
            } else {
                // Text-to-Speech is ready to use
            }
        } else {
            // Initialization failed
        }
    }

    private fun showEditDialog(buttonProperties: ButtonProperties) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_edit_button)

        val displayNameEditText = dialog.findViewById<EditText>(R.id.displayNameEditText)
        val soundNameEditText = dialog.findViewById<EditText>(R.id.soundNameEditText)
        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)

        displayNameEditText.setText(buttonProperties.displayName)
        soundNameEditText.setText(buttonProperties.soundName)

        confirmButton.setOnClickListener {
            val displayName = displayNameEditText.text.toString()
            val soundName = soundNameEditText.text.toString()

            if (displayName.isNotBlank() && soundName.isNotBlank()) {
                buttonProperties.displayName = displayName
                buttonProperties.soundName = soundName
                selectedButton?.text = displayName
                dialog.dismiss()
                isEditMode = false
                findViewById<Button>(R.id.editModeButton).text = "Edit"
                selectedButton = null
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun speakOut(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}