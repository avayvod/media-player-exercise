package experimental.users.avayvod.musicplayer

import android.graphics.BitmapFactory
import android.view.animation.AnticipateOvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOneOn
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

@Composable
fun MusicPlayerScreen(
  modifier: Modifier = Modifier,
  scale: Float = 1f,
  rotation: Float = 0f,
  offset: Offset = Offset.Zero,
  useMarquee: Boolean = true,
  viewModel: MusicPlayerViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsState()
  Box(
    modifier =
      modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
        rotationZ = rotation
        translationX = offset.x
        translationY = offset.y
      }
  ) {
    MusicPlayerScreenContent(
      state = uiState,
      useMarquee = useMarquee,
      onTogglePlayPause = viewModel::togglePlayPause,
      onNextTrack = viewModel::nextTrack,
      onPrevTrack = viewModel::prevTrack,
      onToggleRepeatMode = viewModel::toggleRepeatMode,
      onToggleFavorite = viewModel::toggleFavorite,
      onSeek = viewModel::seekTo,
    )
  }
}

@Composable
fun MusicPlayerScreenContent(
  state: MusicPlayerState,
  useMarquee: Boolean,
  onTogglePlayPause: () -> Unit,
  onNextTrack: () -> Unit,
  onPrevTrack: () -> Unit,
  onToggleRepeatMode: () -> Unit,
  onToggleFavorite: () -> Unit,
  onSeek: (Long) -> Unit,
) {
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .background(
          MaterialTheme.colorScheme.surface,
          RoundedCornerShape(MusicPlayerDimens.widgetCornerRadius),
        )
        .padding(MusicPlayerDimens.widgetPadding)
        .width(480.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    TrackInfoSection(state.trackInfo, state.isLoadingError, useMarquee)
    Spacer(modifier = Modifier.height(MusicPlayerDimens.progressBarTopMargin))
    PlaybackProgressSection(state.currentTimeMs, state.bufferedMs, state.totalTimeMs, onSeek)
    Spacer(modifier = Modifier.height(MusicPlayerDimens.progressBarBottomMargin))
    ControlButtonsSection(
      isPlaying = state.isPlaying,
      repeatMode = state.repeatMode,
      isFavorite = state.isFavorite,
      isNextTrackEnabled = state.isNextTrackEnabled,
      onTogglePlayPause = onTogglePlayPause,
      onNextTrack = onNextTrack,
      onPrevTrack = onPrevTrack,
      onToggleRepeatMode = onToggleRepeatMode,
      onToggleFavorite = onToggleFavorite,
    )
  }
}

