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
import androidx.compose.material3.*
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.function.UnaryOperator

private const val TAG = "MainActivity"

@Serializable
data class ButtonProperties(
    var buttonId: Int = 0,
    var hierarchyId: Int = 1, // Default hierarchyId set to 1
    var displayName: String = "",
    var soundName: String = "",
    var isVisible: Boolean = true,
    var isGroup: Boolean = false,
    var groupName: String = "",
    var parentButtonId: Int = 0
)

object ButtonPropertiesManager {
    private val TAG = "ButtonPropertiesManager"

    fun saveButtonProperties(
        propertiesList: List<ButtonProperties>,
        buttonPropertiesFile: File
    ) {
        try {
            val json = Json.encodeToString(propertiesList)
            buttonPropertiesFile.writeText(json)
            Log.d(TAG, "Button properties saved: $json")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving button properties", e)
        }
    }

    fun loadButtonProperties(
        buttonPropertiesFile: File
    ): MutableList<ButtonProperties> {
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
}

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var isEditMode by mutableStateOf(false)
    private lateinit var textToSpeech: TextToSpeech
    private var buttonPropertiesList by mutableStateOf(mutableListOf<ButtonProperties>())
    private val buttonPropertiesFile by lazy { File(filesDir, "button_properties.json") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buttonPropertiesList = ButtonPropertiesManager.loadButtonProperties(buttonPropertiesFile)
        setContent {
            MyApp(
                buttonPropertiesList = buttonPropertiesList,
                isEditMode = isEditMode,
                toggleEditMode = { isEditMode = !isEditMode },
                doneEditing = {
                    ButtonPropertiesManager.saveButtonProperties(buttonPropertiesList, buttonPropertiesFile)
                    isEditMode = false
                },
                speakOut = { text -> speakOut(text) },
                buttonPropertiesFile = buttonPropertiesFile // Pass the file to MyApp
            )
        }
        textToSpeech = TextToSpeech(this, this)
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
    speakOut: (String) -> Unit,
    buttonPropertiesFile: File // Add the file parameter
) {
    val navigateToSecondScreen = remember { mutableStateOf(false) }
    val selectedButtonProperties = remember { mutableStateOf<ButtonProperties?>(null) }

    // Function to navigate to startup page
    val navigateToStartup = {
        navigateToSecondScreen.value = false
        selectedButtonProperties.value = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = navigateToStartup,
            ) {
                Text("Home")
            }
            Button(onClick = toggleEditMode) {
                Text(text = if (isEditMode) "Cancel Edit Mode" else "Edit")
            }
        }
        val mainPageButtons = buttonPropertiesList.filter { it.hierarchyId == 1 && it.parentButtonId == 0 }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(mainPageButtons.size) { index ->
                val properties = mainPageButtons[index]
                if (properties.isVisible || isEditMode) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (isEditMode) {
                                    // Edit mode: Select button for editing
                                    selectedButtonProperties.value = properties.copy()
                                } else if (properties.isGroup) {
                                    // Navigate to second screen for this group
                                    navigateToSecondScreen.value = true
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
        if (selectedButtonProperties.value != null && isEditMode && !navigateToSecondScreen.value) {
            EditButtonDialog(
                buttonProperties = selectedButtonProperties.value!!,
                onDismiss = {
                    selectedButtonProperties.value = null
                },
                onConfirm = { updatedProperties ->
                    val index = buttonPropertiesList.indexOfFirst { it == selectedButtonProperties.value }
                    if (index != -1) {
                        buttonPropertiesList[index] = updatedProperties
                    }
                    selectedButtonProperties.value = null
                    doneEditing() // Save changes
                }
            )
        }
        if (navigateToSecondScreen.value) {
            selectedButtonProperties.value?.let { properties ->
                SecondScreen(
                    buttonPropertiesList = buttonPropertiesList,
                    parentButtonId = properties.buttonId,
                    navigateToStartup = navigateToStartup,
                    isEditMode = isEditMode,
                    buttonPropertiesFile = buttonPropertiesFile,
                    doneEditing = doneEditing
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
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isGroup, onCheckedChange = { isChecked -> isGroup = isChecked })
                    Text(text = "Group?")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isVisible, onCheckedChange = { isChecked -> isVisible = isChecked })
                    Text(text = "Visible?")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    buttonProperties.copy(
                        displayName = displayName,
                        soundName = soundName,
                        groupName = groupName,
                        isGroup = isGroup,
                        isVisible = isVisible
                    )
                )
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SecondScreen(
    buttonPropertiesList: MutableList<ButtonProperties>,
    parentButtonId: Int,
    isEditMode: Boolean,
    navigateToStartup: () -> Unit,
    buttonPropertiesFile: File,
    doneEditing: () -> Unit
) {
    // Ensure childButtons are remembered and mutable
    val childButtons = remember {
        // Load existing buttons from buttonPropertiesList filtered by parentButtonId and hierarchyId
        val existingButtons = buttonPropertiesList.filter { it.parentButtonId == parentButtonId && it.hierarchyId == 2 }

        if (existingButtons.isEmpty()) {
            // If no existing buttons, create new buttons
            val newButtons = List(24) { index ->
                ButtonProperties(
                    buttonId = buttonPropertiesList.size + index + 1,
                    hierarchyId = 2,
                    parentButtonId = parentButtonId,
                    displayName = "Button ${buttonPropertiesList.size + index + 1}",
                    soundName = "",
                    isVisible = true,
                    isGroup = false,
                    groupName = ""
                )
            }
            // Add new buttons to the main buttonPropertiesList
            buttonPropertiesList.addAll(newButtons)
            // Save the updated button properties list
            ButtonPropertiesManager.saveButtonProperties(buttonPropertiesList, buttonPropertiesFile)
            // Return the new buttons as mutable state list
            newButtons.toMutableStateList()
        } else {
            // Return existing buttons as mutable state list
            existingButtons.toMutableStateList()
        }
    }

    // Remember the selected button properties for editing
    val selectedButtonProperties = remember { mutableStateOf<ButtonProperties?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(childButtons.size) { index ->
                val properties = childButtons[index]
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (isEditMode) {
                                selectedButtonProperties.value = properties.copy()
                            } else {
                                // Handle child button actions or navigation if needed
                                // For now, let's leave this empty or handle specific actions
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

        // EditButtonDialog for editing selectedButtonProperties
        if (selectedButtonProperties.value != null && isEditMode) {
            EditButtonDialog(
                buttonProperties = selectedButtonProperties.value!!,
                onDismiss = {
                    selectedButtonProperties.value = null
                },
                onConfirm = { updatedProperties ->
                    // Find the index of the selected button in childButtons
                    val index = childButtons.indexOfFirst { it == selectedButtonProperties.value }
                    if (index != -1) {
                        // Update the button at the found index with updated properties
                        childButtons[index] = updatedProperties
                        // Update the original buttonPropertiesList with updated properties
                        buttonPropertiesList.replaceAll {
                            if (it == selectedButtonProperties.value) updatedProperties else it
                        }
                        // Save the updated button properties list to file
                        ButtonPropertiesManager.saveButtonProperties(buttonPropertiesList, buttonPropertiesFile)
                        // Trigger doneEditing callback to finalize changes
                        doneEditing()
                    }
                    // Reset selectedButtonProperties after editing
                    selectedButtonProperties.value = null
                }
            )
        }
    }
}