package experimental.users.avayvod.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

  companion object {
    private const val TRANSFORMATIONS_ENABLED = true
    private const val USE_MARQUEE_FOR_LONG_TEXT = true
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MusicPlayerTheme {
        var scale by remember { mutableFloatStateOf(1f) }
        var rotation by remember { mutableFloatStateOf(0f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        val transformState = rememberTransformableState { zoomChange, panChange, rotationChange ->
          if (TRANSFORMATIONS_ENABLED) {
            scale = (scale * zoomChange).coerceIn(0.5f, 3f)
            rotation += rotationChange
            offset += panChange
          }
        }
        Surface(modifier = Modifier.fillMaxSize(), color = Color.DarkGray) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            MusicPlayerScreen(
              modifier =
                if (TRANSFORMATIONS_ENABLED) {
                  Modifier.transformable(transformState)
                } else {
                  Modifier
                },
              scale = scale,
              rotation = rotation,
              offset = offset,
              useMarquee = USE_MARQUEE_FOR_LONG_TEXT,
            )
            if (TRANSFORMATIONS_ENABLED) {
              FloatingActionButton(
                onClick = {
                  scale = 1f
                  rotation = 0f
                  offset = Offset.Zero
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                shape = CircleShape,
              ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Reset position")
              }
            }
          }
        }
      }
    }
  }
}