@Composable
fun TrackInfoSection(trackInfo: TrackInfo?, isLoadingError: Boolean, useMarquee: Boolean) {
  val gradientWidth = 16.dp
  Row(modifier = Modifier.fillMaxWidth()) {
    val context = LocalContext.current
    val imageBitmap =
      remember(trackInfo?.albumArtFileName) {
        val fileName = trackInfo?.albumArtFileName ?: "placeholder.jpeg"
        try {
          context.assets.open(fileName).use { stream ->
            BitmapFactory.decodeStream(stream).asImageBitmap()
          }
        } catch (e: Exception) {
          null
        }
      }

    if (imageBitmap != null) {
      Image(
        bitmap = imageBitmap,
        contentDescription = "Album Art",
        modifier =
          Modifier.size(MusicPlayerDimens.albumArtSize)
            .clip(RoundedCornerShape(MusicPlayerDimens.albumArtCornerRadius)),
        contentScale = ContentScale.Crop,
      )
    } else {
      Surface(
        modifier = Modifier.size(MusicPlayerDimens.albumArtSize),
        shape = RoundedCornerShape(MusicPlayerDimens.albumArtCornerRadius),
        color = MaterialTheme.colorScheme.outline,
      ) {}
    }
    Spacer(modifier = Modifier.width(MusicPlayerDimens.albumArtMetadataSpacing))
    if (isLoadingError) {
      Column(
        modifier =
          Modifier.height(MusicPlayerDimens.albumArtSize)
            .padding(
              top = MusicPlayerDimens.trackInfoTextVPadding,
              bottom = MusicPlayerDimens.trackInfoTextVPadding,
            ),
        verticalArrangement = Arrangement.Center,
      ) {
        Text(
          "Error loading track",
          style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
          color = MaterialTheme.colorScheme.error,
          maxLines = 1,
        )
      }
    } else {
      Column(
        modifier =
          Modifier.height(MusicPlayerDimens.albumArtSize)
            .padding(
              top = MusicPlayerDimens.trackInfoTextVPadding,
              bottom = MusicPlayerDimens.trackInfoTextVPadding,
            ),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        var showTitleGradient by remember { mutableStateOf(false) }
        var showDetailsGradient by remember { mutableStateOf(false) }
        val surfaceColor = MaterialTheme.colorScheme.surface
        BoxWithConstraints {
          val maxWidth = constraints.maxWidth
          Box(
            modifier =
              Modifier.drawWithContent {
                drawContent()
                if (showTitleGradient && useMarquee) {
                  drawRect(
                    brush =
                      Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, surfaceColor),
                        startX = size.width - gradientWidth.toPx(),
                        endX = size.width,
                      ),
                    topLeft = Offset(x = size.width - gradientWidth.toPx(), y = 0f),
                  )
                }
              }
          ) {
            Text(
              trackInfo?.title ?: "",
              style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              softWrap = false,
              overflow = if (useMarquee) TextOverflow.Clip else TextOverflow.Ellipsis,
              onTextLayout = { showTitleGradient = it.size.width > maxWidth },
              modifier =
                if (useMarquee) {
                  Modifier.basicMarquee(initialDelayMillis = 3000, repeatDelayMillis = 1000)
                } else {
                  Modifier
                },
            )
          }
        }
        BoxWithConstraints {
          val maxWidth = constraints.maxWidth
          Box(
            modifier =
              Modifier.drawWithContent {
                drawContent()
                if (showDetailsGradient && useMarquee) {
                  drawRect(
                    brush =
                      Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, surfaceColor),
                        startX = size.width - gradientWidth.toPx(),
                        endX = size.width,
                      ),
                    topLeft = Offset(x = size.width - gradientWidth.toPx(), y = 0f),
                  )
                }
              }
          ) {
            Text(
              "${trackInfo?.album ?: "Unknown Album"}, ${trackInfo?.artist ?: "Unknown Artist"}",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
              maxLines = 1,
              softWrap = false,
              overflow = if (useMarquee) TextOverflow.Clip else TextOverflow.Ellipsis,
              onTextLayout = { showDetailsGradient = it.size.width > maxWidth },
              modifier =
                if (useMarquee) {
                  Modifier.basicMarquee(initialDelayMillis = 3000, repeatDelayMillis = 1000)
                } else {
                  Modifier
                },
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackProgressSection(
  currentTimeMs: Long,
  bufferedMs: Long,
  totalTimeMs: Long,
  onSeek: (Long) -> Unit,
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
    val sliderColors =
      SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.onSurface,
        activeTrackColor = Color.Transparent,
        inactiveTrackColor = Color.Transparent,
      )
    val fraction = if (totalTimeMs > 0) currentTimeMs.toFloat() / totalTimeMs.toFloat() else 0f
    val bufferedFraction = if (totalTimeMs > 0) bufferedMs.toFloat() / totalTimeMs.toFloat() else 0f
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val thumbSizeDp =
      if (isPressed || isDragged) {
        MusicPlayerDimens.scrubberRadius * 4
      } else {
        MusicPlayerDimens.scrubberRadius * 2
      }
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    Slider(
      value = fraction,
      onValueChange = { onSeek((it * totalTimeMs).toLong()) },
      modifier =
        Modifier.fillMaxWidth()
          .height(MusicPlayerDimens.scrubberRadius * 4)
          .padding(0.dp)
          .semantics(mergeDescendants = true) {
            stateDescription = formatTimeForAccessibility(currentTimeMs)
          },
      colors = sliderColors,
      interactionSource = interactionSource,
      // drawing thumb via this parameter adds unwanted padding to the slider
      thumb = {},
      track = {
        Canvas(modifier = Modifier.fillMaxWidth().height(thumbSizeDp)) {
          val inactiveTrackStrokeWidthPx = MusicPlayerDimens.progressBarInactiveTrackHeight.toPx()
          val activeTrackStrokeWidthPx = MusicPlayerDimens.progressBarActiveTrackHeight.toPx()
          // inactive track
          drawLine(
            color = onSurfaceColor.copy(alpha = 0.2f),
            start = Offset(0f, center.y),
            end = Offset(size.width, center.y),
            strokeWidth = inactiveTrackStrokeWidthPx,
            cap = StrokeCap.Round,
          )
          // buffered track
          drawLine(
            color = onSurfaceColor.copy(alpha = 0.5f),
            start = Offset(0f, center.y),
            end = Offset(size.width * bufferedFraction, center.y),
            strokeWidth = inactiveTrackStrokeWidthPx,
            cap = StrokeCap.Round,
          )
          // active track
          drawLine(
            color = onSurfaceColor,
            start = Offset(0f, center.y),
            end = Offset(size.width * fraction, center.y),
            strokeWidth = activeTrackStrokeWidthPx,
            cap = StrokeCap.Round,
          )
          drawCircle(
            color = onSurfaceColor,
            radius = thumbSizeDp.toPx() / 2,
            center = Offset(size.width * fraction, center.y),
          )
        }
      },
    )
    Spacer(modifier = Modifier.height(MusicPlayerDimens.timestampTopMargin))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(
        formatTime(currentTimeMs),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        modifier =
          Modifier.semantics {
            contentDescription = "Current time ${formatTimeForAccessibility(currentTimeMs)}"
          },
      )
      Text(
        formatTime(totalTimeMs),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        modifier =
          Modifier.semantics {
            contentDescription = "Total time ${formatTimeForAccessibility(totalTimeMs)}"
          },
      )
    }
  }
}

