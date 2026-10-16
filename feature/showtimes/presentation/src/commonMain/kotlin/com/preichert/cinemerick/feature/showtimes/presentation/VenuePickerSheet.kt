package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.preichert.cinemerick.core.designsystem.CinemerickBottomSheet
import com.preichert.cinemerick.core.designsystem.CinemerickPrimaryButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.designsystem.bounceClickable
import com.preichert.cinemerick.core.designsystem.resources.Res as DesignRes
import com.preichert.cinemerick.core.designsystem.resources.ic_check
import com.preichert.cinemerick.core.designsystem.resources.ic_close
import com.preichert.cinemerick.core.designsystem.resources.ic_search
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.choose_venues
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_close
import com.preichert.cinemerick.feature.showtimes.presentation.resources.done_with_count
import com.preichert.cinemerick.feature.showtimes.presentation.resources.no_venues_found
import com.preichert.cinemerick.feature.showtimes.presentation.resources.retry
import com.preichert.cinemerick.feature.showtimes.presentation.resources.search_venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.select_all
import com.preichert.cinemerick.feature.showtimes.presentation.resources.venues_selected_count
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VenuePickerSheet(
    picker: VenuePickerUi,
    selectedVenues: Set<Venue>,
    onDismiss: () -> Unit,
    onSearchChange: (String) -> Unit,
    onVenueToggle: (Venue) -> Unit,
    onReload: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var localQuery by remember { mutableStateOf("") }
    var localSelected by remember { mutableStateOf(selectedVenues) }
    val (badgeColor, badgeContent) = picker.chain.badgeColors()
    val selectedCount = localSelected.count { it.chain == picker.chain }
    val colors = MaterialTheme.colorScheme

    CinemerickBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.space6),
            verticalArrangement = Arrangement.spacedBy(Spacing.space3)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.space6, end = Spacing.space3, top = Spacing.space2),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.space3)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = picker.chain.shortName(),
                        color = badgeContent,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = picker.chain.displayName(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(Res.string.choose_venues),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .background(colors.surfaceContainerHigh, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(DesignRes.drawable.ic_close),
                        contentDescription = stringResource(Res.string.details_close),
                        tint = colors.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.space4)
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceContainerHigh)
                    .padding(horizontal = Spacing.space4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.space3)
            ) {
                Icon(
                    painter = painterResource(DesignRes.drawable.ic_search),
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                BasicTextField(
                    value = localQuery,
                    onValueChange = { localQuery = it },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = colors.onSurface,
                        fontWeight = FontWeight.SemiBold
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    decorationBox = { innerField ->
                        if (localQuery.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.search_venue),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurfaceVariant
                            )
                        }
                        innerField()
                    }
                )
            }

            when (val catalog = picker.catalog) {
                is VenueCatalogUi.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is VenueCatalogUi.Loaded -> {
                    val filtered = remember(catalog.venues, localQuery) {
                        filteredVenues(catalog.venues, localQuery)
                    }
                    if (filtered.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.no_venues_found),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.space6),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.space6),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(Res.string.venues_selected_count, selectedCount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurfaceVariant
                            )
                            TextButton(onClick = { localSelected = localSelected + filtered }) {
                                Text(
                                    text = stringResource(Res.string.select_all),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.primary
                                )
                            }
                        }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                        ) {
                            items(filtered, key = { it.key }) { venue ->
                                VenueRow(
                                    modifier = Modifier.animateItem(),
                                    venue = venue,
                                    selected = venue in localSelected,
                                    onClick = {
                                        localSelected = if (venue in localSelected) {
                                            localSelected - venue
                                        } else {
                                            localSelected + venue
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                is VenueCatalogUi.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.space6),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.space2)
                    ) {
                        Text(
                            text = catalog.message.asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.error
                        )
                        OutlinedButton(onClick = onReload) {
                            Text(stringResource(Res.string.retry))
                        }
                    }
                }
            }

            CinemerickPrimaryButton(
                text = stringResource(Res.string.done_with_count, selectedCount),
                onClick = {
                    onSearchChange(localQuery)
                    val venuesToAdd = localSelected - selectedVenues
                    val venuesToRemove = selectedVenues - localSelected
                    venuesToAdd.forEach { onVenueToggle(it) }
                    venuesToRemove.forEach { onVenueToggle(it) }
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.space4),
            )
        }
    }
}

@Composable
private fun VenueRow(modifier: Modifier, venue: Venue, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.space3)
            .padding(bottom = Spacing.space1)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) colors.primaryContainer else Color.Transparent)
            .bounceClickable { onClick() }
            .defaultMinSize(minHeight = 60.dp)
            .padding(horizontal = Spacing.space3, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.space3)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selected) colors.primary else Color.Transparent)
                .border(2.dp, if (selected) colors.primary else colors.onSurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    painter = painterResource(DesignRes.drawable.ic_check),
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = venue.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            venue.region?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

// Previews - Light theme

@Preview
@Composable
private fun VenuePickerSheetLoadingPreviewLight() {
    LightThemePreview {
        VenuePickerSheet(picker = PreviewData.venueCatalogLoading(), selectedVenues = setOf(PreviewData.sileaVenue), onDismiss = {}, onSearchChange = {}, onVenueToggle = {})
    }
}

@Preview
@Composable
private fun VenuePickerSheetLoadedPreviewLight() {
    LightThemePreview {
        VenuePickerSheet(picker = PreviewData.venueCatalogLoaded(), selectedVenues = setOf(PreviewData.sileaVenue), onDismiss = {}, onSearchChange = {}, onVenueToggle = {})
    }
}

// Previews - Dark theme

@Preview
@Composable
private fun VenuePickerSheetLoadingPreviewDark() {
    DarkThemePreview {
        VenuePickerSheet(picker = PreviewData.venueCatalogLoading(), selectedVenues = setOf(PreviewData.sileaVenue), onDismiss = {}, onSearchChange = {}, onVenueToggle = {})
    }
}

@Preview
@Composable
private fun VenuePickerSheetLoadedPreviewDark() {
    DarkThemePreview {
        VenuePickerSheet(picker = PreviewData.venueCatalogLoaded(), selectedVenues = setOf(PreviewData.sileaVenue), onDismiss = {}, onSearchChange = {}, onVenueToggle = {})
    }
}
