package experimental.users.avayvod.musicplayer

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class RepeatMode {
  OFF,
  ONE,
  ALL,
}

data class TrackInfo(
  val title: String,
  val artist: String,
  val album: String,
  val assetFileName: String,
  val albumArtFileName: String? = null,
  val isFavorite: Boolean = false,
)

data class MusicPlayerState(
  val trackInfo: TrackInfo? = null,
  val currentTimeMs: Long = 0,
  val totalTimeMs: Long = 0,
  val bufferedMs: Long = 0,
  val isPlaying: Boolean = false,
  val repeatMode: RepeatMode = RepeatMode.OFF,
  val isFavorite: Boolean = false,
  val isLoadingError: Boolean = false,
  val isNextTrackEnabled: Boolean = false,
)

class MusicPlayerViewModel(private val application: Application) : AndroidViewModel(application) {

  private fun getIsNextTrackEnabled(repeatMode: RepeatMode): Boolean =
    currentTrackIndex < playlist.size - 1 || repeatMode == RepeatMode.ALL

  private val mediaPlayer = MediaPlayer()
  private var currentTrackIndex = 0
  private val trackIdToBufferedMs = mutableMapOf<String, Long>()
  private var delayedNextTrackJob: Job? = null
  private var playlist =
    listOf(
      TrackInfo(
        title = "Fade",
        artist = "Mezcla",
        album = "Shifting Hues",
        assetFileName = "fade-by-mezcla.mp3",
        albumArtFileName = "fade.jpeg",
      ),
      TrackInfo(
        title = "Memories",
        artist = "Rexlambo",
        album = "Days Gone By",
        assetFileName = "memories-by-rexlambo.mp3",
        albumArtFileName = "memories.jpeg",
      ),
      TrackInfo(
        title = "Remote Location",
        artist = "Filo Starquez",
        album = "Wanderlust",
        assetFileName = "remote-location-by-filo-starquez.mp3",
        albumArtFileName = null,
      ),
      TrackInfo(
        title = "Unknown",
        artist = "Unknown",
        album = "Unknown",
        assetFileName = "broken_track.mp3",
        albumArtFileName = "broken.jpeg",
      ),
      TrackInfo(
        title = "Retroverse Pt1",
        artist = "Lucjo",
        album = "Neon Futures",
        assetFileName = "retroverse-pt1-by-lucjo.mp3",
        albumArtFileName = "retroverse.jpeg",
      ),
      TrackInfo(
        title = "Smile",
        artist = "Next Route And Declan Dp",
        album = "Good Vibes",
        assetFileName = "smile-by-next-route-and-declan-dp.mp3",
        albumArtFileName = "smile.jpeg",
      ),
    )

  private val _uiState =
    MutableStateFlow(
      MusicPlayerState(
        trackInfo = playlist[currentTrackIndex],
        totalTimeMs = 0L,
        currentTimeMs = 0L,
        bufferedMs = 0L,
        isFavorite = playlist[currentTrackIndex].isFavorite,
        isLoadingError = false,
      )
    )
  val uiState: StateFlow<MusicPlayerState> = _uiState

  init {
    loadTrack(playlist[currentTrackIndex])
    mediaPlayer.setOnCompletionListener {
      if (_uiState.value.repeatMode == RepeatMode.ONE) {
        playTrack()
      } else if (_uiState.value.repeatMode == RepeatMode.ALL) {
        nextTrack()
      } else {
        if (currentTrackIndex < playlist.size - 1) {
          nextTrack()
        } else {
          _uiState.value = _uiState.value.copy(isPlaying = false, currentTimeMs = 0)
          mediaPlayer.seekTo(0)
        }
      }
    }
    viewModelScope.launch {
      while (true) {
        if (mediaPlayer.isPlaying) {
          _uiState.value =
            _uiState.value.copy(
              currentTimeMs =
                mediaPlayer.currentPosition.toLong().coerceAtMost(_uiState.value.totalTimeMs)
            )
        }
        delay(100L) // Update ~10 times per second
      }
    }
    viewModelScope.launch {
      while (true) {
        if (_uiState.value.trackInfo != null && _uiState.value.totalTimeMs > 0) {
          val trackId = _uiState.value.trackInfo!!.assetFileName
          val currentBuffered = trackIdToBufferedMs.getOrDefault(trackId, 0L)
          if (currentBuffered < _uiState.value.totalTimeMs) {
            val newBuffered = (currentBuffered + 200L).coerceAtMost(_uiState.value.totalTimeMs)
            trackIdToBufferedMs[trackId] = newBuffered
            if (_uiState.value.bufferedMs != newBuffered) {
              _uiState.value = _uiState.value.copy(bufferedMs = newBuffered)
            }
          }
        }
        delay(100L)
      }
    }
  }

