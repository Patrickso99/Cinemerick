package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.preichert.cinemerick.core.designsystem.CinemerickBottomSheet
import com.preichert.cinemerick.core.designsystem.CinemerickIconButton
import com.preichert.cinemerick.core.designsystem.CinemerickIcons
import com.preichert.cinemerick.core.designsystem.CinemerickPrimaryButton
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.LocalAnimationsEnabled
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.designsystem.rememberSyncedTextFieldState
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.SystemAiAvailability
import com.preichert.cinemerick.core.domain.assistant.SystemAiState
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_all_films
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_all_formats
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_any_time
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_cancel
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_choose_other
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_close
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_confirm_question
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_confirm_yes
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_edit
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_edit_text
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_engine_cloud
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_engine_device
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_error_answer
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_error_answer_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_error_engine
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_error_engine_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_error_key
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_error_key_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_error_offline
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_error_offline_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_error_quota
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_error_quota_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_example_1
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_example_2
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_group_cinemas
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_group_days
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_group_films
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_group_format
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_hidden_formats
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_hint_examples
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_hint_stop
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_ignore_venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_interpret
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_language_hint
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_listening
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_mic_allow
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_mic_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_mic_denied_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_mic_denied_heading
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_mic_denied_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_mic_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_mic_type_instead
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_no_days
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_not_found
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_not_understood_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_not_understood_heading
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_not_understood_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_nothing_heard_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_nothing_heard_heading
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_nothing_heard_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_only_formats
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_open_settings
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_reading
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_reading_hint
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_remove
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_request_label
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_request_placeholder
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_respeak
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_retry
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_search
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_setup_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_setup_key
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_setup_key_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_setup_later
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_setup_system_download_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_setup_system_download_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_setup_system_downloading_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_setup_system_downloading_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_setup_system_enable_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_setup_system_enable_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_setup_system_preparing_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_setup_system_preparing_body
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_setup_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_speak
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_stop
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_type
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_typing_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_unchanged_days
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_unchanged_films
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_unchanged_formats
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_unchanged_venues
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_understood
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_update_key
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_venues_count
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_venues_count_one
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_venues_to_confirm
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_which_venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_without_formats
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_you_said
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The voice sheet of docs/design/voice-search (direction A · Scheda): one bottom sheet from listening to the review.
 * The microphone and the settings are outside the view model, so the screen passes them in.
 */
@Composable
internal fun VoiceSheet(
    voice: VoiceUi,
    engine: EngineUi?,
    systemAi: SystemAiState,
    filters: FilterSummaryUi,
    onAction: (ShowtimesAction) -> Unit,
    onAllowMic: () -> Unit,
    onOpenMicSettings: () -> Unit,
    onOpenAssistantSettings: () -> Unit,
) {
    val dismiss = { onAction(ShowtimesAction.OnVoiceDismiss) }
    CinemerickBottomSheet(onDismissRequest = dismiss) {
        VoiceSheetContent(voice, engine, systemAi, filters, onAction, onAllowMic, onOpenMicSettings, onOpenAssistantSettings)
    }
}

@Composable
private fun VoiceSheetContent(
    voice: VoiceUi,
    engine: EngineUi?,
    systemAi: SystemAiState,
    filters: FilterSummaryUi,
    onAction: (ShowtimesAction) -> Unit,
    onAllowMic: () -> Unit,
    onOpenMicSettings: () -> Unit,
    onOpenAssistantSettings: () -> Unit,
) {
    val close = { onAction(ShowtimesAction.OnVoiceDismiss) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        SheetHeader(title = stringResource(voice.titleRes()), engine = engine.takeIf { voice !is VoiceUi.Setup }, onClose = close)
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.space4, vertical = Spacing.space2),
            verticalArrangement = Arrangement.spacedBy(Spacing.space4),
        ) {
            when (voice) {
                VoiceUi.Setup -> SetupBody(systemAi, onSystemAiDownload = { onAction(ShowtimesAction.OnSystemAiDownload) }, onOpenAssistantSettings)
                VoiceUi.MicRationale -> MicRationaleBody()
                is VoiceUi.MicDenied -> MicDeniedBody(voice.text, onOpenMicSettings) { onAction(ShowtimesAction.OnVoiceTextChange(it)) }
                is VoiceUi.Listening -> ListeningBody(voice) { onAction(ShowtimesAction.OnVoiceStopListening) }
                VoiceUi.NothingHeard -> NothingHeardBody { onAction(ShowtimesAction.OnVoiceListen) }
                is VoiceUi.Typing -> RequestField(voice.text, autoFocus = true) { onAction(ShowtimesAction.OnVoiceTextChange(it)) }
                is VoiceUi.Interpreting -> InterpretingBody(voice.text)
                is VoiceUi.Review -> ReviewBody(voice, filters, onAction)
                is VoiceUi.NotUnderstood -> {
                    Transcript(voice.text, onAction)
                    Notice(CinemerickIcons.Question, stringResource(Res.string.voice_not_understood_heading), stringResource(Res.string.voice_not_understood_body), NoticeTone.INFO)
                }
                is VoiceUi.Failed -> {
                    Transcript(voice.text, onAction)
                    FailureNotice(voice.error, engine?.name.orEmpty())
                }
            }
        }
        SheetActions(voice, onAction, onAllowMic, onOpenAssistantSettings, close)
    }
}

