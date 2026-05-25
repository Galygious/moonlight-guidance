package org.arcanaforge.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.arcanaforge.app.core.database.entity.AIProviderConfigEntity
import org.arcanaforge.app.domain.ai.AiAuthMode
import org.arcanaforge.app.domain.ai.AiProviderType
import org.arcanaforge.app.ui.navigation.AppViewModelFactory
import org.arcanaforge.app.ui.screens.settings.SettingsViewModel.Companion.displaySubtitle

@Composable
fun SettingsScreen(
    viewModelFactory: AppViewModelFactory,
) {
    val viewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.authUrlToOpen) {
        val url = uiState.authUrlToOpen ?: return@LaunchedEffect
        uriHandler.openUri(url)
        viewModel.authUrlOpened()
    }

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessage()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "AI Providers",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "AI is optional. API keys and OAuth tokens stay encrypted on this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            items(uiState.aiConfigs, key = { it.id }) { config ->
                AiProviderConfigRow(
                    config = config,
                    onEnabledChange = { enabled -> viewModel.setProviderEnabled(config.id, enabled) },
                    onDelete = { viewModel.deleteProvider(config.id) },
                )
            }

            item {
                OpenAiAccountCard(
                    isConnecting = uiState.isConnectingOAuth,
                    textModel = uiState.codexTextModel,
                    isSavingModel = uiState.isSavingCodexModel,
                    onTextModelChanged = viewModel::updateCodexTextModel,
                    onSaveModel = viewModel::saveOpenAiCodexModel,
                    onConnect = viewModel::connectOpenAiAccount,
                )
            }

            item {
                ApiKeyCard(
                    uiState = uiState,
                    onProviderTypeChanged = viewModel::updateApiKeyProviderType,
                    onNameChanged = viewModel::updateApiKeyName,
                    onBaseUrlChanged = viewModel::updateApiKeyBaseUrl,
                    onApiKeyChanged = viewModel::updateApiKeyValue,
                    onImageModelChanged = viewModel::updateApiKeyImageModel,
                    onTextModelChanged = viewModel::updateApiKeyTextModel,
                    onSave = viewModel::saveApiKeyConfig,
                )
            }

            item {
                Text(
                    text = "Chakra, crystal, herb, and energy correspondences are provided for reflection and spiritual practice. They are not medical advice or a substitute for professional care.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AiProviderConfigRow(
    config: AIProviderConfigEntity,
    onEnabledChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (config.authMode == AiAuthMode.ApiKey) Icons.Outlined.Key else Icons.Outlined.Link,
                contentDescription = null,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = config.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = config.displaySubtitle(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = config.enabled,
                onCheckedChange = onEnabledChange,
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete AI provider",
                )
            }
        }
    }
}

@Composable
private fun OpenAiAccountCard(
    isConnecting: Boolean,
    textModel: String,
    isSavingModel: Boolean,
    onTextModelChanged: (String) -> Unit,
    onSaveModel: () -> Unit,
    onConnect: () -> Unit,
) {
    Card(shape = MaterialTheme.shapes.medium) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("OpenAI OAuth") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = null,
                        )
                    },
                )
                AssistChip(onClick = {}, label = { Text("Codex-compatible") })
            }
            Text(
                text = "Browser sign-in stores refresh tokens encrypted on this device. The text model is saved with this provider so it can be changed without an app update.",
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedTextField(
                value = textModel,
                onValueChange = onTextModelChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Text model") },
                placeholder = { Text("gpt-5.5") },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onSaveModel,
                    enabled = !isSavingModel,
                ) {
                    Text(if (isSavingModel) "Saving..." else "Save model")
                }
            }
            Button(
                onClick = onConnect,
                enabled = !isConnecting,
            ) {
                Text(if (isConnecting) "Waiting for sign-in..." else "Connect OpenAI account")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApiKeyCard(
    uiState: SettingsUiState,
    onProviderTypeChanged: (AiProviderType) -> Unit,
    onNameChanged: (String) -> Unit,
    onBaseUrlChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onImageModelChanged: (String) -> Unit,
    onTextModelChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    Card(shape = MaterialTheme.shapes.medium) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Bring Your Own Key", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.apiKeyProviderType == AiProviderType.OpenAi,
                    onClick = { onProviderTypeChanged(AiProviderType.OpenAi) },
                    label = { Text("OpenAI") },
                )
                FilterChip(
                    selected = uiState.apiKeyProviderType == AiProviderType.CustomOpenAiCompatible,
                    onClick = { onProviderTypeChanged(AiProviderType.CustomOpenAiCompatible) },
                    label = { Text("Compatible") },
                )
            }
            OutlinedTextField(
                value = uiState.apiKeyName,
                onValueChange = onNameChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Name") },
            )
            if (uiState.apiKeyProviderType == AiProviderType.CustomOpenAiCompatible) {
                OutlinedTextField(
                    value = uiState.apiKeyBaseUrl,
                    onValueChange = onBaseUrlChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Base URL") },
                    placeholder = { Text("https://example.com/v1") },
                )
            }
            OutlinedTextField(
                value = uiState.apiKeyValue,
                onValueChange = onApiKeyChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("API key") },
                visualTransformation = PasswordVisualTransformation(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.apiKeyTextModel,
                    onValueChange = onTextModelChanged,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Text model") },
                )
                OutlinedTextField(
                    value = uiState.apiKeyImageModel,
                    onValueChange = onImageModelChanged,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Image model") },
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = onSave,
                    enabled = !uiState.isSavingApiKey,
                ) {
                    Text(if (uiState.isSavingApiKey) "Saving..." else "Save provider")
                }
            }
        }
    }
}
