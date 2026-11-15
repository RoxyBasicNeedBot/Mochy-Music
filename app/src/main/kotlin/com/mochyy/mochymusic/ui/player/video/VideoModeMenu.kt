package com.mochyy.mochymusic.ui.player.video

import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil3.compose.AsyncImage
import com.mochyy.mochymusic.LocalPlayerConnection
import com.mochyy.mochymusic.R
import com.mochyy.mochymusic.constants.VideoQuality
import com.mochyy.mochymusic.constants.VideoModeWholeVideoKey
import com.mochyy.mochymusic.constants.VideoQualityKey
import com.mochyy.mochymusic.ui.component.AppSwitch
import com.mochyy.mochymusic.utils.rememberPreference
import com.mochyy.mochymusic.ui.component.SheetActionCard
import com.mochyy.mochymusic.ui.component.SheetActionCardRow
import com.mochyy.mochymusic.ui.component.SheetHeaderGap
import com.mochyy.mochymusic.ui.player.PressScaleActionButton
import com.mochyy.mochymusic.ui.player.menu.LocalMenuSheetClose
import com.mochyy.mochymusic.ui.player.menu.LocalMenuSheetFloating
import com.mochyy.mochymusic.ui.player.menu.LocalMenuSheetStack
import com.mochyy.mochymusic.ui.player.menu.LyricsOffsetSheet
import com.mochyy.mochymusic.ui.player.menu.LyricsProviderSheet
import com.mochyy.mochymusic.ui.player.menu.LyricsTranslationSheet
import com.mochyy.mochymusic.ui.player.menu.MenuRow
import com.mochyy.mochymusic.ui.player.menu.MenuRowSurface
import com.mochyy.mochymusic.ui.player.menu.MenuSheetSurface
import com.mochyy.mochymusic.ui.player.menu.PlaybackSpeedSheet
import com.mochyy.mochymusic.ui.player.menu.SleepTimerSheet
import com.mochyy.mochymusic.ui.player.menu.rememberMenuSheetStack
import com.mochyy.mochymusic.ui.screens.settings.videoSizeName
import com.mochyy.mochymusic.ui.theme.InterFontFamily
import com.mochyy.mochymusic.ui.utils.resize
import com.mochyy.mochymusic.utils.rememberEnumPreference
import com.mochyy.mochymusic.ui.component.RemoteFocus

