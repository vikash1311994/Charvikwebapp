package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
  primary = SageTeal,
  onPrimary = Color.White,
  primaryContainer = SageTint,
  onPrimaryContainer = SageTealDark,
  secondary = Terracotta,
  onSecondary = Color.White,
  secondaryContainer = TerracottaSoft,
  onSecondaryContainer = Color(0xFF5D1D0D),
  tertiary = HoneyAmber,
  onTertiary = Color.White,
  tertiaryContainer = HoneySoft,
  onTertiaryContainer = Color(0xFF4B3000),
  background = CreamCanvas,
  onBackground = InkCharcoal,
  surface = PureWarmCard,
  onSurface = InkCharcoal,
  surfaceVariant = LinenSurface,
  onSurfaceVariant = SlateMuted,
  outline = WarmOutline,
  outlineVariant = Color(0xFFE7DFD3),
  error = ClinicalRed,
  onError = Color.White,
  errorContainer = ClinicalRedContainer,
  onErrorContainer = Color(0xFF410E0B),
)

private val DarkColors = darkColorScheme(
  primary = NightSage,
  onPrimary = Color(0xFF0A2A27),
  primaryContainer = NightSageContainer,
  onPrimaryContainer = NightInk,
  secondary = NightTerracotta,
  onSecondary = Color(0xFF3E140B),
  secondaryContainer = NightTerracottaContainer,
  onSecondaryContainer = Color(0xFFFFDAD1),
  tertiary = NightHoney,
  onTertiary = Color(0xFF3B2400),
  tertiaryContainer = Color(0xFF4A330B),
  onTertiaryContainer = Color(0xFFFFE0A8),
  background = NightCanvas,
  onBackground = NightInk,
  surface = NightCard,
  onSurface = NightInk,
  surfaceVariant = NightSurface,
  onSurfaceVariant = NightMuted,
  outline = NightOutline,
  outlineVariant = Color(0xFF283937),
  error = NightRed,
  onError = Color(0xFF601410),
  errorContainer = NightRedContainer,
  onErrorContainer = Color(0xFFF9DEDC),
)

val CharvikShapes = Shapes(
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(18.dp),
  large = RoundedCornerShape(24.dp),
  extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun CharvikTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = if (darkTheme) DarkColors else LightColors,
    typography = CharvikTypography,
    shapes = CharvikShapes,
    content = content,
  )
}