  private fun loadTrack(trackInfo: TrackInfo) {
    try {
      application.assets.openFd(trackInfo.assetFileName).use { afd ->
        mediaPlayer.reset()
        mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        mediaPlayer.prepare()
        _uiState.value =
          _uiState.value.copy(
            trackInfo = trackInfo,
            totalTimeMs = mediaPlayer.duration.toLong(),
            currentTimeMs = 0,
            isFavorite = trackInfo.isFavorite,
            bufferedMs = trackIdToBufferedMs.getOrDefault(trackInfo.assetFileName, 0L),
            isLoadingError = false,
            isNextTrackEnabled = getIsNextTrackEnabled(_uiState.value.repeatMode),
          )
      }
    } catch (e: Exception) {
      // Handle error: track not found, etc.
      _uiState.value =
        _uiState.value.copy(
          trackInfo = trackInfo,
          totalTimeMs = 0,
          currentTimeMs = 0,
          bufferedMs = 0,
          isFavorite = trackInfo.isFavorite,
          isLoadingError = true,
          isNextTrackEnabled = getIsNextTrackEnabled(_uiState.value.repeatMode),
        )
    }
  }

  private fun playTrack() {
    delayedNextTrackJob?.cancel()
    if (_uiState.value.isLoadingError) {
      delayedNextTrackJob =
        viewModelScope.launch {
          delay(1000L)
          nextTrack()
        }
      return
    }
    mediaPlayer.start()
    _uiState.value =
      _uiState.value.copy(isPlaying = true, totalTimeMs = mediaPlayer.duration.toLong())
  }

  fun togglePlayPause() {
    delayedNextTrackJob?.cancel()
    if (mediaPlayer.isPlaying) {
      mediaPlayer.pause()
      _uiState.value = _uiState.value.copy(isPlaying = false)
    } else {
      if (_uiState.value.isLoadingError) {
        delayedNextTrackJob =
          viewModelScope.launch {
            delay(1000L)
            nextTrack()
          }
        return
      }
      mediaPlayer.start()
      _uiState.value = _uiState.value.copy(isPlaying = true)
    }
  }

  fun nextTrack() {
    delayedNextTrackJob?.cancel()
    if (currentTrackIndex == playlist.size - 1 && _uiState.value.repeatMode != RepeatMode.ALL) {
      return
    }
    currentTrackIndex = (currentTrackIndex + 1) % playlist.size
    loadTrack(playlist[currentTrackIndex])
    playTrack()
  }

  fun prevTrack() {
    delayedNextTrackJob?.cancel()
    // If track played for >2s, or if it's the first track and we don't repeat all, restart.
    if (
      mediaPlayer.currentPosition > 2000 ||
        (currentTrackIndex == 0 && _uiState.value.repeatMode != RepeatMode.ALL)
    ) {
      mediaPlayer.seekTo(0)
      _uiState.value = _uiState.value.copy(currentTimeMs = 0)
    } else {
      // Otherwise, go to previous track, wrapping if needed (which only happens if repeatMode==ALL)
      currentTrackIndex = if (currentTrackIndex == 0) playlist.size - 1 else currentTrackIndex - 1
      loadTrack(playlist[currentTrackIndex])
      playTrack()
    }
  }

  fun toggleRepeatMode() {
    val nextMode =
      when (_uiState.value.repeatMode) {
        RepeatMode.OFF -> RepeatMode.ONE
        RepeatMode.ONE -> RepeatMode.ALL
        RepeatMode.ALL -> RepeatMode.OFF
      }
    mediaPlayer.isLooping = nextMode == RepeatMode.ONE
    _uiState.value =
      _uiState.value.copy(
        repeatMode = nextMode,
        isNextTrackEnabled = getIsNextTrackEnabled(nextMode),
      )
  }

  fun toggleFavorite() {
    val newIsFavorite = !_uiState.value.isFavorite
    playlist =
      playlist.mapIndexed { index, trackInfo ->
        if (index == currentTrackIndex) trackInfo.copy(isFavorite = newIsFavorite) else trackInfo
      }
    _uiState.value =
      _uiState.value.copy(isFavorite = newIsFavorite, trackInfo = playlist[currentTrackIndex])
  }

  fun seekTo(positionMs: Long) {
    mediaPlayer.seekTo(positionMs.toInt())
    _uiState.value = _uiState.value.copy(currentTimeMs = positionMs)
    val trackId = _uiState.value.trackInfo?.assetFileName ?: return
    val currentBuffered = trackIdToBufferedMs.getOrDefault(trackId, 0L)
    if (positionMs > currentBuffered) {
      trackIdToBufferedMs[trackId] = positionMs
      _uiState.value = _uiState.value.copy(bufferedMs = positionMs)
    }
  }

  override fun onCleared() {
    mediaPlayer.release()
    // No need to call super.onCleared() as it's empty.
  }
}
