package com.preichert.cinemerick.feature.settings.presentation.key

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.preichert.cinemerick.core.designsystem.CinemerickFilterChip
import com.preichert.cinemerick.core.designsystem.CinemerickIconButton
import com.preichert.cinemerick.core.designsystem.CinemerickIcons
import com.preichert.cinemerick.core.designsystem.CinemerickPrimaryButton
import com.preichert.cinemerick.core.designsystem.CinemerickTonalButton
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.domain.assistant.CloudProvider
import com.preichert.cinemerick.core.domain.assistant.SavedKey
import com.preichert.cinemerick.core.presentation.ObserveAsEvents
import com.preichert.cinemerick.core.presentation.assistant.ProviderMark
import com.preichert.cinemerick.feature.settings.presentation.SettingsHeader
import com.preichert.cinemerick.feature.settings.presentation.resources.Res
import com.preichert.cinemerick.feature.settings.presentation.resources.key_error_empty
import com.preichert.cinemerick.feature.settings.presentation.resources.key_error_invalid
import com.preichert.cinemerick.feature.settings.presentation.resources.key_error_offline
import com.preichert.cinemerick.feature.settings.presentation.resources.key_error_other
import com.preichert.cinemerick.feature.settings.presentation.resources.key_error_rate_limited
import com.preichert.cinemerick.feature.settings.presentation.resources.key_help_prefix
import com.preichert.cinemerick.feature.settings.presentation.resources.key_hide
import com.preichert.cinemerick.feature.settings.presentation.resources.key_label
import com.preichert.cinemerick.feature.settings.presentation.resources.key_model
import com.preichert.cinemerick.feature.settings.presentation.resources.key_model_help
import com.preichert.cinemerick.feature.settings.presentation.resources.key_provider
import com.preichert.cinemerick.feature.settings.presentation.resources.key_save
import com.preichert.cinemerick.feature.settings.presentation.resources.key_replace
import com.preichert.cinemerick.feature.settings.presentation.resources.key_in_use
import com.preichert.cinemerick.feature.settings.presentation.resources.key_use_saved
import com.preichert.cinemerick.feature.settings.presentation.resources.key_saved
import com.preichert.cinemerick.feature.settings.presentation.resources.key_show
import com.preichert.cinemerick.feature.settings.presentation.resources.key_storage
import com.preichert.cinemerick.feature.settings.presentation.resources.key_title
import com.preichert.cinemerick.feature.settings.presentation.resources.key_where
import com.preichert.cinemerick.feature.settings.presentation.resources.settings_back
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AssistantKeyRoot(
    onNavigateBack: () -> Unit,
    viewModel: AssistantKeyViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            AssistantKeyEvent.Saved -> onNavigateBack()
        }
    }
    AssistantKeyScreen(state = state, onAction = viewModel::onAction, onNavigateBack = onNavigateBack)
}

/**
 * The API key screen of docs/design/voice-search (Settings › Assistente): the provider, the key with show/hide and
 * where to find it, the model, and one primary action that checks the key before saving it.
 */
@Composable
fun AssistantKeyScreen(
    state: AssistantKeyState,
    onAction: (AssistantKeyAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val colors = CinemerickTheme.colors
    Scaffold(contentWindowInsets = WindowInsets(0), containerColor = colors.background) { _ ->
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            SettingsHeader(
                title = stringResource(Res.string.key_title),
                backLabel = stringResource(Res.string.settings_back),
                onBack = onNavigateBack,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 640.dp)
                    .padding(horizontal = Spacing.space4)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(Spacing.space4),
            ) {
                ProviderPicker(
                    selected = state.provider,
                    onSelect = { onAction(AssistantKeyAction.OnProviderSelect(it)) },
                )
                state.savedKey?.let { saved ->
                    SavedKeyCard(
                        saved = saved,
                        inUse = saved.provider == state.activeProvider,
                        onUse = { onAction(AssistantKeyAction.OnUseSavedKeyClick) },
                    )
                }
                KeyField(state, onAction)
                ModelField(state.model) { onAction(AssistantKeyAction.OnModelChange(it)) }
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                    Icon(CinemerickIcons.Lock, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text(stringResource(Res.string.key_storage), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                CinemerickPrimaryButton(
                    text = stringResource(Res.string.key_save),
                    onClick = { onAction(AssistantKeyAction.OnSaveClick) },
                    loading = state.isVerifying,
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.space6),
                )
            }
        }
    }
}

