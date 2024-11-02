package com.shaadow.tunes.ui.screens.artist

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
import com.shaadow.tunes.models.ActionInfo
import com.shaadow.tunes.models.LocalMenuState
import com.shaadow.tunes.models.Song
import com.shaadow.tunes.ui.components.CoverScaffold
import com.shaadow.tunes.ui.components.ShimmerHost
import com.shaadow.tunes.ui.components.themed.NonQueuedMediaItemMenu
import com.shaadow.tunes.ui.items.ListItemPlaceholder
import com.shaadow.tunes.ui.items.LocalSongItem
import com.shaadow.tunes.utils.asMediaItem
import com.shaadow.tunes.utils.enqueue
import com.shaadow.tunes.utils.forcePlayAtIndex
import com.shaadow.tunes.utils.forcePlayFromBeginning

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun ArtistLocalSongs(
    browseId: String,
    thumbnailContent: @Composable () -> Unit,
    onGoToAlbum: (String) -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    var songs: List<Song>? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        Database.artistSongs(browseId).collect { songs = it }
    }

    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "thumbnail") {
            CoverScaffold(
                primaryButton = ActionInfo(
                    enabled = !songs.isNullOrEmpty(),
                    onClick = {
                        songs?.let { songs ->
                            if (songs.isNotEmpty()) {
                                binder?.stopRadio()
                                binder?.player?.forcePlayFromBeginning(
                                    songs.shuffled().map(Song::asMediaItem)
                                )
                            }
                        }
                    },
                    icon = Icons.Outlined.Shuffle,
                    description = R.string.shuffle
                ),
                secondaryButton = ActionInfo(
                    enabled = !songs.isNullOrEmpty(),
                    onClick = {
                        binder?.player?.enqueue(songs!!.map(Song::asMediaItem))
                    },
                    icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                    description = R.string.enqueue
                ),
                content = thumbnailContent
            )
        }

        item(key = "spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }

        songs?.let { songs ->
            itemsIndexed(
                items = songs,
                key = { _, song -> song.id }
            ) { index, song ->
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
                            NonQueuedMediaItemMenu(
                                onDismiss = menuState::hide,
                                mediaItem = song.asMediaItem,
                                onGoToAlbum = onGoToAlbum
                            )
                        }
                    }
                )
            }
        } ?: item(key = "loading") {
            ShimmerHost {
                repeat(4) {
                    ListItemPlaceholder()
                }
            }
        }
    }
}