package org.arcanaforge.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.arcanaforge.app.core.datastore.ThemeSetting

private val LightColors = lightColorScheme(
    primary = MoonlightGreen,
    onPrimary = ParchmentSurface,
    primaryContainer = ColorTokens.SageContainer,
    onPrimaryContainer = Ink,
    secondary = BurnishedCopper,
    onSecondary = ParchmentSurface,
    tertiary = CloudBlue,
    background = Parchment,
    onBackground = Ink,
    surface = ParchmentSurface,
    onSurface = Ink,
    surfaceVariant = ColorTokens.LightSurfaceVariant,
    onSurfaceVariant = ColorTokens.LightOnSurfaceVariant,
    outline = ColorTokens.LightOutline,
)

private val DarkColors = darkColorScheme(
    primary = MoonlightGreenDark,
    onPrimary = Night,
    primaryContainer = ColorTokens.DarkSageContainer,
    onPrimaryContainer = ParchmentSurface,
    secondary = BurnishedCopperDark,
    onSecondary = Night,
    tertiary = CloudBlueDark,
    background = Night,
    onBackground = Parchment,
    surface = NightSurface,
    onSurface = Parchment,
    surfaceVariant = ColorTokens.DarkSurfaceVariant,
    onSurfaceVariant = ColorTokens.DarkOnSurfaceVariant,
    outline = ColorTokens.DarkOutline,
)

private val AppTypography = Typography().let { base ->
    base.copy(
        displaySmall = base.displaySmall.copy(fontFamily = FontFamily.Serif),
        headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Serif),
        headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.Serif),
        titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Serif),
    )
}

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
)

@Composable
fun MoonlightGuidanceTheme(
    themeSetting: ThemeSetting,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeSetting) {
        ThemeSetting.System -> isSystemInDarkTheme()
        ThemeSetting.Light -> false
        ThemeSetting.Dark -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}

private object ColorTokens {
    val SageContainer = androidx.compose.ui.graphics.Color(0xFFD9EAE2)
    val DarkSageContainer = androidx.compose.ui.graphics.Color(0xFF25463D)
    val LightSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFEAE7DE)
    val LightOnSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF4E5651)
    val LightOutline = androidx.compose.ui.graphics.Color(0xFF777E78)
    val DarkSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF313936)
    val DarkOnSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFC9D1CC)
    val DarkOutline = androidx.compose.ui.graphics.Color(0xFF8B958F)
}