/** The six providers as single-choice chips, each with its own mark. A saved key is shown on the cards, not here. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProviderPicker(selected: CloudProvider, onSelect: (CloudProvider) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
        Text(stringResource(Res.string.key_provider), style = MaterialTheme.typography.labelLarge, color = CinemerickTheme.colors.onSurface)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2), verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            CloudProvider.entries.forEach { provider ->
                CinemerickFilterChip(
                    label = provider.displayName,
                    selected = provider == selected,
                    onClick = { onSelect(provider) },
                    leadingContent = { ProviderMark(provider, size = 18.dp) },
                )
            }
        }
    }
}

/** The key already saved for the chosen provider: one tap makes it the active one, without pasting it again. */
@Composable
private fun SavedKeyCard(saved: SavedKey, inUse: Boolean, onUse: () -> Unit) {
    val colors = CinemerickTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.surfaceContainer)
            .padding(start = Spacing.space4, end = Spacing.space2, top = Spacing.space2, bottom = Spacing.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        Icon(CinemerickIcons.Key, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Text(
            text = stringResource(Res.string.key_saved, saved.lastFour, saved.model),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (inUse) {
            Text(
                text = stringResource(Res.string.key_in_use),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onPrimaryContainer,
                modifier = Modifier
                    .padding(end = Spacing.space2)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(colors.primaryContainer)
                    .padding(horizontal = Spacing.space2, vertical = Spacing.space1),
            )
        } else {
            CinemerickTonalButton(text = stringResource(Res.string.key_use_saved), onClick = onUse)
        }
    }
}

@Composable
private fun KeyField(state: AssistantKeyState, onAction: (AssistantKeyAction) -> Unit) {
    val colors = CinemerickTheme.colors
    val uriHandler = LocalUriHandler.current
    val provider = state.provider.displayName
    val label = stringResource(Res.string.key_label, provider)
    val errorText = when (state.error) {
        KeyErrorUi.EMPTY -> stringResource(Res.string.key_error_empty, provider)
        KeyErrorUi.INVALID -> stringResource(Res.string.key_error_invalid, provider)
        KeyErrorUi.OFFLINE -> stringResource(Res.string.key_error_offline)
        KeyErrorUi.RATE_LIMITED -> stringResource(Res.string.key_error_rate_limited, provider)
        KeyErrorUi.OTHER -> stringResource(Res.string.key_error_other)
        null -> null
    }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
        Text(
            text = if (state.savedKey != null) stringResource(Res.string.key_replace) else label,
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurface,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(colors.surfaceContainerHigh)
                .then(if (errorText != null) Modifier.border(2.dp, colors.errorText, MaterialTheme.shapes.medium) else Modifier)
                .padding(start = Spacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = state.key,
                onValueChange = { onAction(AssistantKeyAction.OnKeyChange(it)) },
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = label
                        if (errorText != null) error(errorText)
                    },
                enabled = !state.isVerifying,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                visualTransformation = if (state.isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAction(AssistantKeyAction.OnSaveClick) }),
                decorationBox = { inner ->
                    if (state.key.isEmpty() && state.provider.keyPrefix.isNotEmpty()) {
                        Text("${state.provider.keyPrefix}…", style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                    }
                    inner()
                },
            )
            CinemerickIconButton(
                icon = if (state.isKeyVisible) CinemerickIcons.EyeOff else CinemerickIcons.Eye,
                contentDescription = stringResource(if (state.isKeyVisible) Res.string.key_hide else Res.string.key_show),
                onClick = { onAction(AssistantKeyAction.OnKeyVisibilityToggle) },
                container = Color.Transparent,
                tint = colors.onSurfaceVariant,
            )
        }
        if (errorText != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space1), verticalAlignment = Alignment.CenterVertically) {
                Icon(CinemerickIcons.Alert, contentDescription = null, tint = colors.errorText, modifier = Modifier.size(16.dp))
                Text(errorText, style = MaterialTheme.typography.bodySmall, color = colors.errorText)
            }
        } else if (state.provider.keyPrefix.isNotEmpty()) {
            Text(stringResource(Res.string.key_help_prefix, state.provider.keyPrefix), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        Text(
            text = stringResource(Res.string.key_where),
            style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline),
            color = colors.primary,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button) { uriHandler.openUri(state.provider.keyPageUrl) }
                .padding(vertical = Spacing.space3),
        )
    }
}

@Composable
private fun ModelField(model: String, onChange: (String) -> Unit) {
    val colors = CinemerickTheme.colors
    val label = stringResource(Res.string.key_model)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
        BasicTextField(
            value = model,
            onValueChange = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(colors.surfaceContainerHigh)
                .padding(horizontal = Spacing.space4, vertical = Spacing.space4)
                .semantics { contentDescription = label },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
        )
        Text(stringResource(Res.string.key_model_help), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

@Preview(name = "Key, empty")
@Composable
private fun AssistantKeyEmptyPreview() = LightThemePreview {
    AssistantKeyScreen(AssistantKeyState(provider = CloudProvider.ANTHROPIC, model = CloudProvider.ANTHROPIC.defaultModel), {}, {})
}

@Preview(name = "Key, verifying")
@Composable
private fun AssistantKeyVerifyingPreview() = LightThemePreview {
    AssistantKeyScreen(AssistantKeyState(provider = CloudProvider.ANTHROPIC, key = "sk-ant-example-key", isVerifying = true), {}, {})
}

@Preview(name = "Key, rejected, dark")
@Composable
private fun AssistantKeyRejectedDarkPreview() = DarkThemePreview {
    AssistantKeyScreen(
        AssistantKeyState(provider = CloudProvider.ANTHROPIC, key = "sk-ant-example", isKeyVisible = true, error = KeyErrorUi.INVALID),
        {}, {},
    )
}

@Preview(name = "Key, saved key to switch to")
@Composable
private fun AssistantKeySavedPreview() = LightThemePreview {
    AssistantKeyScreen(
        AssistantKeyState(
            provider = CloudProvider.GEMINI,
            model = "gemini-2.5-flash",
            savedKeys = mapOf(
                CloudProvider.GEMINI to SavedKey(CloudProvider.GEMINI, "x7Qa", "gemini-2.5-flash"),
                CloudProvider.OPENAI to SavedKey(CloudProvider.OPENAI, "9fK2", "gpt-5-mini"),
            ),
            activeProvider = CloudProvider.OPENAI,
        ),
        {}, {},
    )
}

@Preview(name = "Key, Mistral (no prefix), offline")
@Composable
private fun AssistantKeyOfflinePreview() = LightThemePreview {
    AssistantKeyScreen(AssistantKeyState(provider = CloudProvider.MISTRAL, model = "mistral-small-latest", key = "abc", error = KeyErrorUi.OFFLINE), {}, {})
}