// the chevron's menu in the full video mode: a card floating over the video, and every sheet it opens floats the same way
@Composable
internal fun VideoModeMenu(
    session: YouTubeVideoSession?,
    onDismiss: () -> Unit,
    onExitVideoMode: () -> Unit,
    onOpenPane: (VideoPane) -> Unit,
    onAddToPlaylist: () -> Unit,
    onSongInfo: () -> Unit,
    // opens select video once the card has gone, the mode hosts it so it stays when the menu leaves
    onPickVideo: () -> Unit,
) {
    val connection = LocalPlayerConnection.current ?: return
    val track by connection.uiState.trackInfo.collectAsState()
    val (quality) = rememberEnumPreference(VideoQualityKey, VideoQuality.Q1080)
    val (wholeVideo, setWholeVideo) = rememberPreference(VideoModeWholeVideoKey, defaultValue = false)
    var showQuality by remember { mutableStateOf(false) }
    var showProvider by remember { mutableStateOf(false) }
    var showOffset by remember { mutableStateOf(false) }
    var showTranslation by remember { mutableStateOf(false) }
    var showSpeed by remember { mutableStateOf(false) }
    var showSleep by remember { mutableStateOf(false) }
    // what to do once the card has slid away, so leaving the mode or opening the pane doesn't cut its exit short
    var afterClose by remember { mutableStateOf<(() -> Unit)?>(null) }
    val stack = rememberMenuSheetStack()

    CompositionLocalProvider(
        LocalMenuSheetFloating provides true,
        LocalMenuSheetStack provides stack,
    ) {
        MenuSheetSurface(
            onDismiss = {
                onDismiss()
                afterClose?.invoke()
            },
        ) { _ ->
            val close = LocalMenuSheetClose.current
            fun closeThen(action: () -> Unit) {
                afterClose = action
                close()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(top = 6.dp, bottom = 18.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = track.thumbnailUrl?.resize(240, 240),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f)),
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontFamily = InterFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = track.artist,
                            color = Color(0xFFB3B3B3),
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    PressScaleActionButton(
                        icon = if (track.liked) R.drawable.ic_untitled_heart else R.drawable.ic_untitled_heart_unfill,
                        tint = Color.White,
                        containerColor = Color.White.copy(alpha = if (track.liked) 0.3f else 0.15f),
                        onClick = connection::toggleLike,
                    )
                }

                Spacer(modifier = Modifier.height(SheetHeaderGap))

                SheetActionCardRow {
                    SheetActionCard(
                        icon = R.drawable.fullscreen_exit,
                        title = stringResource(R.string.video_mode_exit),
                        onClick = { closeThen(onExitVideoMode) },
                        modifier = Modifier.weight(1f),
                    )
                    SheetActionCard(
                        icon = R.drawable.playlist_add,
                        title = stringResource(R.string.add_to_playlist),
                        onClick = { closeThen(onAddToPlaylist) },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                MenuRow(
                    icon = R.drawable.info,
                    title = stringResource(R.string.video_mode_song_info),
                    onClick = { closeThen(onSongInfo) },
                )

                Spacer(modifier = Modifier.height(16.dp))
                MenuRow(
                    icon = R.drawable.tune,
                    title = stringResource(R.string.video_quality),
                    onClick = { showQuality = true },
                    trailing = { TrailingValue(qualityLabel(quality, session)) },
                )
                Spacer(modifier = Modifier.height(8.dp))
                MenuRow(
                    icon = R.drawable.fullscreen_exit,
                    title = stringResource(R.string.video_mode_whole_video),
                    onClick = { setWholeVideo(!wholeVideo) },
                    trailing = { AppSwitch(checked = wholeVideo, onCheckedChange = setWholeVideo) },
                )
                Spacer(modifier = Modifier.height(8.dp))
                // a playing video can be swapped for the right one, a song with none can have one found
                MenuRow(
                    icon = R.drawable.slow_motion_video,
                    title = stringResource(if (session != null) R.string.video_wrong else R.string.video_find),
                    onClick = { closeThen(onPickVideo) },
                )

                Spacer(modifier = Modifier.height(16.dp))
                MenuRow(
                    icon = R.drawable.lyrics,
                    title = stringResource(R.string.video_mode_lyrics),
                    onClick = { closeThen { onOpenPane(VideoPane.LYRICS) } },
                )
                Spacer(modifier = Modifier.height(8.dp))
                MenuRow(
                    icon = R.drawable.queue_music,
                    title = stringResource(R.string.video_mode_queue),
                    onClick = { closeThen { onOpenPane(VideoPane.QUEUE) } },
                )

                Spacer(modifier = Modifier.height(16.dp))
                MenuRow(
                    icon = R.drawable.lyrics,
                    title = stringResource(R.string.video_mode_lyrics_provider),
                    onClick = { showProvider = true },
                )
                Spacer(modifier = Modifier.height(8.dp))
                MenuRow(
                    icon = R.drawable.fast_forward,
                    title = stringResource(R.string.video_mode_lyrics_offset),
                    onClick = { showOffset = true },
                )
                Spacer(modifier = Modifier.height(8.dp))
                MenuRow(
                    icon = R.drawable.translate,
                    title = stringResource(R.string.video_mode_lyrics_translation),
                    onClick = { showTranslation = true },
                )

                Spacer(modifier = Modifier.height(16.dp))
                MenuRow(
                    icon = R.drawable.speed,
                    title = stringResource(R.string.video_mode_speed),
                    onClick = { showSpeed = true },
                )
                Spacer(modifier = Modifier.height(8.dp))
                MenuRow(
                    icon = R.drawable.sleep_timer,
                    title = stringResource(R.string.sleep_timer),
                    onClick = { showSleep = true },
                )
            }
        }

        if (showQuality) VideoQualitySheet(session = session, onDismiss = { showQuality = false })
        if (showProvider) LyricsProviderSheet(onDismiss = { showProvider = false })
        if (showOffset) LyricsOffsetSheet(onDismiss = { showOffset = false })
        if (showTranslation) LyricsTranslationSheet(onDismiss = { showTranslation = false })
        if (showSpeed) PlaybackSpeedSheet(onDismiss = { showSpeed = false })
        if (showSleep) SleepTimerSheet(onDismiss = { showSleep = false })
    }
}