private fun formatTime(timeMs: Long): String {
  val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs)
  val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) % 60
  // TODO: make this work in all locales (if mm:ss is not the correct style or need to be RTL)
  return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

private fun formatTimeForAccessibility(timeMs: Long): String {
  val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs)
  val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) % 60
  val minSuffix = if (minutes == 1L) "" else "s"
  val secSuffix = if (seconds == 1L) "" else "s"
  return when {
    minutes > 0 && seconds > 0 -> "$minutes minute$minSuffix $seconds second$secSuffix"
    minutes > 0 -> "$minutes minute$minSuffix"
    else -> "$seconds second$secSuffix"
  }
}

@Composable
fun ControlButtonsSection(
  isPlaying: Boolean,
  repeatMode: RepeatMode,
  isFavorite: Boolean,
  isNextTrackEnabled: Boolean,
  onTogglePlayPause: () -> Unit,
  onNextTrack: () -> Unit,
  onPrevTrack: () -> Unit,
  onToggleRepeatMode: () -> Unit,
  onToggleFavorite: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  val favoriteIconScale = remember { Animatable(1f) }
  val repeatIconRotation = remember { Animatable(0f) }
  Row(
    modifier = Modifier.width(MusicPlayerDimens.controlsLayoutWidth),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    IconButton(
      onClick = {
        onToggleRepeatMode()
        if (!repeatIconRotation.isRunning) {
          scope.launch {
            repeatIconRotation.snapTo(0f)
            repeatIconRotation.animateTo(
              targetValue = 360f,
              animationSpec =
                tween(
                  durationMillis = 700,
                  easing = Easing { AnticipateOvershootInterpolator().getInterpolation(it) },
                ),
            )
          }
        }
      },
      modifier = Modifier.size(MusicPlayerDimens.smallButtonSize),
    ) {
      when (repeatMode) {
        RepeatMode.OFF ->
          Icon(
            Icons.Filled.Repeat,
            contentDescription = "Repeat off; enable repeat one",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.rotate(repeatIconRotation.value),
          )
        RepeatMode.ONE ->
          Icon(
            Icons.Filled.RepeatOneOn,
            contentDescription = "Repeat one track; enable repeat all",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.rotate(repeatIconRotation.value),
          )
        RepeatMode.ALL ->
          Icon(
            Icons.Filled.RepeatOn,
            contentDescription = "Repeat all tracks; disable repeat",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.rotate(repeatIconRotation.value),
          )
      }
    }
    IconButton(onClick = onPrevTrack, modifier = Modifier.size(MusicPlayerDimens.smallButtonSize)) {
      Icon(
        Icons.Filled.SkipPrevious,
        contentDescription = "Previous track",
        tint = MaterialTheme.colorScheme.onSurface,
      )
    }
    FloatingActionButton(
      onClick = onTogglePlayPause,
      modifier = Modifier.size(MusicPlayerDimens.playButtonSize),
      shape = CircleShape,
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
      Icon(
        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
        contentDescription = if (isPlaying) "Pause" else "Play",
        modifier = Modifier.size(MusicPlayerDimens.playButtonIconSize),
      )
    }
    IconButton(
      onClick = onNextTrack,
      enabled = isNextTrackEnabled,
      modifier = Modifier.size(MusicPlayerDimens.smallButtonSize),
    ) {
      Icon(
        Icons.Filled.SkipNext,
        contentDescription = "Next track",
        tint =
          if (isNextTrackEnabled) {
            MaterialTheme.colorScheme.onSurface
          } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
          },
      )
    }
    IconButton(
      onClick = {
        onToggleFavorite()
        if (!isFavorite && !favoriteIconScale.isRunning) {
          scope.launch {
            favoriteIconScale.animateTo(
              1.2f,
              animationSpec = tween(150, easing = FastOutSlowInEasing),
            )
            favoriteIconScale.animateTo(
              1.0f,
              animationSpec = tween(150, easing = FastOutSlowInEasing),
            )
            favoriteIconScale.animateTo(
              1.4f,
              animationSpec = tween(150, easing = FastOutSlowInEasing),
            )
            favoriteIconScale.animateTo(
              1.0f,
              animationSpec = tween(150, easing = FastOutSlowInEasing),
            )
          }
        }
      },
      modifier = Modifier.size(MusicPlayerDimens.smallButtonSize),
    ) {
      Icon(
        if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.scale(favoriteIconScale.value),
      )
    }
  }
}
