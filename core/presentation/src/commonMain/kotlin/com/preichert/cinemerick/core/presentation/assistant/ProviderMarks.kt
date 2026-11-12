package com.preichert.cinemerick.core.presentation.assistant

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.preichert.cinemerick.core.domain.assistant.CloudProvider
import com.preichert.cinemerick.core.presentation.resources.Res
import com.preichert.cinemerick.core.presentation.resources.provider_anthropic
import com.preichert.cinemerick.core.presentation.resources.provider_gemini
import com.preichert.cinemerick.core.presentation.resources.provider_grok
import com.preichert.cinemerick.core.presentation.resources.provider_mistral
import com.preichert.cinemerick.core.presentation.resources.provider_openai
import com.preichert.cinemerick.core.presentation.resources.provider_openrouter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** The VectorDrawable of [provider]'s mark, from the provider file as supplied. */
internal fun CloudProvider.markResource(): DrawableResource = when (this) {
    CloudProvider.OPENAI -> Res.drawable.provider_openai
    CloudProvider.ANTHROPIC -> Res.drawable.provider_anthropic
    CloudProvider.GEMINI -> Res.drawable.provider_gemini
    CloudProvider.GROK -> Res.drawable.provider_grok
    CloudProvider.MISTRAL -> Res.drawable.provider_mistral
    CloudProvider.OPENROUTER -> Res.drawable.provider_openrouter
}

/**
 * Whether the mark is one color that follows the theme (OpenAI, Grok, OpenRouter) rather than its own colors.
 * Those files are black or single-color: [ProviderMark] tints them with the theme's on-surface color.
 */
internal val CloudProvider.isMonochromeMark: Boolean
    get() = when (this) {
        CloudProvider.OPENAI, CloudProvider.GROK, CloudProvider.OPENROUTER -> true
        CloudProvider.ANTHROPIC, CloudProvider.GEMINI, CloudProvider.MISTRAL -> false
    }

/**
 * The mark of [provider] on its own, without a tile. The marks are the provider files as supplied (SVG converted to
 * VectorDrawable in composeResources). Monochrome marks follow the theme; the others keep their own colors.
 * Decorative: the name next to it says which provider it is.
 */
@Composable
fun ProviderMark(
    provider: CloudProvider,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
) {
    Icon(
        painter = painterResource(provider.markResource()),
        contentDescription = null,
        modifier = modifier.size(size),
        tint = if (provider.isMonochromeMark) MaterialTheme.colorScheme.onSurface else Color.Unspecified,
    )
}
