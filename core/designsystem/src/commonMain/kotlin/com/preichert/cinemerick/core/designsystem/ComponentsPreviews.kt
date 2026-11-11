package com.preichert.cinemerick.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// One preview per distinct state. Each is wrapped in the light or dark theme.

// Filter chip

@Preview(name = "FilterChip unselected")
@Composable
private fun FilterChipUnselectedPreview() {
    LightThemePreview {
        CinemerickFilterChip(label = "IMAX", selected = false, onClick = {})
    }
}

@Preview(name = "FilterChip selected")
@Composable
private fun FilterChipSelectedPreview() {
    LightThemePreview {
        CinemerickFilterChip(label = "IMAX", selected = true, onClick = {})
    }
}

@Preview(name = "FilterChip disabled")
@Composable
private fun FilterChipDisabledPreview() {
    LightThemePreview {
        CinemerickFilterChip(label = "IMAX", selected = false, onClick = {}, enabled = false)
    }
}

@Preview(name = "FilterChip long label, large font", fontScale = 2f)
@Composable
private fun FilterChipLongLabelPreview() {
    LightThemePreview {
        CinemerickFilterChip(
            label = "Versione originale sottotitolata in italiano",
            selected = true,
            onClick = {},
        )
    }
}

@Preview(name = "FilterChip selected dark")
@Composable
private fun FilterChipSelectedDarkPreview() {
    DarkThemePreview {
        CinemerickFilterChip(label = "IMAX", selected = true, onClick = {})
    }
}

// Icon button

@Preview(name = "IconButton light")
@Composable
private fun IconButtonLightPreview() {
    LightThemePreview {
        CinemerickIconButton(
            icon = CinemerickIcons.Close,
            contentDescription = "Close",
            onClick = {},
        )
    }
}

@Preview(name = "IconButton dark")
@Composable
private fun IconButtonDarkPreview() {
    DarkThemePreview {
        CinemerickIconButton(
            icon = CinemerickIcons.Close,
            contentDescription = "Close",
            onClick = {},
        )
    }
}

// Primary button

@Preview(name = "PrimaryButton enabled")
@Composable
private fun PrimaryButtonEnabledPreview() {
    LightThemePreview {
        CinemerickPrimaryButton(text = "Search", onClick = {})
    }
}

@Preview(name = "PrimaryButton loading")
@Composable
private fun PrimaryButtonLoadingPreview() {
    LightThemePreview {
        CinemerickPrimaryButton(text = "Search", onClick = {}, loading = true)
    }
}

@Preview(name = "PrimaryButton disabled")
@Composable
private fun PrimaryButtonDisabledPreview() {
    LightThemePreview {
        CinemerickPrimaryButton(text = "Search", onClick = {}, enabled = false)
    }
}

@Preview(name = "PrimaryButton dark")
@Composable
private fun PrimaryButtonDarkPreview() {
    DarkThemePreview {
        CinemerickPrimaryButton(text = "Search", onClick = {})
    }
}

// Secondary button

@Preview(name = "SecondaryButton enabled")
@Composable
private fun SecondaryButtonEnabledPreview() {
    LightThemePreview {
        CinemerickSecondaryButton(text = "Cancel", onClick = {})
    }
}

@Preview(name = "SecondaryButton disabled")
@Composable
private fun SecondaryButtonDisabledPreview() {
    LightThemePreview {
        CinemerickSecondaryButton(text = "Cancel", onClick = {}, enabled = false)
    }
}

@Preview(name = "SecondaryButton dark")
@Composable
private fun SecondaryButtonDarkPreview() {
    DarkThemePreview {
        CinemerickSecondaryButton(text = "Cancel", onClick = {})
    }
}

// Tonal button

@Preview(name = "TonalButton text")
@Composable
private fun TonalButtonTextPreview() {
    LightThemePreview {
        CinemerickTonalButton(text = "Open details", onClick = {})
    }
}

@Preview(name = "TonalButton with icon")
@Composable
private fun TonalButtonIconPreview() {
    LightThemePreview {
        CinemerickTonalButton(text = "Open details", onClick = {}, icon = CinemerickIcons.ArrowBack)
    }
}

