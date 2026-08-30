package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun MediaViewerDialog(
    mediaUris: List<String>,
    initialIndex: Int = 0,
    title: String = "Journey Media",
    subtitle: String? = null,
    onDismissRequest: () -> Unit
) {
    if (mediaUris.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, mediaUris.lastIndex)) }
    val currentUri = mediaUris.getOrNull(currentIndex) ?: return
    val isVideo = remember(currentUri) { isVideoMedia(currentUri) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("media_viewer_dialog")
        ) {
            // Main Media Content
            if (isVideo) {
                VideoPlayerView(
                    videoUri = currentUri,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PhotoViewer(
                    imageUri = currentUri,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top App Bar Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .testTag("media_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Media Viewer",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isVideo) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(16.dp).padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.75f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (mediaUris.size > 1) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "${currentIndex + 1} / ${mediaUris.size}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            // Bottom Navigation Arrows (if multiple media items)
            if (mediaUris.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp, start = 24.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentIndex > 0) currentIndex--
                        },
                        enabled = currentIndex > 0,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (currentIndex > 0) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Media",
                            tint = if (currentIndex > 0) Color.White else Color.White.copy(alpha = 0.3f)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (currentIndex < mediaUris.lastIndex) currentIndex++
                        },
                        enabled = currentIndex < mediaUris.lastIndex,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (currentIndex < mediaUris.lastIndex) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Media",
                            tint = if (currentIndex < mediaUris.lastIndex) Color.White else Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoViewer(
    imageUri: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = "Full Size Travel Photo",
            modifier = Modifier
                .fillMaxSize()
                .testTag("fullscreen_photo_image"),
            contentScale = ContentScale.Fit,
            onSuccess = { isLoading = false },
            onError = { isLoading = false }
        )

        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

@Composable
private fun VideoPlayerView(
    videoUri: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }
    var showControls by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }

    // Coroutine ticker for updating seek progress
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoViewInstance?.let { vv ->
                currentPosition = vv.currentPosition.toLong()
                duration = vv.duration.toLong().coerceAtLeast(1L)
            }
            delay(250)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("video_player_surface"),
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(Uri.parse(videoUri))
                    setOnPreparedListener { mp ->
                        isBuffering = false
                        duration = mp.duration.toLong()
                        mp.isLooping = true
                        start()
                        isPlaying = true
                    }
                    setOnCompletionListener {
                        isPlaying = false
                        currentPosition = duration
                    }
                    setOnErrorListener { _, _, _ ->
                        isBuffering = false
                        true
                    }
                    videoViewInstance = this
                }
            },
            update = { vv ->
                videoViewInstance = vv
            }
        )

        if (isBuffering) {
            CircularProgressIndicator(
                color = Color(0xFF00E5FF),
                modifier = Modifier.size(48.dp)
            )
        }

        // Overlay Playback Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            IconButton(
                onClick = {
                    videoViewInstance?.let { vv ->
                        if (vv.isPlaying) {
                            vv.pause()
                            isPlaying = false
                        } else {
                            vv.start()
                            isPlaying = true
                        }
                    }
                },
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .testTag("video_play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Bottom Seekbar & Time Label
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp, start = 20.dp, end = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                        onValueChange = { fraction ->
                            val targetMs = (fraction * duration).toInt()
                            videoViewInstance?.seekTo(targetMs)
                            currentPosition = targetMs.toLong()
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF),
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTimeMs(currentPosition),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Text(
                            text = formatTimeMs(duration),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            videoViewInstance?.stopPlayback()
        }
    }
}

fun isVideoMedia(uri: String): Boolean {
    val lower = uri.lowercase(Locale.ROOT)
    return lower.endsWith(".mp4") ||
            lower.endsWith(".mkv") ||
            lower.endsWith(".webm") ||
            lower.endsWith(".3gp") ||
            lower.contains("video") ||
            lower.contains("/mp4")
}

private fun formatTimeMs(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
}
