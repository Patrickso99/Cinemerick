package com.preichert.cinemerick.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource

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
            icon = painterResource(CinemerickIcons.Close),
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
            icon = painterResource(CinemerickIcons.Close),
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
        CinemerickTonalButton(text = "Open details", onClick = {}, icon = painterResource(CinemerickIcons.ArrowBack))
    }
}

@Preview(name = "TonalButton with icon dark")
@Composable
private fun TonalButtonIconDarkPreview() {
    DarkThemePreview {
        CinemerickTonalButton(text = "Open details", onClick = {}, icon = painterResource(CinemerickIcons.ArrowBack))
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
            badgeContainer = Color(0xFFE30613),
            badgeContent = Color.White,
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
            badgeContainer = Color(0xFFE30613),
            badgeContent = Color.White,
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
            badgeContainer = Color(0xFF1E6FD9),
            badgeContent = Color.White,
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
        CinemerickChainChip(label = "UCI", container = Color(0xFFE30613), content = Color.White)
    }
}

@Preview(name = "ChainChip dark")
@Composable
private fun ChainChipDarkPreview() {
    DarkThemePreview {
        CinemerickChainChip(label = "UCI", container = Color(0xFFE30613), content = Color.White)
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
