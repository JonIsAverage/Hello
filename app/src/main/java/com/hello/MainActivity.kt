package com.hello

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

private const val TAG = "MainActivity"

@Serializable
data class ButtonProperties(
    var buttonId: Int = 0,
    var hierarchyId: Int = 1,
    var displayName: String = "",
    var soundName: String = "",
    var isVisible: Boolean = true,
    var isGroup: Boolean = false,
    var groupName: String = "",
    var parentButtonId: Int = 0
)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var isEditMode by mutableStateOf(false)
    private lateinit var textToSpeech: TextToSpeech
    private var buttonPropertiesList by mutableStateOf(mutableListOf<ButtonProperties>())
    private val buttonPropertiesFile by lazy { File(filesDir, "button_properties.json") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buttonPropertiesList = loadButtonProperties()
        setContent {
            MyApp(
                buttonPropertiesList = buttonPropertiesList,
                isEditMode = isEditMode,
                toggleEditMode = { isEditMode = !isEditMode },
                doneEditing = {
                    saveButtonProperties(buttonPropertiesList)
                    isEditMode = false
                },
                speakOut = { text -> speakOut(text) }
            )
        }
        textToSpeech = TextToSpeech(this, this)
    }

    private fun saveButtonProperties(propertiesList: List<ButtonProperties>) {
        try {
            val json = Json.encodeToString(propertiesList)
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
                Json.decodeFromString(json)
            } else {
                MutableList(24) { index ->
                    ButtonProperties(
                        buttonId = index + 1,
                        hierarchyId = 1,
                        displayName = "",
                        soundName = "",
                        isVisible = true,
                        isGroup = false,
                        groupName = ""
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading button properties", e)
            MutableList(24) { index ->
                ButtonProperties(
                    buttonId = index + 1,
                    hierarchyId = 1,
                    displayName = "",
                    soundName = "",
                    isVisible = true,
                    isGroup = false,
                    groupName = ""
                )
            }
        }
    }

    private fun speakOut(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
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

    override fun onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}

@Composable
fun MyApp(
    buttonPropertiesList: MutableList<ButtonProperties>,
    isEditMode: Boolean,
    toggleEditMode: () -> Unit,
    doneEditing: () -> Unit,
    speakOut: (String) -> Unit
) {
    val navigateToSecondScreen = remember { mutableStateOf(false) }
    val selectedButtonProperties = remember { mutableStateOf<ButtonProperties?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isEditMode) {
                Button(onClick = doneEditing) {
                    Text("Done")
                }
            }
            Button(onClick = toggleEditMode) {
                Text(text = if (isEditMode) "Cancel Edit Mode" else "Edit")
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(buttonPropertiesList.size) { index ->
                val properties = buttonPropertiesList[index]
                if (properties.isVisible || isEditMode) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (properties.isGroup) {
                                    // Navigate to second screen for this group
                                    navigateToSecondScreen.value = true
                                    selectedButtonProperties.value = properties
                                } else if (isEditMode) {
                                    // Edit mode: Select button for editing
                                    selectedButtonProperties.value = properties
                                } else {
                                    // Handle other actions or navigation if needed
                                    speakOut(properties.soundName)
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (properties.isVisible) Color.LightGray else Color.Gray),
                            shape = RectangleShape,
                            content = {
                                Text(
                                    text = if (properties.isGroup) properties.groupName else properties.displayName,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        )
                    }
                }
            }
        }
        if (selectedButtonProperties.value != null && isEditMode) {
            EditButtonDialog(
                buttonProperties = selectedButtonProperties.value!!,
                onDismiss = { selectedButtonProperties.value = null },
                onConfirm = { updatedProperties ->
                    val index = buttonPropertiesList.indexOfFirst { it == selectedButtonProperties.value }
                    if (index != -1) {
                        buttonPropertiesList[index] = updatedProperties
                    }
                    selectedButtonProperties.value = null
                }
            )
        }
        if (navigateToSecondScreen.value) {
            selectedButtonProperties.value?.let { properties ->
                val groupButtons = buttonPropertiesList.filter { it.groupName == properties.groupName }
                SecondScreen(
                    buttonPropertiesList = groupButtons.toMutableList(),
                    parentButtonId = properties.buttonId
                )
            }
        }
    }
}

@Composable
fun EditButtonDialog(
    buttonProperties: ButtonProperties,
    onDismiss: () -> Unit,
    onConfirm: (ButtonProperties) -> Unit
) {
    var displayName by remember { mutableStateOf(buttonProperties.displayName) }
    var soundName by remember { mutableStateOf(buttonProperties.soundName) }
    var groupName by remember { mutableStateOf(buttonProperties.groupName) }
    var isGroup by remember { mutableStateOf(buttonProperties.isGroup) }
    var isVisible by remember { mutableStateOf(buttonProperties.isVisible) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Button") },
        text = {
            Column {
                if (!isGroup) {
                    BasicTextField(
                        value = displayName,
                        onValueChange = { newValue -> displayName = newValue },
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (displayName.isEmpty()) {
                                Text(text = "Display Name", color = Color.Gray)
                            }
                            innerTextField()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BasicTextField(
                        value = soundName,
                        onValueChange = { newValue -> soundName = newValue },
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (soundName.isEmpty()) {
                                Text(text = "Sound Name", color = Color.Gray)
                            }
                            innerTextField()
                        }
                    )
                } else {
                    BasicTextField(
                        value = groupName,
                        onValueChange = { newValue -> groupName = newValue },
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (groupName.isEmpty()) {
                                Text(text = "Group Name", color = Color.Gray)
                            }
                            innerTextField()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isGroup, onCheckedChange = { isChecked ->
                            isGroup = isChecked
                        })
                        Text("Group?")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isVisible, onCheckedChange = { isChecked ->
                            isVisible = isChecked
                        })
                        Text("Visible")
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        buttonProperties.displayName = displayName
                        buttonProperties.soundName = soundName
                        buttonProperties.groupName = groupName
                        buttonProperties.isGroup = isGroup
                        buttonProperties.isVisible = isVisible
                        onConfirm(buttonProperties)
                        onDismiss()
                    }
                ) {
                    Text("Confirm")
                }
                Button(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun SecondScreen(buttonPropertiesList: MutableList<ButtonProperties>, parentButtonId: Int) {
    // Filter button properties based on parentButtonId
    val filteredButtons = buttonPropertiesList.filter { it.parentButtonId == parentButtonId }

    Column(modifier = Modifier.fillMaxSize()) {
        for (properties in filteredButtons) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .clickable {
                        // Handle navigation or any other action on button click if needed
                    }
            ) {
                Button(
                    onClick = {
                        // Handle button click as needed
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray, shape = RectangleShape), // Background color and shape
                    shape = RectangleShape, // Button shape
                    content = {
                        Text(
                            text = properties.displayName,
                            color = Color.White, // Text color
                            textAlign = TextAlign.Center
                        )
                    }
                )
            }
        }
    }
}