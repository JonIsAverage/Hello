package com.hello

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.view.View
import android.app.Dialog
import android.util.DisplayMetrics
import android.widget.GridLayout
import android.widget.CheckBox


data class ButtonProperties(var displayName: String, var soundName: String, var isVisible: Boolean = true)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var buttonPropertiesList: Array<ButtonProperties>

    private var isEditMode = false
    private var selectedButton: Button? = null

    /* Declare Button Properties Below */
    private var button1Properties = ButtonProperties("", "Unassigned")
    private var button2Properties = ButtonProperties("", "Unassigned")
    private var button3Properties = ButtonProperties("", "Unassigned")
    private var button4Properties = ButtonProperties("", "Unassigned")
    private var button5Properties = ButtonProperties("", "Unassigned")
    private var button6Properties = ButtonProperties("", "Unassigned")
    private var button7Properties = ButtonProperties("", "Unassigned")
    private var button8Properties = ButtonProperties("", "Unassigned")
    private var button9Properties = ButtonProperties("", "Unassigned")
    private var button10Properties = ButtonProperties("", "Unassigned")
    private var button11Properties = ButtonProperties("", "Unassigned")
    private var button12Properties = ButtonProperties("", "Unassigned")
    private var button13Properties = ButtonProperties("", "Unassigned")
    /* Declare Button Properties Above */

    private lateinit var textToSpeech: TextToSpeech


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        textToSpeech = TextToSpeech(this, this)

        // Get screen dimensions in pixels
        val displayMetrics = resources.displayMetrics
        val screenWidthPx = displayMetrics.widthPixels
        val screenHeightPx = displayMetrics.heightPixels

        val columnCount = when {
            screenWidthPx > 1200 -> 4 // Large screens
            screenWidthPx > 600 -> 3 // Medium screens
            else -> 2 // Small screens
        }

        val rowCount = when {
            screenHeightPx > 1800 -> 6 // Tall screens
            screenHeightPx > 1200 -> 5 // Large screens
            screenHeightPx > 800 -> 4 // Medium screens
            else -> 3 // Small screens
        }

        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)

        gridLayout.columnCount = columnCount
        gridLayout.rowCount = rowCount

        /* Declare Buttons */
        val button1 = findViewById<Button>(R.id.button1)
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)
        val button4 = findViewById<Button>(R.id.button4)
        val button5 = findViewById<Button>(R.id.button5)
        val button6 = findViewById<Button>(R.id.button6)
        val button7 = findViewById<Button>(R.id.button7)
        val button8 = findViewById<Button>(R.id.button8)
        val button9 = findViewById<Button>(R.id.button9)
        val button10 = findViewById<Button>(R.id.button10)
        val button11 = findViewById<Button>(R.id.button11)
        val button12 = findViewById<Button>(R.id.button12)
        val button13 = findViewById<Button>(R.id.button13)
        /* Declare Buttons */


        /* Declare Button Attributes */
        val metrics: DisplayMetrics = resources.displayMetrics
        val density = metrics.density

        val paddingDp = 20
        val paddingPixels = (paddingDp * density).toInt()

        val textSize = 16f

        val buttonSize = (screenWidthPx / columnCount).coerceAtMost(screenHeightPx / rowCount)
        val minButtonSize = resources.getDimensionPixelSize(R.dimen.min_button_size) // Example minimum size from resources
        val finalButtonSize = buttonSize.coerceAtMost(minButtonSize)

        val buttons = listOf<Button>(button1, button2, button3, button4, button5, button6, button7, button8, button9, button10, button11, button12, button13)

        buttons.forEach { button ->
            val buttonLayoutParams = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f), // Row span (1)
                GridLayout.spec(GridLayout.UNDEFINED, 1f) // Column span (1)
            )

            buttonLayoutParams.width = finalButtonSize
            buttonLayoutParams.height = finalButtonSize
            button.layoutParams = buttonLayoutParams
            button.textSize = textSize
            button.setPadding(paddingPixels, paddingPixels, paddingPixels, paddingPixels)
        }


        button1.visibility = View.VISIBLE
        button2.visibility = View.VISIBLE
        button3.visibility = View.VISIBLE
        button4.visibility = View.VISIBLE
        button5.visibility = View.VISIBLE
        button6.visibility = View.VISIBLE
        button7.visibility = View.INVISIBLE
        button8.visibility = View.INVISIBLE
        button9.visibility = View.INVISIBLE
        button10.visibility = View.INVISIBLE
        button11.visibility = View.INVISIBLE
        button12.visibility = View.INVISIBLE
        button13.visibility = View.INVISIBLE
        /* Declare Button Attributes */

        val editModeButton = findViewById<Button>(R.id.editModeButton)

        button1.setOnClickListener {
            if (isEditMode) {
                selectedButton = button1
                showEditDialog(button1Properties)
            } else {
                speakOut(button1Properties.soundName)
            }
        }

        button2.setOnClickListener {
            if (isEditMode) {
                selectedButton = button2
                showEditDialog(button2Properties)
            } else {
                speakOut(button2Properties.soundName)
            }
        }

        button3.setOnClickListener {
            if (isEditMode) {
                selectedButton = button3
                showEditDialog(button3Properties)
            } else {
                speakOut(button3Properties.soundName)
            }
        }

        button4.setOnClickListener {
            if (isEditMode) {
                selectedButton = button4
                showEditDialog(button4Properties)
            } else {
                speakOut(button4Properties.soundName)
            }
        }

        button5.setOnClickListener {
            if (isEditMode) {
                selectedButton = button5
                showEditDialog(button5Properties)
            } else {
                speakOut(button5Properties.soundName)
            }
        }

        button6.setOnClickListener {
            if (isEditMode) {
                selectedButton = button6
                showEditDialog(button6Properties)
            } else {
                speakOut(button6Properties.soundName)
            }
        }

        button7.setOnClickListener {
            if (isEditMode) {
                selectedButton = button7
                showEditDialog(button7Properties)
            } else {
                speakOut(button7Properties.soundName)
            }
        }

        button8.setOnClickListener {
            if (isEditMode) {
                selectedButton = button8
                showEditDialog(button8Properties)
            } else {
                speakOut(button8Properties.soundName)
            }
        }

        button9.setOnClickListener {
            if (isEditMode) {
                selectedButton = button9
                showEditDialog(button9Properties)
            } else {
                speakOut(button9Properties.soundName)
            }
        }

        button10.setOnClickListener {
            if (isEditMode) {
                selectedButton = button10
                showEditDialog(button10Properties)
            } else {
                speakOut(button10Properties.soundName)
            }
        }

        button11.setOnClickListener {
            if (isEditMode) {
                selectedButton = button11
                showEditDialog(button11Properties)
            } else {
                speakOut(button11Properties.soundName)
            }
        }

        button12.setOnClickListener {
            if (isEditMode) {
                selectedButton = button12
                showEditDialog(button12Properties)
            } else {
                speakOut(button12Properties.soundName)
            }
        }

        button13.setOnClickListener {
            if (isEditMode) {
                selectedButton = button13
                showEditDialog(button13Properties)
            } else {
                speakOut(button13Properties.soundName)
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
        val checkboxVisibility = dialog.findViewById<CheckBox>(R.id.checkboxVisibility)

        displayNameEditText.setText(buttonProperties.displayName)
        soundNameEditText.setText(buttonProperties.soundName)

        checkboxVisibility.isChecked = selectedButton?.visibility == View.VISIBLE

        displayNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                displayNameEditText.post {
                    displayNameEditText.selectAll()
                }
            }
        }

        soundNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                soundNameEditText.post {
                    soundNameEditText.selectAll()
                }
            }
        }

        confirmButton.setOnClickListener {
            val displayName = displayNameEditText.text.toString()
            val soundName = soundNameEditText.text.toString()

            if (!checkboxVisibility.isChecked || (displayName.isNotBlank() && soundName.isNotBlank())) {
                buttonProperties.displayName = displayName
                buttonProperties.soundName = soundName
                selectedButton?.text = displayName

                if (checkboxVisibility.isChecked) {
                    selectedButton?.visibility = View.VISIBLE
                } else {
                    selectedButton?.visibility = View.INVISIBLE
                }

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