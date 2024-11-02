package com.shaadow.tunes.ui.screens.builtinplaylist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.Database
import com.shaadow.tunes.LocalPlayerServiceBinder
import com.shaadow.tunes.R
import com.shaadow.tunes.enums.BuiltInPlaylist
import com.shaadow.tunes.models.ActionInfo
import com.shaadow.tunes.models.LocalMenuState
import com.shaadow.tunes.models.Song
import com.shaadow.tunes.models.SongWithContentLength
import com.shaadow.tunes.ui.components.CoverScaffold
import com.shaadow.tunes.ui.components.SwipeToActionBox
import com.shaadow.tunes.ui.components.themed.InHistoryMediaItemMenu
import com.shaadow.tunes.ui.components.themed.NonQueuedMediaItemMenu
import com.shaadow.tunes.ui.items.LocalSongItem
import com.shaadow.tunes.utils.asMediaItem
import com.shaadow.tunes.utils.enqueue
import com.shaadow.tunes.utils.forcePlayAtIndex
import com.shaadow.tunes.utils.forcePlayFromBeginning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun BuiltInPlaylistSongs(
    builtInPlaylist: BuiltInPlaylist,
    onGoToAlbum: (String) -> Unit,
    onGoToArtist: (String) -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    var songs: List<Song> by remember { mutableStateOf(emptyList()) }

    LaunchedEffect(Unit) {
        when (builtInPlaylist) {
            BuiltInPlaylist.Favorites -> Database.favorites()

            BuiltInPlaylist.Offline -> Database
                .songsWithContentLength()
                .flowOn(Dispatchers.IO)
                .map { songs ->
                    songs.filter { song ->
                        song.contentLength?.let {
                            binder?.cache?.isCached(song.song.id, 0, song.contentLength)
                        } ?: false
                    }.map(SongWithContentLength::song)
                }
        }.collect { songs = it }
    }

    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "thumbnail") {
            CoverScaffold(
                primaryButton = ActionInfo(
                    enabled = songs.isNotEmpty(),
                    onClick = {
                        binder?.stopRadio()
                        binder?.player?.forcePlayFromBeginning(
                            songs.shuffled().map(Song::asMediaItem)
                        )
                    },
                    icon = Icons.Outlined.Shuffle,
                    description = R.string.shuffle
                ),
                secondaryButton = ActionInfo(
                    enabled = songs.isNotEmpty(),
                    onClick = {
                        binder?.player?.enqueue(songs.map(Song::asMediaItem))
                    },
                    icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                    description = R.string.enqueue
                ),
                content = {
                    BuiltInPlaylistThumbnail(builtInPlaylist = builtInPlaylist)
                }
            )
        }

        item(key = "spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }

        itemsIndexed(
            items = songs,
            key = { _, song -> song.id },
            contentType = { _, song -> song },
        ) { index, song ->
            SwipeToActionBox(
                modifier = Modifier.animateItem(),
                primaryAction = ActionInfo(
                    onClick = { binder?.player?.enqueue(song.asMediaItem) },
                    icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                    description = R.string.enqueue
                )
            ) {
                LocalSongItem(
                    song = song,
                    onClick = {
                        binder?.stopRadio()
                        binder?.player?.forcePlayAtIndex(
                            songs.map(Song::asMediaItem),
                            index
                        )
                    },
                    onLongClick = {
                        menuState.display {
                            when (builtInPlaylist) {
                                BuiltInPlaylist.Favorites -> NonQueuedMediaItemMenu(
                                    mediaItem = song.asMediaItem,
                                    onDismiss = menuState::hide,
                                    onGoToAlbum = onGoToAlbum,
                                    onGoToArtist = onGoToArtist
                                )

                                BuiltInPlaylist.Offline -> InHistoryMediaItemMenu(
                                    song = song,
                                    onDismiss = menuState::hide,
                                    onGoToAlbum = onGoToAlbum,
                                    onGoToArtist = onGoToArtist
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