private fun VoiceUi.titleRes(): StringResource = when (this) {
    VoiceUi.Setup -> Res.string.voice_setup_title
    VoiceUi.MicRationale -> Res.string.voice_mic_title
    is VoiceUi.MicDenied -> Res.string.voice_mic_denied_title
    is VoiceUi.Listening -> Res.string.voice_listening
    VoiceUi.NothingHeard -> Res.string.voice_nothing_heard_title
    is VoiceUi.Typing -> Res.string.voice_typing_title
    is VoiceUi.Interpreting -> Res.string.voice_reading
    is VoiceUi.Review, is VoiceUi.Failed -> Res.string.voice_understood
    is VoiceUi.NotUnderstood -> Res.string.voice_not_understood_title
}

@Composable
private fun SheetHeader(title: String, engine: EngineUi?, onClose: () -> Unit) {
    val colors = CinemerickTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = Spacing.space4, end = Spacing.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        if (engine != null) EngineBadge(engine)
        CinemerickIconButton(
            icon = CinemerickIcons.Close,
            contentDescription = stringResource(Res.string.voice_close),
            onClick = onClose,
            container = Color.Transparent,
            tint = colors.onSurfaceVariant,
        )
    }
}

/** Where the request goes: "Cloud · Anthropic" makes it visible that the text leaves the phone; "On device" that it does not. */
@Composable
private fun EngineBadge(engine: EngineUi) {
    val colors = CinemerickTheme.colors
    val label = if (engine.onDevice) stringResource(Res.string.voice_engine_device) else stringResource(Res.string.voice_engine_cloud, engine.name)
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.surface)
            .border(1.dp, colors.outlineVariant, CircleShape)
            .padding(horizontal = Spacing.space2, vertical = Spacing.space1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.space1),
    ) {
        Icon(if (engine.onDevice) CinemerickIcons.Lock else CinemerickIcons.Cloud, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (engine.onDevice) colors.onSurfaceVariant else colors.onSurface, maxLines = 1)
    }
}

// ---------- Listening ----------