@Composable
private fun TrailingValue(text: String) {
    Text(
        text = text,
        color = Color(0xFF9A9A9A),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

// what the quality reads as: "Auto · 720p" while auto is playing a size, otherwise the size itself
@Composable
internal fun qualityLabel(quality: VideoQuality, session: YouTubeVideoSession?): String {
    if (quality != VideoQuality.AUTO) return videoSizeName(quality.shortSide)
    val playing = session?.playingShortSide ?: 0
    return if (playing > 0) {
        stringResource(R.string.video_quality_auto_now, videoSizeName(playing))
    } else {
        stringResource(R.string.video_quality_auto)
    }
}

private data class QualityOptionUi(
    val quality: VideoQuality,
    val title: String,
    val subtitle: String,
    val badge: String? = null,
    val showFlame: Boolean = false,
)

private fun qualityOptionInfo(option: VideoQuality): QualityOptionUi = when (option) {
    VideoQuality.AUTO -> QualityOptionUi(
        quality = option,
        title = "Auto",
        subtitle = "Adjusts to your connection",
    )
    VideoQuality.Q2160 -> QualityOptionUi(
        quality = option,
        title = "4K",
        subtitle = "Sharpest",
        badge = "Ultra HD",
        showFlame = true,
    )
    VideoQuality.Q1440 -> QualityOptionUi(
        quality = option,
        title = "1440p",
        subtitle = "Very sharp",
        badge = "2K",
    )
    VideoQuality.Q1080 -> QualityOptionUi(
        quality = option,
        title = "1080p",
        subtitle = "Recommended",
        badge = "Default",
    )
    VideoQuality.Q720 -> QualityOptionUi(
        quality = option,
        title = "720p",
        subtitle = "Balanced",
    )
    VideoQuality.Q480 -> QualityOptionUi(
        quality = option,
        title = "480p",
        subtitle = "Saves data",
    )
    VideoQuality.Q360 -> QualityOptionUi(
        quality = option,
        title = "360p",
        subtitle = "Low data",
    )
    VideoQuality.Q240 -> QualityOptionUi(
        quality = option,
        title = "240p",
        subtitle = "Very low data",
    )
    VideoQuality.Q144 -> QualityOptionUi(
        quality = option,
        title = "144p",
        subtitle = "Lowest data",
    )
}

// auto, then every size this video really comes in (all of them until it has loaded)
@Composable
internal fun VideoQualitySheet(session: YouTubeVideoSession?, onDismiss: () -> Unit) {
    val (quality, setQuality) = rememberEnumPreference(VideoQualityKey, VideoQuality.Q1080)
    val sizes = session?.sizes.orEmpty()
    val options = remember(sizes) {
        listOf(VideoQuality.AUTO) + VideoQuality.entries.filter { option ->
            option != VideoQuality.AUTO && (sizes.isEmpty() || option.shortSide in sizes)
        }
    }
    val coroutineScope = rememberCoroutineScope()
    var selectedQuality by remember(quality) { mutableStateOf(quality) }

    MenuSheetSurface(onDismiss = onDismiss, wrapHeight = true) { _ ->
        val close = LocalMenuSheetClose.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 8.dp, bottom = 22.dp),
        ) {
            // M3 handle bar
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Header title & subtitle
            Text(
                text = stringResource(R.string.video_quality),
                color = Color.White,
                fontFamily = InterFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.3).sp,
                modifier = Modifier.padding(start = 2.dp),
            )
            Text(
                text = "Applies to this and all upcoming videos",
                color = Color.White.copy(alpha = 0.55f),
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(start = 2.dp, top = 2.dp, bottom = 14.dp),
            )

            // Quality rows list
            for (option in options) {
                val info = remember(option) { qualityOptionInfo(option) }
                val isSelected = option == selectedQuality
                QualityRow(
                    info = info,
                    selected = isSelected,
                    onClick = {
                        selectedQuality = option
                        setQuality(option)
                        session?.setQuality(option, 2160)
                        coroutineScope.launch {
                            delay(260)
                            close()
                        }
                    },
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (sizes.isEmpty()) {
                Text(
                    text = stringResource(R.string.video_quality_not_loaded),
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun QualityRow(
    info: QualityOptionUi,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (selected) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f)
    val borderModifier = if (selected) {
        Modifier.border(BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)), RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(borderModifier)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = RemoteFocus,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = info.title,
                    color = Color.White,
                    fontFamily = InterFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                if (info.showFlame) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.flame_4k))
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(22.dp),
                    )
                }
                if (info.badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = info.badge,
                            color = Color.White.copy(alpha = 0.75f),
                            fontFamily = InterFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
            Text(
                text = info.subtitle,
                color = Color.White.copy(alpha = 0.5f),
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        this@Row.AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ),
            exit = fadeOut() + scaleOut(),
        ) {
            Icon(
                painter = painterResource(R.drawable.check),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
