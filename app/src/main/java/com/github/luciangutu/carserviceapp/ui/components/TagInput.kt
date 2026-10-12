package com.github.luciangutu.carserviceapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TagInput(
    selectedTags: List<String>,
    onTagsChanged: (List<String>) -> Unit,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    label: String = "Operations",
    onPendingTagChanged: (String) -> Unit = {}
) {
    var textFieldValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val filteredSuggestions = suggestions.filter {
        it.contains(textFieldValue, ignoreCase = true) && !selectedTags.contains(it)
    }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded && filteredSuggestions.isNotEmpty(),
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    onPendingTagChanged(it)
                    expanded = true
                    if (it.endsWith(",") || it.endsWith("\n")) {
                        val newTag = it.trim().removeSuffix(",").trim()
                        if (newTag.isNotEmpty() && !selectedTags.contains(newTag)) {
                            onTagsChanged(selectedTags + newTag)
                            onPendingTagChanged("")
                        }
                        textFieldValue = ""
                        expanded = false
                    }
                },
                label = { Text(label) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val newTag = textFieldValue.trim()
                        if (newTag.isNotEmpty() && !selectedTags.contains(newTag)) {
                            onTagsChanged(selectedTags + newTag)
                            onPendingTagChanged("")
                        }
                        textFieldValue = ""
                        expanded = false
                    }
                )
            )

            if (filteredSuggestions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    filteredSuggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                if (!selectedTags.contains(suggestion)) {
                                    onTagsChanged(selectedTags + suggestion)
                                    onPendingTagChanged("")
                                }
                                textFieldValue = ""
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedTags.forEach { tag ->
                InputChip(
                    selected = true,
                    onClick = { /* Nothing */ },
                    label = { Text(tag) },
                    trailingIcon = {
                        IconButton(
                            onClick = { onTagsChanged(selectedTags - tag) },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove $tag",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                )
            }
        }
    }
}