@Composable
private fun ListeningBody(voice: VoiceUi.Listening, onStop: () -> Unit) {
    val colors = CinemerickTheme.colors
    ListeningOrb(level = voice.level, active = true, icon = CinemerickIcons.Assistant, contentDescription = stringResource(Res.string.voice_stop), onClick = onStop)
    if (voice.transcript.isBlank()) {
        Text(stringResource(Res.string.voice_hint_examples), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        Example(stringResource(Res.string.voice_example_1))
        Example(stringResource(Res.string.voice_example_2))
    } else {
        Text(
            text = voice.transcript,
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        Text(stringResource(Res.string.voice_hint_stop), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun Example(text: String) {
    val colors = CinemerickTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surface)
            .padding(Spacing.space3),
    )
}

/**
 * The listening circle: a filled center the user taps when done, with two rings that grow with the voice level.
 * With animations off the rings stay still. Off ([active] false) is the microphone that cannot listen.
 */
@Composable
private fun ListeningOrb(level: Float, active: Boolean, icon: Painter, contentDescription: String?, onClick: (() -> Unit)?) {
    val colors = CinemerickTheme.colors
    val animate = LocalAnimationsEnabled.current
    val scale by animateFloatAsState(if (animate && active) 1f + level * 0.12f else 1f, label = "orbLevel")
    val ring = if (active) colors.primary else colors.outlineVariant
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(168.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(168.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(CircleShape)
                    .background(ring.copy(alpha = if (active) 0.10f else 0.4f))
            )
            Box(
                Modifier
                    .size(124.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(CircleShape)
                    .background(ring.copy(alpha = if (active) 0.18f else 0.5f))
            )
            if (onClick != null) {
                FilledIconButton(
                    onClick = onClick,
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
                ) {
                    Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(32.dp))
                }
            } else {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(colors.surface).border(1.dp, colors.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
private fun NothingHeardBody(onRetry: () -> Unit) {
    ListeningOrb(level = 0f, active = false, icon = CinemerickIcons.Mic, contentDescription = stringResource(Res.string.voice_retry), onClick = onRetry)
    Notice(CinemerickIcons.MicOff, stringResource(Res.string.voice_nothing_heard_heading), stringResource(Res.string.voice_nothing_heard_body), NoticeTone.INFO)
}

@Composable
private fun MicRationaleBody() {
    ListeningOrb(level = 0f, active = false, icon = CinemerickIcons.Mic, contentDescription = null, onClick = null)
    Text(stringResource(Res.string.voice_mic_body), style = MaterialTheme.typography.bodyLarge, color = CinemerickTheme.colors.onSurface)
}

@Composable
private fun MicDeniedBody(text: String, onOpenSettings: () -> Unit, onTextChange: (String) -> Unit) {
    Notice(
        icon = CinemerickIcons.MicOff,
        title = stringResource(Res.string.voice_mic_denied_heading),
        body = stringResource(Res.string.voice_mic_denied_body),
        tone = NoticeTone.WARNING,
        actionLabel = stringResource(Res.string.voice_open_settings),
        onAction = onOpenSettings,
    )
    RequestField(text, onTextChange = onTextChange)
}

@Composable
private fun SetupBody(systemAi: SystemAiState, onSystemAiDownload: () -> Unit, onOpenAssistantSettings: () -> Unit) {
    val colors = CinemerickTheme.colors
    Text(stringResource(Res.string.voice_setup_body), style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
    // The AI of the system comes first: it is free and nothing leaves the phone.
    val name = systemAi.name.orEmpty()
    when (systemAi.availability) {
        SystemAiAvailability.DOWNLOADABLE -> SetupChoice(
            icon = CinemerickIcons.Assistant,
            title = stringResource(Res.string.voice_setup_system_download_title, name),
            body = stringResource(Res.string.voice_setup_system_download_body),
            onClick = onSystemAiDownload,
        )
        SystemAiAvailability.DOWNLOADING -> Notice(
            icon = CinemerickIcons.Assistant,
            title = stringResource(Res.string.voice_setup_system_downloading_title, name),
            body = stringResource(Res.string.voice_setup_system_downloading_body, (systemAi.downloadedBytes ?: 0L).toMegabytesText()),
            tone = NoticeTone.INFO,
        )
        SystemAiAvailability.NEEDS_ENABLING -> Notice(
            icon = CinemerickIcons.Alert,
            title = stringResource(Res.string.voice_setup_system_enable_title, name),
            body = stringResource(Res.string.voice_setup_system_enable_body),
            tone = NoticeTone.WARNING,
        )
        SystemAiAvailability.NOT_READY -> Notice(
            icon = CinemerickIcons.Question,
            title = stringResource(Res.string.voice_setup_system_preparing_title, name),
            body = stringResource(Res.string.voice_setup_system_preparing_body),
            tone = NoticeTone.INFO,
        )
        SystemAiAvailability.UNSUPPORTED, SystemAiAvailability.AVAILABLE -> Unit
    }
    SetupChoice(
        icon = CinemerickIcons.Key,
        title = stringResource(Res.string.voice_setup_key),
        body = stringResource(Res.string.voice_setup_key_body),
        onClick = onOpenAssistantSettings,
    )
}

/** One way to get the assistant going: an icon, what it is, and a chevron. The whole card is the touch target. */
@Composable
private fun SetupChoice(icon: Painter, title: String, body: String, onClick: () -> Unit) {
    val colors = CinemerickTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(Spacing.space4),
        horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(MaterialTheme.shapes.small).background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        Icon(CinemerickIcons.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
    }
}

/** "Mar 15 dic": the weekday, the day and the month, so a date months away is not read as one of this month. */
@Composable
private fun LocalDate.reviewLabel(): String =
    "${stringResource(dayOfWeek.shortNameResource())} $day ${month.displayName().take(3).lowercase()}"

private fun Long.toMegabytesText(): String = "${this / 1_000_000L} MB"

// ---------- Text ----------

/** The request as an editable field: to fix a word, or to type the whole request where speaking is not possible. */
@Composable
private fun RequestField(text: String, autoFocus: Boolean = false, onTextChange: (String) -> Unit) {
    val colors = CinemerickTheme.colors
    // Typing was chosen on purpose: the keyboard opens at once. After a refused microphone it waits for a tap.
    val focusRequester = remember { FocusRequester() }
    if (autoFocus) LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val field = rememberSyncedTextFieldState(text)
    val label = stringResource(Res.string.voice_request_label)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
        BasicTextField(
            value = field.value,
            onValueChange = { if (field.onEdit(it)) onTextChange(it.text) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(colors.surface)
                .border(2.dp, colors.primary, MaterialTheme.shapes.medium)
                .padding(Spacing.space3)
                .focusRequester(focusRequester)
                .semantics { contentDescription = label },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            decorationBox = { inner ->
                if (field.value.text.isEmpty()) {
                    Text(stringResource(Res.string.voice_request_placeholder), style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                }
                inner()
            },
        )
        Text(stringResource(Res.string.voice_language_hint), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

/** What was heard or typed, two lines at most, with Edit and Speak again. */
@Composable
private fun Transcript(text: String, onAction: (ShowtimesAction) -> Unit) {
    val colors = CinemerickTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surface)
            .border(1.dp, colors.outlineVariant, MaterialTheme.shapes.medium)
            .padding(start = Spacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "«$text»",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(vertical = Spacing.space3),
        )
        CinemerickIconButton(
            icon = CinemerickIcons.Edit,
            contentDescription = stringResource(Res.string.voice_edit_text),
            onClick = { onAction(ShowtimesAction.OnVoiceType) },
            container = Color.Transparent,
            tint = colors.onSurfaceVariant,
        )
        CinemerickIconButton(
            icon = CinemerickIcons.Assistant,
            contentDescription = stringResource(Res.string.voice_respeak),
            onClick = { onAction(ShowtimesAction.OnVoiceListen) },
            container = Color.Transparent,
            tint = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun InterpretingBody(text: String) {
    val colors = CinemerickTheme.colors
    Text("«$text»", style = MaterialTheme.typography.bodyMedium, color = colors.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.space3)) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colors.primary, strokeWidth = 2.dp)
        Text(stringResource(Res.string.voice_reading_hint), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
    }
}

// ---------- Review ----------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewBody(voice: VoiceUi.Review, filters: FilterSummaryUi, onAction: (ShowtimesAction) -> Unit) {
    val review = voice.review
    Transcript(voice.text, onAction)

    val venues = review.venues
    if (review.venuesMentioned || review.questions.isNotEmpty() || review.notFound.isNotEmpty()) {
        val count = venues.orEmpty().size
        val countLabel = if (count == 1) stringResource(Res.string.voice_venues_count_one) else stringResource(Res.string.voice_venues_count, count)
        val aside = if (review.questions.isEmpty()) countLabel else stringResource(Res.string.voice_venues_to_confirm, countLabel, review.questions.size)
        ReviewGroup(stringResource(Res.string.voice_group_cinemas), aside) {
            if (!venues.isNullOrEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2), verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                    venues.forEach { venue ->
                        ValueChip(
                            label = venue.label(),
                            dot = venue.chain.badgeColors().first,
                            onRemove = { onAction(ShowtimesAction.OnVoiceVenueRemove(venue)) },
                        )
                    }
                }
            }
            review.questions.forEach { question ->
                VenueQuestion(
                    question = question,
                    onConfirm = { onAction(ShowtimesAction.OnVoiceVenueConfirm(question.spoken, it)) },
                    onShowCandidates = { onAction(ShowtimesAction.OnVoiceVenueCandidates(question.spoken)) },
                    onIgnore = { onAction(ShowtimesAction.OnVoiceVenueIgnore(question.spoken)) },
                )
            }
            review.notFound.forEach { spoken ->
                Notice(CinemerickIcons.Alert, title = null, body = stringResource(Res.string.voice_not_found, spoken), tone = NoticeTone.NEUTRAL)
            }
        }
    } else {
        val current = filters.chains.flatMap { it.venues }.joinToString(", ").ifEmpty { "—" }
        Unchanged(Res.string.voice_group_cinemas, stringResource(Res.string.voice_unchanged_venues, current))
    }

    val days = review.days
    if (days != null) {
        ReviewGroup(stringResource(Res.string.voice_group_days)) {
            DayRows(days) { onAction(ShowtimesAction.OnVoiceDayRemove(it)) }
        }
    } else {
        val current = filters.days.map { it.date.reviewLabel() }
            .joinToString(", ").ifEmpty { stringResource(Res.string.voice_no_days) }
        Unchanged(Res.string.voice_group_days, stringResource(Res.string.voice_unchanged_days, current))
    }

    val formats = review.formats
    if (formats != null) {
        ReviewGroup(stringResource(Res.string.voice_group_format)) {
            ValueChip(label = formats.label(), icon = CinemerickIcons.Sliders, onRemove = { onAction(ShowtimesAction.OnVoiceFormatsRemove) })
            val hidden = formats.hiddenTags.sorted()
            if (formats.only.isNotEmpty() && hidden.isNotEmpty()) {
                Text(
                    stringResource(Res.string.voice_hidden_formats, hidden.joinToString(", ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = CinemerickTheme.colors.onSurfaceVariant,
                )
            }
        }
    } else {
        val current = if (filters.hiddenFormats.isEmpty()) stringResource(Res.string.voice_all_formats)
        else stringResource(Res.string.voice_without_formats, filters.hiddenFormats.joinToString(", "))
        Unchanged(Res.string.voice_group_format, stringResource(Res.string.voice_unchanged_formats, current))
    }

    val films = review.films
    if (films != null) {
        ReviewGroup(stringResource(Res.string.voice_group_films)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2), verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                films.forEach { title -> ValueChip(label = title, icon = CinemerickIcons.Film, onRemove = { onAction(ShowtimesAction.OnVoiceFilmRemove(title)) }) }
            }
        }
    } else {
        val current = filters.filmQueries.joinToString(", ").ifEmpty { stringResource(Res.string.voice_all_films) }
        Unchanged(Res.string.voice_group_films, stringResource(Res.string.voice_unchanged_films, current))
    }
}

@Composable
private fun FormatChoiceUi.label(): String = when {
    only.isNotEmpty() -> stringResource(Res.string.voice_only_formats, only.joinToString(", "))
    else -> stringResource(Res.string.voice_without_formats, excluded.joinToString(", "))
}

private fun Venue.label(): String = "${chain.displayName()} · ${name.removePrefix(chain.displayName()).trim().ifEmpty { name }}"

@Composable
private fun ReviewGroup(label: String, aside: String? = null, content: @Composable () -> Unit) {
    val colors = CinemerickTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f).semantics { heading() })
            if (aside != null) Text(aside, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
        content()
    }
}

/** A group the request did not mention: it stays as it is, and the sheet says so with the current value. */
@Composable
private fun Unchanged(label: StringResource, text: String) {
    ReviewGroup(stringResource(label)) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = CinemerickTheme.colors.onSurfaceVariant)
    }
}

/** A value the request sets, with a 48dp remove button. The chain dot is decorative: the name is always written. */
@Composable
private fun ValueChip(label: String, onRemove: () -> Unit, dot: Color? = null, icon: Painter? = null) {
    val colors = CinemerickTheme.colors
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(colors.surface)
            .border(1.dp, colors.outlineVariant, MaterialTheme.shapes.small)
            .padding(start = Spacing.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
    ) {
        if (dot != null) Box(Modifier.size(10.dp).clip(CircleShape).background(dot).border(1.dp, colors.chainEdge, CircleShape))
        if (icon != null) Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
        CinemerickIconButton(
            icon = CinemerickIcons.Close,
            contentDescription = stringResource(Res.string.voice_remove, label),
            onClick = onRemove,
            container = Color.Transparent,
            tint = colors.onSurfaceVariant,
        )
    }
}

/** One row per day with its window; a day can be removed from the request. */
@Composable
private fun DayRows(days: List<DayUi>, onRemove: (LocalDate) -> Unit) {
    val colors = CinemerickTheme.colors
    val anyTime = stringResource(Res.string.voice_any_time)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surface)
            .border(1.dp, colors.outlineVariant, MaterialTheme.shapes.medium),
    ) {
        days.forEach { day ->
            val label = day.date.reviewLabel()
            val window = listOf(day.minTime, day.maxTime).let { (from, to) ->
                if (from.isBlank() && to.isBlank()) anyTime else "${from.ifBlank { "00:00" }}–${to.ifBlank { "24:00" }}"
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = Spacing.space3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
            ) {
                Icon(CinemerickIcons.Calendar, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Text(label, style = MaterialTheme.typography.labelLarge, color = colors.onSurface, modifier = Modifier.weight(1f))
                Text(window, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                CinemerickIconButton(
                    icon = CinemerickIcons.Close,
                    contentDescription = stringResource(Res.string.voice_remove, "$label $window"),
                    onClick = { onRemove(day.date) },
                    container = Color.Transparent,
                    tint = colors.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * A cinema the assistant is not sure about: the likely one with Yes, or up to three alternatives to pick from and
 * Skip. Warning colors with an icon, so it does not read as an error.
 */
@Composable
private fun VenueQuestion(
    question: VenueQuestionUi,
    onConfirm: (Venue) -> Unit,
    onShowCandidates: () -> Unit,
    onIgnore: () -> Unit,
) {
    val colors = CinemerickTheme.colors
    val content = colors.onWarningContainer
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.warningContainer)
            .padding(Spacing.space3),
        verticalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Icon(CinemerickIcons.Question, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
            Column {
                val title = if (question.showCandidates) stringResource(Res.string.voice_which_venue)
                else stringResource(Res.string.voice_confirm_question, question.best.label())
                Text(title, style = MaterialTheme.typography.titleSmall, color = content)
                Text(stringResource(Res.string.voice_you_said, question.spoken), style = MaterialTheme.typography.bodyMedium, color = content)
            }
        }
        if (question.showCandidates) {
            Column(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).background(colors.surface)) {
                question.candidates.forEach { venue ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .selectable(selected = false, role = Role.RadioButton, onClick = { onConfirm(venue) })
                            .padding(horizontal = Spacing.space3),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
                    ) {
                        RadioButton(selected = false, onClick = null, colors = RadioButtonDefaults.colors(unselectedColor = colors.onSurfaceVariant))
                        Box(Modifier.size(10.dp).clip(CircleShape).background(venue.chain.badgeColors().first))
                        Text(venue.label(), style = MaterialTheme.typography.labelLarge, color = colors.onSurface, modifier = Modifier.weight(1f))
                    }
                }
            }
            TextButton(onClick = onIgnore) { Text(stringResource(Res.string.voice_ignore_venue), color = content) }
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2), verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                CinemerickPrimaryButton(
                    text = stringResource(Res.string.voice_confirm_yes, question.best.name),
                    onClick = { onConfirm(question.best) },
                    height = 44.dp,
                )
                TextButton(onClick = onShowCandidates, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(Res.string.voice_choose_other), color = content, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

// ---------- Notices and actions ----------

private enum class NoticeTone { INFO, WARNING, ERROR, NEUTRAL }

/** An ongoing condition inside the sheet (T4): icon, title and what to do, colored by how serious it is. */
@Composable
private fun Notice(
    icon: Painter,
    title: String?,
    body: String,
    tone: NoticeTone,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = CinemerickTheme.colors
    val (container, content) = when (tone) {
        NoticeTone.INFO -> colors.primaryContainer to colors.onPrimaryContainer
        NoticeTone.WARNING -> colors.warningContainer to colors.onWarningContainer
        NoticeTone.ERROR -> colors.error to colors.onError
        NoticeTone.NEUTRAL -> colors.surface to colors.onSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(container)
            .then(if (tone == NoticeTone.NEUTRAL) Modifier.border(1.dp, colors.outlineVariant, MaterialTheme.shapes.medium) else Modifier)
            .padding(Spacing.space3)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1), modifier = Modifier.weight(1f)) {
            if (title != null) Text(title, style = MaterialTheme.typography.titleSmall, color = content)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = content)
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) { Text(actionLabel, color = content, style = MaterialTheme.typography.labelLarge) }
            }
        }
    }
}

@Composable
private fun FailureNotice(error: AssistantError, name: String) {
    when (error) {
        AssistantError.INVALID_KEY -> Notice(CinemerickIcons.Key, stringResource(Res.string.voice_error_key, name), stringResource(Res.string.voice_error_key_body), NoticeTone.ERROR)
        AssistantError.OFFLINE -> Notice(CinemerickIcons.Cloud, stringResource(Res.string.voice_error_offline), stringResource(Res.string.voice_error_offline_body), NoticeTone.WARNING)
        AssistantError.RATE_LIMITED -> Notice(CinemerickIcons.Alert, stringResource(Res.string.voice_error_quota, name), stringResource(Res.string.voice_error_quota_body), NoticeTone.WARNING)
        AssistantError.BAD_ANSWER -> Notice(CinemerickIcons.Alert, stringResource(Res.string.voice_error_answer), stringResource(Res.string.voice_error_answer_body), NoticeTone.ERROR)
        AssistantError.NO_ENGINE, AssistantError.UNAVAILABLE, AssistantError.UNKNOWN ->
            Notice(CinemerickIcons.Alert, stringResource(Res.string.voice_error_engine), stringResource(Res.string.voice_error_engine_body), NoticeTone.ERROR)
    }
}

/** The one primary action of each step at the end, a text button for the way out or the alternative. */
@Composable
private fun SheetActions(
    voice: VoiceUi,
    onAction: (ShowtimesAction) -> Unit,
    onAllowMic: () -> Unit,
    onOpenAssistantSettings: () -> Unit,
    onClose: () -> Unit,
) {
    val colors = CinemerickTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.space4, vertical = Spacing.space3),
        horizontalArrangement = Arrangement.spacedBy(Spacing.space2, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        @Composable
        fun Secondary(label: StringResource, onClick: () -> Unit) {
            TextButton(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(label), color = colors.primary, style = MaterialTheme.typography.labelLarge)
            }
        }

        @Composable
        fun Primary(label: StringResource, onClick: () -> Unit, loading: Boolean = false) {
            CinemerickPrimaryButton(text = stringResource(label), onClick = onClick, loading = loading, modifier = Modifier.weight(1f))
        }

        val type = { onAction(ShowtimesAction.OnVoiceType) }
        val listen = { onAction(ShowtimesAction.OnVoiceListen) }
        val interpret = { onAction(ShowtimesAction.OnVoiceInterpret) }
        when (voice) {
            VoiceUi.Setup -> Secondary(Res.string.voice_setup_later, onClose)
            VoiceUi.MicRationale -> {
                Secondary(Res.string.voice_mic_type_instead, type)
                Primary(Res.string.voice_mic_allow, onAllowMic)
            }
            is VoiceUi.MicDenied -> {
                Secondary(Res.string.voice_cancel, onClose)
                Primary(Res.string.voice_interpret, interpret)
            }
            is VoiceUi.Listening -> Secondary(Res.string.voice_type, type)
            VoiceUi.NothingHeard -> {
                Secondary(Res.string.voice_type, type)
                Primary(Res.string.voice_retry, listen)
            }
            is VoiceUi.Typing -> {
                Secondary(Res.string.voice_speak, listen)
                Primary(Res.string.voice_interpret, interpret)
            }
            is VoiceUi.Interpreting -> {
                Secondary(Res.string.voice_cancel, type)
                Primary(Res.string.voice_search, {}, loading = true)
            }
            is VoiceUi.Review -> {
                Secondary(Res.string.voice_cancel, onClose)
                Primary(Res.string.voice_search, { onAction(ShowtimesAction.OnVoiceSearch) })
            }
            is VoiceUi.NotUnderstood -> {
                Secondary(Res.string.voice_edit, type)
                Primary(Res.string.voice_respeak, listen)
            }
            is VoiceUi.Failed -> if (voice.error == AssistantError.INVALID_KEY) {
                Secondary(Res.string.voice_close, onClose)
                Primary(Res.string.voice_update_key, onOpenAssistantSettings)
            } else {
                Secondary(Res.string.voice_edit, type)
                Primary(Res.string.voice_retry, interpret)
            }
        }
    }
}

// ---------- Previews ----------

private val previewSilea = Venue(Chain.THE_SPACE, "1009", "Silea", "Veneto")
private val previewMarcon = Venue(Chain.UCI, "uci-marcon", "UCI Luxe Marcon", "Veneto")
private val previewFiume = Venue(Chain.NOTORIOUS, "fiumeveneto", "Fiume Veneto", "Friuli")
private val previewFilters = FilterSummaryUi()
private const val PREVIEW_REQUEST = "Vorrei andare a vedere il film Hope in una di queste catene: The Space Silea, Uci Marcon e Notorius Fiume"

private val previewReview = VoiceReviewUi(
    venues = listOf(previewSilea, previewMarcon),
    questions = listOf(VenueQuestionUi("Notorius Fiume", previewFiume, listOf(previewFiume))),
    days = listOf(
        DayUi(LocalDate(2026, 11, 12), isSelected = true, minTime = "17:00", maxTime = "20:00"),
        DayUi(LocalDate(2026, 11, 13), isSelected = true, minTime = "20:00", maxTime = "24:00"),
    ),
    films = listOf("Hope"),
)

@Composable
private fun PreviewSheet(voice: VoiceUi, engine: EngineUi? = EngineUi("Anthropic", onDevice = false), systemAi: SystemAiState = SystemAiState()) {
    VoiceSheetContent(voice, engine, systemAi, previewFilters, {}, {}, {}, {})
}

@Preview(name = "Listening, waiting")
@Composable
private fun VoiceListeningEmptyPreview() = LightThemePreview { PreviewSheet(VoiceUi.Listening()) }

@Preview(name = "Listening, transcript")
@Composable
private fun VoiceListeningPreview() = LightThemePreview { PreviewSheet(VoiceUi.Listening("Vorrei vedere i film a The Space Silea, UCI Marcon", 0.6f)) }

@Preview(name = "Listening dark")
@Composable
private fun VoiceListeningDarkPreview() = DarkThemePreview { PreviewSheet(VoiceUi.Listening("UCI Marcon venerdì dopo", 0.3f)) }

@Preview(name = "Review, to confirm")
@Composable
private fun VoiceReviewPreview() = LightThemePreview { PreviewSheet(VoiceUi.Review(PREVIEW_REQUEST, previewReview)) }

@Preview(name = "Review, candidates, dark")
@Composable
private fun VoiceReviewCandidatesDarkPreview() = DarkThemePreview {
    PreviewSheet(VoiceUi.Review(PREVIEW_REQUEST, previewReview.showCandidates("Notorius Fiume")))
}

@Preview(name = "Review, not found and formats")
@Composable
private fun VoiceReviewNotFoundPreview() = LightThemePreview {
    PreviewSheet(
        VoiceUi.Review(
            "UCI Marcon e il Cinema Astra sabato sera, solo 2D",
            VoiceReviewUi(
                venues = listOf(previewMarcon),
                notFound = listOf("Cinema Astra"),
                formats = FormatChoiceUi(only = listOf("2D"), excluded = emptyList(), hiddenTags = setOf("3D", "IMAX", "4DX")),
            ),
        )
    )
}

@Preview(name = "Typing")
@Composable
private fun VoiceTypingPreview() = LightThemePreview { PreviewSheet(VoiceUi.Typing("UCI Marcon venerdì dopo le 20")) }

@Preview(name = "Interpreting")
@Composable
private fun VoiceInterpretingPreview() = LightThemePreview { PreviewSheet(VoiceUi.Interpreting(PREVIEW_REQUEST)) }

@Preview(name = "Mic rationale")
@Composable
private fun VoiceMicRationalePreview() = LightThemePreview { PreviewSheet(VoiceUi.MicRationale) }

@Preview(name = "Mic denied")
@Composable
private fun VoiceMicDeniedPreview() = LightThemePreview { PreviewSheet(VoiceUi.MicDenied("")) }

@Preview(name = "Nothing heard")
@Composable
private fun VoiceNothingHeardPreview() = LightThemePreview { PreviewSheet(VoiceUi.NothingHeard) }

@Preview(name = "Not understood")
@Composable
private fun VoiceNotUnderstoodPreview() = LightThemePreview { PreviewSheet(VoiceUi.NotUnderstood("Che tempo fa domani a Treviso?")) }

@Preview(name = "Key rejected")
@Composable
private fun VoiceKeyRejectedPreview() = LightThemePreview { PreviewSheet(VoiceUi.Failed(PREVIEW_REQUEST, AssistantError.INVALID_KEY)) }

@Preview(name = "Offline dark")
@Composable
private fun VoiceOfflineDarkPreview() = DarkThemePreview { PreviewSheet(VoiceUi.Failed(PREVIEW_REQUEST, AssistantError.OFFLINE)) }

@Preview(name = "Setup")
@Composable
private fun VoiceSetupPreview() = LightThemePreview { PreviewSheet(VoiceUi.Setup, engine = null) }

@Preview(name = "Setup, Gemini Nano to download")
@Composable
private fun VoiceSetupDownloadablePreview() = LightThemePreview {
    PreviewSheet(VoiceUi.Setup, engine = null, systemAi = SystemAiState("Gemini Nano", SystemAiAvailability.DOWNLOADABLE))
}

@Preview(name = "Setup, downloading, dark")
@Composable
private fun VoiceSetupDownloadingPreview() = DarkThemePreview {
    PreviewSheet(VoiceUi.Setup, engine = null, systemAi = SystemAiState("Gemini Nano", SystemAiAvailability.DOWNLOADING, downloadedBytes = 412_000_000L))
}

@Preview(name = "Setup, Apple Intelligence off")
@Composable
private fun VoiceSetupNeedsEnablingPreview() = LightThemePreview {
    PreviewSheet(VoiceUi.Setup, engine = null, systemAi = SystemAiState("Apple Intelligence", SystemAiAvailability.NEEDS_ENABLING))
}

@Preview(name = "Review, on device")
@Composable
private fun VoiceReviewOnDevicePreview() = LightThemePreview {
    PreviewSheet(VoiceUi.Review(PREVIEW_REQUEST, previewReview), engine = EngineUi("Gemini Nano", onDevice = true))
}
