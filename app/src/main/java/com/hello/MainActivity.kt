package com.hello

import android.app.Dialog
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale

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
    private lateinit var buttons: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textToSpeech = TextToSpeech(this, this)

        // Load button properties from JSON
        buttonPropertiesList = loadButtonProperties()

        setupGridLayout()
        initializeButtons()

        val editModeButton = findViewById<Button>(R.id.editModeButton)
        editModeButton.setOnClickListener {
            toggleEditMode()
        }

        val editAllButton = findViewById<Button>(R.id.editAllButton)
        editAllButton.setOnClickListener {
            editAllMode()
        }

        val doneButton = findViewById<Button>(R.id.doneButton)
        doneButton.setOnClickListener {
            doneEditing()
        }
    }

    private fun setupGridLayout() {
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
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        val editModeButton = findViewById<Button>(R.id.editModeButton)
        if (isEditMode) {
            editModeButton.text = "Cancel Edit Mode"
            findViewById<Button>(R.id.editAllButton).visibility = View.VISIBLE
            findViewById<Button>(R.id.doneButton).visibility = View.VISIBLE
        } else {
            editModeButton.text = "Edit"
            findViewById<Button>(R.id.editAllButton).visibility = View.GONE
            findViewById<Button>(R.id.doneButton).visibility = View.GONE
        }
        selectedButton = null
        initializeButtons()
    }

    private fun editAllMode() {
        isEditMode = true
        findViewById<Button>(R.id.editModeButton).text = "Cancel Edit Mode"
        findViewById<Button>(R.id.editAllButton).visibility = View.GONE
        findViewById<Button>(R.id.doneButton).visibility = View.VISIBLE

        buttons.forEach { button ->
            val index = buttons.indexOf(button)
            val properties = buttonPropertiesList.getOrElse(index) { ButtonProperties("", "Unassigned") }
            button.visibility = View.VISIBLE
            button.setOnClickListener {
                selectedButton = button
                showEditDialog(properties)
            }
        }
    }

    private fun doneEditing() {
        isEditMode = false
        findViewById<Button>(R.id.editModeButton).text = "Edit"
        findViewById<Button>(R.id.editAllButton).visibility = View.VISIBLE
        findViewById<Button>(R.id.doneButton).visibility = View.GONE

        saveButtonProperties()
        initializeButtons()
    }

    private fun initializeButtons() {
        buttons = listOf<Button>(
            findViewById(R.id.button1), findViewById(R.id.button2), findViewById(R.id.button3),
            findViewById(R.id.button4), findViewById(R.id.button5), findViewById(R.id.button6),
            findViewById(R.id.button7), findViewById(R.id.button8), findViewById(R.id.button9),
            findViewById(R.id.button10), findViewById(R.id.button11), findViewById(R.id.button12),
            findViewById(R.id.button13)
        )

        val metrics: DisplayMetrics = resources.displayMetrics
        val density = metrics.density

        val paddingDp = 20
        val paddingPixels = (paddingDp * density).toInt()

        val textSize = 16f

        val screenWidthPx = metrics.widthPixels
        val screenHeightPx = metrics.heightPixels

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
            button.visibility = if (properties.isVisible || isEditMode) View.VISIBLE else View.INVISIBLE

            button.setOnClickListener {
                if (isEditMode) {
                    selectedButton = button
                    showEditDialog(properties)
                } else {
                    speakOut(properties.soundName)
                }
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
                if (!isEditMode) {
                    saveButtonProperties()
                    initializeButtons()
                }
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
                MutableList(13) { ButtonProperties("", "Unassigned") }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading button properties", e)
            MutableList(13) { ButtonProperties("", "Unassigned") }
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