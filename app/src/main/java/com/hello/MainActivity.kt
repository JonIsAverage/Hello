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

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var isEditMode = false
    private var selectedButton: Button? = null
    private var buttonSoundName: String? = null

    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        textToSpeech = TextToSpeech(this, this)

        val button1 = findViewById<Button>(R.id.button1)
        val editText = findViewById<EditText>(R.id.editText)
        val button3 = findViewById<Button>(R.id.button3)
        val changeTextButton = findViewById<Button>(R.id.changeTextButton)
        val editModeButton = findViewById<Button>(R.id.editModeButton)

        val buttons = arrayOf("Button 1", "Button 3")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, buttons)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        button1.setOnClickListener {
            if (isEditMode) {
                selectedButton = button1
                showEditDialog()
                Toast.makeText(this, "Button 1 selected for editing", Toast.LENGTH_SHORT).show()
            } else {
                speakOut(buttonSoundName ?: button1.text.toString())
            }
        }
        button3.setOnClickListener {
            if (isEditMode) {
                selectedButton = button3
                showEditDialog()
                Toast.makeText(this, "Button 3 selected for editing", Toast.LENGTH_SHORT).show()
            } else {
                speakOut(buttonSoundName ?: button3.text.toString())
            }
        }
        editModeButton.setOnClickListener {
            isEditMode = !isEditMode
            editModeButton.text = if (isEditMode) "Cancel Edit Mode" else "Edit"
            selectedButton = null
        }
        changeTextButton.setOnClickListener {
            val newText = editText.text.toString()
            if (isEditMode && selectedButton != null) {
                if (newText.isNotBlank()) {
                    selectedButton?.text = newText
                    isEditMode = false
                    editModeButton.text = "Edit"
                    selectedButton = null
                } else {
                    Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please select a button to edit", Toast.LENGTH_SHORT).show()
            }
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

    private fun showEditDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_edit_button)

        val displayNameEditText = dialog.findViewById<EditText>(R.id.displayNameEditText)
        val soundNameEditText = dialog.findViewById<EditText>(R.id.soundNameEditText)
        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)

        confirmButton.setOnClickListener {
            val displayName = displayNameEditText.text.toString()
            val soundName = soundNameEditText.text.toString()

            if (displayName.isNotBlank() && soundName.isNotBlank()) {
                selectedButton?.text = displayName
                buttonSoundName = soundName
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