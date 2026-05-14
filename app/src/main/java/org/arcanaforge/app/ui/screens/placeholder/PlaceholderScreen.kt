package org.arcanaforge.app.ui.screens.placeholder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.arcanaforge.app.ui.components.EmptyState

@Composable
fun PlaceholderScreen(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        EmptyState(
            icon = Icons.Outlined.Info,
            title = title,
            body = body,
        )
    }
}
