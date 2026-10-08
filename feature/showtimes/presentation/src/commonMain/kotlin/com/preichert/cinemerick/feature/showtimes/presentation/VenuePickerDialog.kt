package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.DevicePreviews
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.designsystem.bounceClickable
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.cancel
import com.preichert.cinemerick.feature.showtimes.presentation.resources.done
import com.preichert.cinemerick.feature.showtimes.presentation.resources.no_venues_found
import com.preichert.cinemerick.feature.showtimes.presentation.resources.retry
import com.preichert.cinemerick.feature.showtimes.presentation.resources.search_venue
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun VenuePickerDialog(
    picker: VenuePickerUi,
    selectedVenues: Set<Venue>,
    onDismiss: () -> Unit,
    onSearchChange: (String) -> Unit,
    onVenueToggle: (Venue) -> Unit,
    onReload: () -> Unit = {}
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var localQuery by remember { mutableStateOf("") }
    var localSelected by remember { mutableStateOf(selectedVenues) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(Spacing.lg),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = stringResource(Res.string.search_venue),
                    style = MaterialTheme.typography.headlineSmall
                )
                OutlinedTextField(
                    value = localQuery,
                    onValueChange = { localQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(Res.string.search_venue)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                when (val catalog = picker.catalog) {
                    is VenueCatalogUi.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is VenueCatalogUi.Loaded -> {
                        val filtered = remember(catalog.venues, localQuery) {
                            filteredVenues(catalog.venues, localQuery)
                        }
                        if (filtered.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.no_venues_found),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                items(filtered, key = { it.key }) { venue ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem()
                                            .bounceClickable {
                                                localSelected = if (venue in localSelected) {
                                                    localSelected - venue
                                                } else {
                                                    localSelected + venue
                                                }
                                            }
                                            .padding(Spacing.sm),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                    ) {
                                        Checkbox(
                                            checked = venue in localSelected,
                                            onCheckedChange = {
                                                localSelected = if (venue in localSelected) {
                                                    localSelected - venue
                                                } else {
                                                    localSelected + venue
                                                }
                                            }
                                        )
                                        Column {
                                            Text(venue.name)
                                            venue.region?.let {
                                                Text(
                                                    it,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is VenueCatalogUi.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Text(
                                text = catalog.message.asString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            OutlinedButton(onClick = onReload) {
                                Text(stringResource(Res.string.retry))
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.cancel))
                    }
                    Button(onClick = {
                        onSearchChange(localQuery)
                        val venuesToAdd = localSelected - selectedVenues
                        val venuesToRemove = selectedVenues - localSelected
                        venuesToAdd.forEach { onVenueToggle(it) }
                        venuesToRemove.forEach { onVenueToggle(it) }
                        onDismiss()
                    }) {
                        Text(stringResource(Res.string.done))
                    }
                }
            }
        }
    }
}

// Previews - Light theme

@Preview
@Composable
private fun VenuePickerDialogLoadingPreviewLight() {
    LightThemePreview {
        VenuePickerDialog(picker = PreviewData.venueCatalogLoading(), selectedVenues = setOf(PreviewData.sileaVenue), onDismiss = {}, onSearchChange = {}, onVenueToggle = {})
    }
}

@Preview
@Composable
private fun VenuePickerDialogLoadedPreviewLight() {
    LightThemePreview {
        VenuePickerDialog(picker = PreviewData.venueCatalogLoaded(), selectedVenues = setOf(PreviewData.sileaVenue), onDismiss = {}, onSearchChange = {}, onVenueToggle = {})
    }
}

// Previews - Dark theme

@Preview
@Composable
private fun VenuePickerDialogLoadingPreviewDark() {
    DarkThemePreview {
        VenuePickerDialog(picker = PreviewData.venueCatalogLoading(), selectedVenues = setOf(PreviewData.sileaVenue), onDismiss = {}, onSearchChange = {}, onVenueToggle = {})
    }
}

@Preview
@Composable
private fun VenuePickerDialogLoadedPreviewDark() {
    DarkThemePreview {
        VenuePickerDialog(picker = PreviewData.venueCatalogLoaded(), selectedVenues = setOf(PreviewData.sileaVenue), onDismiss = {}, onSearchChange = {}, onVenueToggle = {})
    }
}
