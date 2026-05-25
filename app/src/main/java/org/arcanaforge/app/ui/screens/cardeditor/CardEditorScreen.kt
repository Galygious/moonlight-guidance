package org.arcanaforge.app.ui.screens.cardeditor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.io.File

@Composable
fun CardEditorScreen(
    viewModelFactory: ViewModelProvider.Factory,
    modifier: Modifier = Modifier,
) {
    val viewModel: CardEditorViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            viewModel.attachImage(uri)
        }
    }

    CardEditorContent(
        uiState = uiState,
        onTitleChange = viewModel::updateTitle,
        onSubtitleChange = viewModel::updateSubtitle,
        onSuitChange = viewModel::updateSuit,
        onGroupChange = viewModel::updateGroup,
        onKeywordsChange = viewModel::updateKeywords,
        onUprightMeaningChange = viewModel::updateUprightMeaning,
        onReversedMeaningChange = viewModel::updateReversedMeaning,
        onNotesChange = viewModel::updateNotes,
        onChakrasChange = viewModel::updateChakras,
        onCrystalsChange = viewModel::updateCrystals,
        onElementsChange = viewModel::updateElements,
        onZodiacSignsChange = viewModel::updateZodiacSigns,
        onPlanetsChange = viewModel::updatePlanets,
        onColorsChange = viewModel::updateColors,
        onHerbsChange = viewModel::updateHerbs,
        onCustomCorrespondencesChange = viewModel::updateCustomCorrespondences,
        onAiPromptChange = viewModel::updateAiPrompt,
        onSave = viewModel::saveCard,
        onPickImage = {
            imagePicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun CardEditorContent(
    uiState: CardEditorUiState,
    onTitleChange: (String) -> Unit,
    onSubtitleChange: (String) -> Unit,
    onSuitChange: (String) -> Unit,
    onGroupChange: (String) -> Unit,
    onKeywordsChange: (String) -> Unit,
    onUprightMeaningChange: (String) -> Unit,
    onReversedMeaningChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onChakrasChange: (String) -> Unit,
    onCrystalsChange: (String) -> Unit,
    onElementsChange: (String) -> Unit,
    onZodiacSignsChange: (String) -> Unit,
    onPlanetsChange: (String) -> Unit,
    onColorsChange: (String) -> Unit,
    onHerbsChange: (String) -> Unit,
    onCustomCorrespondencesChange: (String) -> Unit,
    onAiPromptChange: (String) -> Unit,
    onSave: () -> Unit,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (uiState.isLoading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        item {
            Text(
                text = uiState.card?.title ?: "Card",
                style = MaterialTheme.typography.headlineMedium,
            )
        }

        item {
            ImagePanel(
                uiState = uiState,
                onPickImage = onPickImage,
            )
        }

        uiState.errorMessage?.let { message ->
            item {
                MessageCard(message = message, isError = true)
            }
        }

        uiState.statusMessage?.let { message ->
            item {
                MessageCard(message = message, isError = false)
            }
        }

        item {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Title") },
            )
        }
        item {
            OutlinedTextField(
                value = uiState.subtitle,
                onValueChange = onSubtitleChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Subtitle") },
            )
        }
        item {
            OutlinedTextField(
                value = uiState.suit,
                onValueChange = onSuitChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Suit") },
            )
        }
        item {
            OutlinedTextField(
                value = uiState.group,
                onValueChange = onGroupChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Group") },
            )
        }
        item {
            OutlinedTextField(
                value = uiState.keywordsText,
                onValueChange = onKeywordsChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Keywords") },
                supportingText = { Text("Comma-separated") },
            )
        }
        item {
            OutlinedTextField(
                value = uiState.uprightMeaning,
                onValueChange = onUprightMeaningChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text("Upright meaning") },
            )
        }
        item {
            OutlinedTextField(
                value = uiState.reversedMeaning,
                onValueChange = onReversedMeaningChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text("Reversed meaning") },
            )
        }
        item {
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text("Personal notes") },
            )
        }
        item {
            CorrespondencesSection(
                uiState = uiState,
                onChakrasChange = onChakrasChange,
                onCrystalsChange = onCrystalsChange,
                onElementsChange = onElementsChange,
                onZodiacSignsChange = onZodiacSignsChange,
                onPlanetsChange = onPlanetsChange,
                onColorsChange = onColorsChange,
                onHerbsChange = onHerbsChange,
                onCustomCorrespondencesChange = onCustomCorrespondencesChange,
            )
        }
        item {
            OutlinedTextField(
                value = uiState.aiPrompt,
                onValueChange = onAiPromptChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                label = { Text("AI prompt metadata") },
            )
        }
        item {
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Text("Save Card")
            }
        }
    }
}

@Composable
private fun CorrespondencesSection(
    uiState: CardEditorUiState,
    onChakrasChange: (String) -> Unit,
    onCrystalsChange: (String) -> Unit,
    onElementsChange: (String) -> Unit,
    onZodiacSignsChange: (String) -> Unit,
    onPlanetsChange: (String) -> Unit,
    onColorsChange: (String) -> Unit,
    onHerbsChange: (String) -> Unit,
    onCustomCorrespondencesChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Correspondences",
            style = MaterialTheme.typography.titleLarge,
        )
        Card {
            Text(
                text = "Chakra, crystal, herb, and energy correspondences are provided for reflection and spiritual practice. They are not medical advice or a substitute for professional care.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        CorrespondenceTextField(
            value = uiState.chakrasText,
            onValueChange = onChakrasChange,
            label = "Chakras",
        )
        CorrespondenceTextField(
            value = uiState.crystalsText,
            onValueChange = onCrystalsChange,
            label = "Crystals",
        )
        CorrespondenceTextField(
            value = uiState.elementsText,
            onValueChange = onElementsChange,
            label = "Elements",
        )
        CorrespondenceTextField(
            value = uiState.zodiacSignsText,
            onValueChange = onZodiacSignsChange,
            label = "Zodiac signs",
        )
        CorrespondenceTextField(
            value = uiState.planetsText,
            onValueChange = onPlanetsChange,
            label = "Planets",
        )
        CorrespondenceTextField(
            value = uiState.colorsText,
            onValueChange = onColorsChange,
            label = "Colors",
        )
        CorrespondenceTextField(
            value = uiState.herbsText,
            onValueChange = onHerbsChange,
            label = "Herbs / plants",
        )
        OutlinedTextField(
            value = uiState.customCorrespondencesText,
            onValueChange = onCustomCorrespondencesChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            label = { Text("Custom correspondences") },
            supportingText = { Text("One per line, like: Symbol: Moon, Water") },
        )
    }
}

@Composable
private fun CorrespondenceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        supportingText = { Text("Comma-separated") },
    )
}

@Composable
private fun ImagePanel(
    uiState: CardEditorUiState,
    onPickImage: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                val imagePath = uiState.image?.thumbnailPath ?: uiState.image?.localPath
                if (imagePath != null) {
                    AsyncImage(
                        model = File(imagePath),
                        contentDescription = "Card image",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.ImageNotSupported,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "No image attached",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Button(
            onClick = onPickImage,
            enabled = !uiState.isImportingImage,
            modifier = Modifier.fillMaxWidth(),
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Outlined.AddPhotoAlternate,
                contentDescription = null,
            )
            Text(
                text = if (uiState.isImportingImage) "Importing..." else "Attach Image",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean,
) {
    Card {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )
    }
}
