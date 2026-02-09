package experimental.users.avayvod.musicplayer

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.libraries.material.compose.GoogleMaterial3Theme

val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF004a77),
    onPrimary = Color.White,
    surface = Color(0xFF2e3240),
    onSurface = Color.White,
    onSurfaceVariant = Color(0xB2FFFFFF),
  )

/** Dimensions used throughout the music player app. */
object MusicPlayerDimens {
  val widgetCornerRadius = 16.dp
  val widgetPadding = 32.dp
  val albumArtSize = 88.dp
  val albumArtCornerRadius = 4.dp
  val albumArtMetadataSpacing = 16.dp
  val trackInfoTextVPadding = 11.dp
  val progressBarInactiveTrackHeight = 2.dp
  val progressBarActiveTrackHeight = 3.dp
  val scrubberRadius = 6.dp
  val progressBarTopMargin = 31.dp
  val progressBarBottomMargin = 15.dp
  val timestampTopMargin = 1.dp
  val controlsLayoutWidth = 312.dp
  val smallButtonSize = 36.dp
  val playButtonSize = 72.dp
  val playButtonIconSize = 48.dp
}

@Composable
fun MusicPlayerTheme(content: @Composable () -> Unit) {
  GoogleMaterial3Theme(colorScheme = DarkColorScheme) { content() }
}