@Preview(name = "TonalButton with icon dark")
@Composable
private fun TonalButtonIconDarkPreview() {
    DarkThemePreview {
        CinemerickTonalButton(text = "Open details", onClick = {}, icon = CinemerickIcons.ArrowBack)
    }
}

// Chain card

@Preview(name = "ChainCard collapsed with selection")
@Composable
private fun ChainCardCollapsedPreview() {
    LightThemePreview {
        CinemerickChainCard(
            name = "UCI",
            subtitle = "2 sale selezionate",
            initials = "UC",
            badgeContainer = CinemaBrandColors.uciBlue,
            badgeContent = CinemaBrandColors.onUciBlue,
            selectedCount = 2,
            expanded = false,
            onToggle = {},
            venues = {},
        )
    }
}

@Preview(name = "ChainCard expanded")
@Composable
private fun ChainCardExpandedPreview() {
    LightThemePreview {
        CinemerickChainCard(
            name = "UCI",
            subtitle = "Marcon",
            initials = "UC",
            badgeContainer = CinemaBrandColors.uciBlue,
            badgeContent = CinemaBrandColors.onUciBlue,
            selectedCount = 1,
            expanded = true,
            onToggle = {},
            venues = { Text("UCI Luxe Marcon") },
        )
    }
}

@Preview(name = "ChainCard empty with quick add dark")
@Composable
private fun ChainCardEmptyDarkPreview() {
    DarkThemePreview {
        CinemerickChainCard(
            name = "Cinergia",
            subtitle = "Nessuna sala selezionata",
            initials = "CI",
            badgeContainer = CinemaBrandColors.cinergia,
            badgeContent = CinemaBrandColors.onCinergia,
            selectedCount = 0,
            expanded = false,
            onToggle = {},
            onQuickAdd = {},
            venues = {},
        )
    }
}

// Chain chip

@Preview(name = "ChainChip light")
@Composable
private fun ChainChipLightPreview() {
    LightThemePreview {
        CinemerickChainChip(label = "UCI", container = CinemaBrandColors.uciBlue, content = CinemaBrandColors.onUciBlue)
    }
}

@Preview(name = "ChainChip dark")
@Composable
private fun ChainChipDarkPreview() {
    DarkThemePreview {
        CinemerickChainChip(label = "UCI", container = CinemaBrandColors.uciBlue, content = CinemaBrandColors.onUciBlue)
    }
}

// Switch

@Preview(name = "Switch on")
@Composable
private fun SwitchOnPreview() {
    LightThemePreview {
        CinemerickSwitch(checked = true)
    }
}

@Preview(name = "Switch off")
@Composable
private fun SwitchOffPreview() {
    LightThemePreview {
        CinemerickSwitch(checked = false)
    }
}

@Preview(name = "Switch on dark")
@Composable
private fun SwitchOnDarkPreview() {
    DarkThemePreview {
        CinemerickSwitch(checked = true)
    }
}

@Preview(name = "Switch off dark")
@Composable
private fun SwitchOffDarkPreview() {
    DarkThemePreview {
        CinemerickSwitch(checked = false)
    }
}

// Segmented control

@Preview(name = "Segmented, icons")
@Composable
private fun SegmentedControlIconsPreview() {
    LightThemePreview {
        CinemerickSegmentedControl(
            options = listOf("Auto" to CinemerickIcons.Auto, "Light" to CinemerickIcons.Sun, "Dark" to CinemerickIcons.Moon),
            selectedIndex = 0,
            onSelect = {},
        )
    }
}

@Preview(name = "Segmented, text only, dark")
@Composable
private fun SegmentedControlTextDarkPreview() {
    DarkThemePreview {
        CinemerickSegmentedControl(options = listOf("English" to null, "Italiano" to null), selectedIndex = 1, onSelect = {})
    }
}

// Switch row

@Preview(name = "Switch row on, with description")
@Composable
private fun SwitchRowOnPreview() {
    LightThemePreview {
        CinemerickSwitchRow(
            title = "Smooth animations",
            description = "Transitions and list motion",
            checked = true,
            onCheckedChange = {},
        )
    }
}

@Preview(name = "Switch row off, dark")
@Composable
private fun SwitchRowOffDarkPreview() {
    DarkThemePreview {
        CinemerickSwitchRow(title = "Smooth animations", checked = false, onCheckedChange = {})
    }
}
