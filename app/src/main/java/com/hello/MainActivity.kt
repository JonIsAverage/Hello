package com.hello

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale

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

@Serializable
data class PasscodeProperties(
    var passCode: String = ""
)

object PasscodePropertiesManager {
    private const val TAG = "PasscodePropertiesManager"

    fun savePasscodeProperties(
        passcodeProperties: PasscodeProperties,
        passcodePropertiesFile: File
    ) {
        try {
            val json = Json.encodeToString(passcodeProperties)
            passcodePropertiesFile.writeText(json)
            Log.d(TAG, "Passcode saved: $json")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving passcode", e)
        }
    }

    fun loadPasscodeProperties(
        passcodePropertiesFile: File
    ): PasscodeProperties {
        return try {
            if (passcodePropertiesFile.exists()) {
                val json = passcodePropertiesFile.readText()
                Log.d(TAG, "Passcode loaded: $json")
                Json.decodeFromString(json)
            } else {
                PasscodeProperties()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading passcode", e)
            PasscodeProperties()
        }
    }
}

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var isEditMode by mutableStateOf(false)
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var passcodeProperties: PasscodeProperties
    private var buttonPropertiesList by mutableStateOf(mutableListOf<ButtonProperties>())
    private val buttonPropertiesFile by lazy { File(filesDir, "button_properties.json") }
    private val passcodePropertiesFile by lazy { File(filesDir, "passcode_properties.json") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        passcodeProperties = PasscodePropertiesManager.loadPasscodeProperties(passcodePropertiesFile)
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
                buttonPropertiesFile = buttonPropertiesFile,
                passcodeProperties = passcodeProperties,
                updatePasscode = { newPasscode ->
                    passcodeProperties = newPasscode
                    PasscodePropertiesManager.savePasscodeProperties(passcodeProperties, passcodePropertiesFile)
                }
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
    buttonPropertiesFile: File,
    passcodeProperties: PasscodeProperties,
    updatePasscode: (PasscodeProperties) -> Unit
) {
    val navigateToSecondScreen = remember { mutableStateOf(false) }
    val selectedButtonProperties = remember { mutableStateOf<ButtonProperties?>(null) }
    var showPasscodeDialog by remember { mutableStateOf(false) }
    var showCreatePasscodeDialog by remember { mutableStateOf(false) }

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
            Button(onClick = navigateToStartup) {
                Text("Home")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = {
                if (passcodeProperties.passCode.isNotEmpty()) {
                    showPasscodeDialog = true
                } else {
                    showCreatePasscodeDialog = true
                }
            }) {
                Text("Edit")
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
                    doneEditing = doneEditing,
                    speakOut = speakOut // Pass speakOut here
                )
            }
        }
        // Dialogs for passcode
        if (showPasscodeDialog) {
            PasscodeDialog(
                passcodeProperties = passcodeProperties,
                onDismissRequest = { showPasscodeDialog = false },
                onSubmit = { enteredPasscode ->
                    if (enteredPasscode == passcodeProperties.passCode) {
                        toggleEditMode()
                    } else {
                        // Handle incorrect passcode
                    }
                }
            )
        }
        if (showCreatePasscodeDialog) {
            CreatePasscodeDialog(
                onDismissRequest = { showCreatePasscodeDialog = false },
                onSubmit = { newPasscode ->
                    passcodeProperties.passCode = newPasscode
                    updatePasscode(passcodeProperties)
                    toggleEditMode()
                }
            )
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
    doneEditing: () -> Unit,
    speakOut: (String) -> Unit // Add speakOut as a parameter
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
                    displayName = "",
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
                            if (!isEditMode && properties.isVisible && !properties.isGroup) {
                                speakOut(properties.soundName)
                            } else if (isEditMode) {
                                selectedButtonProperties.value = properties.copy()
                            } else {
                                // Handle other scenarios if needed
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

@Composable
fun PasscodeDialog(
    passcodeProperties: PasscodeProperties,
    onDismissRequest: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var passcode by remember { mutableStateOf("") }
    var showMessage by remember { mutableStateOf(false) } // Flag to control message visibility
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Enter Passcode") },
        text = {
            Column {
                Text("Please enter your passcode.")
                TextField(
                    value = passcode,
                    onValueChange = { passcode = it },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                if (showMessage) {
                    Text(text = message, color = if (message == "Passcode Correct!") Color.Green else Color.Red)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val isCorrect = passcode == passcodeProperties.passCode
                    showMessage = true
                    message = if (isCorrect) "Passcode Correct!" else "Incorrect Passcode"
                    if (isCorrect) {
                        onSubmit(passcode)
                        onDismissRequest()
                    }
                    // Implement logic to potentially dismiss the dialog after showing the message (see next step)
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreatePasscodeDialog(
    onDismissRequest: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var passcode by remember { mutableStateOf("") }
    var confirmPasscode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Create Passcode") },
        text = {
            Column {
                Text("Please create a new passcode.")
                TextField(
                    value = passcode,
                    onValueChange = { passcode = it },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text("Passcode") }
                )
                TextField(
                    value = confirmPasscode,
                    onValueChange = { confirmPasscode = it },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text("Confirm Passcode") }
                )
                if (passcode != confirmPasscode) {
                    Text("Passcodes do not match", color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (passcode == confirmPasscode) {
                        onSubmit(passcode)
                        onDismissRequest()
                    }
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}