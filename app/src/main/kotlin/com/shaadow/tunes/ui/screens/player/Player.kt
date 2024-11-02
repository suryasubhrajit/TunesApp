package com.shaadow.tunes.ui.screens.player

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.shaadow.innertube.models.NavigationEndpoint
import com.shaadow.tunes.Database
import com.shaadow.tunes.LocalPlayerServiceBinder
import com.shaadow.tunes.R
import com.shaadow.tunes.models.LocalMenuState
import com.shaadow.tunes.ui.components.TooltipIconButton
import com.shaadow.tunes.ui.components.themed.BaseMediaItemMenu
import com.shaadow.tunes.utils.DisposableListener
import com.shaadow.tunes.utils.formatAsDuration
import com.shaadow.tunes.utils.isLandscape
import com.shaadow.tunes.utils.positionAndDurationState
import com.shaadow.tunes.utils.seamlessPlay
import com.shaadow.tunes.utils.shouldBePlaying
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun Player(
    onGoToAlbum: (String) -> Unit,
    onGoToArtist: (String) -> Unit
) {
    val menuState = LocalMenuState.current
    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    var shouldBePlaying by remember { mutableStateOf(binder.player.shouldBePlaying) }
    var nullableMediaItem by remember {
        mutableStateOf(
            binder.player.currentMediaItem,
            neverEqualPolicy()
        )
    }

    binder.player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableMediaItem = mediaItem
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }
        }
    }

    val mediaItem = nullableMediaItem ?: return
    val positionAndDuration by binder.player.positionAndDurationState()
    val nextSongTitle =
        if (binder.player.hasNextMediaItem()) binder.player.getMediaItemAt(binder.player.nextMediaItemIndex).mediaMetadata.title.toString()
        else stringResource(id = R.string.open_queue)

    var artistId: String? by remember(mediaItem) {
        mutableStateOf(
            mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.let { artists ->
                if (artists.size == 1) artists.first()
                else null
            }
        )
    }

    var isShowingLyrics by rememberSaveable { mutableStateOf(false) }
    var isShowingStatsForNerds by rememberSaveable { mutableStateOf(false) }
    var isQueueOpen by rememberSaveable { mutableStateOf(false) }
    var isShowingSleepTimerDialog by rememberSaveable { mutableStateOf(false) }
    val sleepTimerMillisLeft by (binder.sleepTimerMillisLeft
        ?: flowOf(null))
        .collectAsState(initial = null)

    val queueState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(mediaItem) {
        withContext(Dispatchers.IO) {
            if (artistId == null) {
                val artistsInfo = Database.songArtistInfo(mediaItem.mediaId)
                if (artistsInfo.size == 1) artistId = artistsInfo.first().id
            }
        }
    }

    val thumbnailContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
        Thumbnail(
            isShowingLyrics = isShowingLyrics,
            onShowLyrics = { isShowingLyrics = it },
            isShowingStatsForNerds = isShowingStatsForNerds,
            onShowStatsForNerds = { isShowingStatsForNerds = it },
            modifier = modifier
        )
    }

    val controlsContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
        Controls(
            mediaId = mediaItem.mediaId,
            title = mediaItem.mediaMetadata.title?.toString().orEmpty(),
            artist = mediaItem.mediaMetadata.artist?.toString().orEmpty(),
            shouldBePlaying = shouldBePlaying,
            position = positionAndDuration.first,
            duration = positionAndDuration.second,
            onGoToArtist = artistId?.let {
                { onGoToArtist(it) }
            },
            modifier = modifier
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.weight(1F)
        ) {
            if (isLandscape) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 32.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(0.66f)
                            .padding(bottom = 16.dp)
                    ) {
                        thumbnailContent(
                            Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    controlsContent(
                        Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxHeight()
                            .weight(1f)
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 54.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1.25f)
                    ) {
                        thumbnailContent(
                            Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                        )
                    }

                    controlsContent(
                        Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(BottomSheetDefaults.ExpandedShape)
                .clickable { isQueueOpen = true }
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp))
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount < 0) isQueueOpen = true
                        }
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { isQueueOpen = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.PlaylistPlay,
                    contentDescription = null
                )
            }

            Text(
                text = nextSongTitle,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1F),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            TooltipIconButton(
                description = R.string.sleep_timer,
                onClick = { isShowingSleepTimerDialog = true },
                icon = if (sleepTimerMillisLeft == null) Icons.Outlined.Timer else Icons.Filled.Timer
            )

            IconButton(
                onClick = {
                    menuState.display {
                        BaseMediaItemMenu(
                            onDismiss = menuState::hide,
                            mediaItem = mediaItem,
                            onStartRadio = {
                                binder.stopRadio()
                                binder.player.seamlessPlay(mediaItem)
                                binder.setupRadio(NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId))
                            },
                            onGoToAlbum = onGoToAlbum,
                            onGoToArtist = onGoToArtist
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreHoriz,
                    contentDescription = null,
                )
            }
        }

        if (isShowingSleepTimerDialog) {
            if (sleepTimerMillisLeft != null) {
                AlertDialog(
                    onDismissRequest = { isShowingSleepTimerDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                binder.cancelSleepTimer()
                                isShowingSleepTimerDialog = false
                            }
                        ) {
                            Text(text = stringResource(id = R.string.stop))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { isShowingSleepTimerDialog = false }
                        ) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                    },
                    title = {
                        Text(text = stringResource(id = R.string.stop_sleep_timer_dialog))
                    },
                    text = {
                        sleepTimerMillisLeft?.let {
                            FlowColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(
                                    space = 8.dp,
                                    alignment = Alignment.CenterVertically
                                ),
                                horizontalArrangement = Arrangement.spacedBy(
                                    space = 8.dp,
                                    alignment = Alignment.CenterHorizontally
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(126.sp.value.dp)
                                        .border(
                                            width = 4.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = formatAsDuration(it),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                Button(
                                    onClick = {
                                        binder.startSleepTimer(it + 60 * 1000L)
                                        isShowingSleepTimerDialog = false
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text(text = "+1:00")
                                }
                            }
                        }
                    }
                )
            } else {
                var amount by remember { mutableIntStateOf(1) }

                AlertDialog(
                    onDismissRequest = { isShowingSleepTimerDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                binder.startSleepTimer(amount * 10 * 60 * 1000L)
                                isShowingSleepTimerDialog = false
                            },
                            enabled = amount > 0
                        ) {
                            Text(text = stringResource(id = R.string.set))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { isShowingSleepTimerDialog = false }
                        ) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                    },
                    title = {
                        Text(text = stringResource(id = R.string.set_sleep_timer))
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(
                                space = 16.dp,
                                alignment = Alignment.CenterHorizontally
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            FilledTonalIconButton(
                                onClick = { amount-- },
                                enabled = amount > 1
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Remove,
                                    contentDescription = null
                                )
                            }

                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${amount / 6}h ${(amount % 6) * 10}m",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            FilledTonalIconButton(
                                onClick = { amount++ },
                                enabled = amount < 60
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                )
            }
        }

        if (isQueueOpen) {
            ModalBottomSheet(
                onDismissRequest = { isQueueOpen = false },
                modifier = Modifier.fillMaxWidth(),
                sheetState = queueState,
                dragHandle = {
                    Surface(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Box(modifier = Modifier.size(width = 32.dp, height = 4.dp))
                    }
                }
            ) {
                Queue(
                    onGoToAlbum = onGoToAlbum,
                    onGoToArtist = onGoToArtist
                )
            }
        }
    }
}