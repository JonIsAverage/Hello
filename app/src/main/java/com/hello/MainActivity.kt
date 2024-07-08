package com.hello

import android.app.Dialog
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale
import kotlinx.serialization.encodeToString
import android.util.Log

private const val TAG = "MainActivity"

private val jsonFormat = Json { prettyPrint = true }

@Serializable
data class ButtonProperties(var displayName: String, var soundName: String, var isVisible: Boolean = true)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var isEditMode = false
    private var selectedButton: Button? = null
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var buttonPropertiesList: MutableList<ButtonProperties>
    private val buttonPropertiesFile by lazy { File(filesDir, "button_properties.json") }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textToSpeech = TextToSpeech(this, this)

        buttonPropertiesList = loadButtonProperties()

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
        val buttons = listOf(button1, button2, button3, button4, button5, button6, button7, button8, button9, button10, button11, button12, button13)
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

        buttons.forEachIndexed { index, button ->
            val buttonLayoutParams = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
            )

            buttonLayoutParams.width = finalButtonSize
            buttonLayoutParams.height = finalButtonSize
            button.layoutParams = buttonLayoutParams
            button.textSize = textSize
            button.setPadding(paddingPixels, paddingPixels, paddingPixels, paddingPixels)

            // Load button properties
            val properties = buttonPropertiesList.getOrElse(index) { ButtonProperties("", "Unassigned") }
            button.text = properties.displayName
            button.visibility = if (properties.isVisible) View.VISIBLE else View.INVISIBLE

            button.setOnClickListener {
                if (isEditMode) {
                    selectedButton = button
                    showEditDialog(properties)
                } else {
                    speakOut(properties.soundName)
                }
            }
        }

        /* Declare Button Attributes */

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

        val editModeButton = findViewById<Button>(R.id.editModeButton)
        editModeButton.setOnClickListener {
            isEditMode = !isEditMode
            if (isEditMode) {
                editModeButton.text = "Cancel Edit Mode"
                buttons.forEach { it.visibility = View.VISIBLE }
            } else {
                editModeButton.text = "Edit"
                buttons.forEachIndexed { index, button ->
                    button.visibility = if (buttonPropertiesList[index].isVisible) View.VISIBLE else View.INVISIBLE
                }
                saveButtonProperties()
            }
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
        checkboxVisibility.isChecked = buttonProperties.isVisible

        displayNameEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                (v as EditText).post {
                    v.selectAll()
                }
            }
        }

        soundNameEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                (v as EditText).post {
                    v.selectAll()
                }
            }
        }

        confirmButton.setOnClickListener {
            val displayName = displayNameEditText.text.toString()
            val soundName = soundNameEditText.text.toString()

            if (!checkboxVisibility.isChecked || (displayName.isNotBlank() && soundName.isNotBlank())) {
                buttonProperties.displayName = displayName
                buttonProperties.soundName = soundName
                buttonProperties.isVisible = checkboxVisibility.isChecked

                selectedButton?.apply {
                    text = displayName
                    visibility = if (buttonProperties.isVisible) View.VISIBLE else View.INVISIBLE
                }

                dialog.dismiss()
                isEditMode = false
                findViewById<Button>(R.id.editModeButton).text = "Edit"
                selectedButton = null

                saveButtonProperties()
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun saveButtonProperties() {
        try {
            val json = jsonFormat.encodeToString(buttonPropertiesList)
            buttonPropertiesFile.writeText(json)
            Log.d(TAG, "Button properties saved: $json")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving button properties", e)
        }
    }

    private fun loadButtonProperties(): MutableList<ButtonProperties> {
        return try {
            if (buttonPropertiesFile.exists()) {
                val json = buttonPropertiesFile.readText()
                Log.d(TAG, "Button properties loaded: $json")
                jsonFormat.decodeFromString(json)
            } else {
                MutableList(13) { ButtonProperties("", "Unassigned", it < 6) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading button properties", e)
            MutableList(13) { ButtonProperties("", "Unassigned", it < 6) }
        }
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