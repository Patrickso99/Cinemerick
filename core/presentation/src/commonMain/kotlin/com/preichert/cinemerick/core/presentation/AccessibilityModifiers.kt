package com.preichert.cinemerick.core.presentation

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Add semantic content description for accessibility.
 * Usage: Modifier.withContentDescription("Button description")
 */
fun Modifier.withContentDescription(description: String): Modifier =
    if (description.isNotEmpty()) {
        semantics { contentDescription = description }
    } else {
        this
    }

/**
 * Marks a decorative element that should be ignored by accessibility services.
 * Usage: Modifier.isDecorative()
 */
fun Modifier.isDecorative(): Modifier =
    semantics { contentDescription = "" }

/**
 * Add a semantic label for a list item.
 * Useful for screen readers to announce list position.
 */
fun Modifier.listItemDescription(
    index: Int,
    totalCount: Int,
    itemDescription: String
): Modifier =
    withContentDescription("$itemDescription, item ${index + 1} of $totalCount")
