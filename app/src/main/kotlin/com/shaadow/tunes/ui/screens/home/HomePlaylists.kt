package com.shaadow.tunes.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.Database
import com.shaadow.tunes.R
import com.shaadow.tunes.enums.BuiltInPlaylist
import com.shaadow.tunes.enums.PlaylistSortBy
import com.shaadow.tunes.enums.SortOrder
import com.shaadow.tunes.models.Playlist
import com.shaadow.tunes.models.PlaylistPreview
import com.shaadow.tunes.query
import com.shaadow.tunes.ui.components.themed.TextFieldDialog
import com.shaadow.tunes.ui.items.BuiltInPlaylistItem
import com.shaadow.tunes.ui.items.LocalPlaylistItem
import com.shaadow.tunes.utils.playlistSortByKey
import com.shaadow.tunes.utils.playlistSortOrderKey
import com.shaadow.tunes.utils.rememberPreference

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun HomePlaylists(
    onBuiltInPlaylist: (Int) -> Unit,
    onPlaylistClick: (Playlist) -> Unit
) {
    var isCreatingANewPlaylist by rememberSaveable { mutableStateOf(false) }
    var sortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.Name)
    var sortOrder by rememberPreference(playlistSortOrderKey, SortOrder.Ascending)
    var items: List<PlaylistPreview> by remember { mutableStateOf(emptyList()) }
    var isSorting by rememberSaveable { mutableStateOf(false) }
    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0F else 180F,
        label = "rotation"
    )

    LaunchedEffect(sortBy, sortOrder) {
        Database.playlistPreviews(sortBy, sortOrder).collect { items = it }
    }

    if (isCreatingANewPlaylist) {
        TextFieldDialog(
            title = stringResource(id = R.string.new_playlist),
            hintText = stringResource(id = R.string.playlist_name_hint),
            onDismiss = {
                isCreatingANewPlaylist = false
            },
            onDone = { text ->
                query {
                    Database.insert(Playlist(name = text))
                }
            }
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(
            key = "header",
            span = { GridItemSpan(maxLineSpan) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { isSorting = true }
                ) {
                    Text(
                        text = when (sortBy) {
                            PlaylistSortBy.Name -> stringResource(id = R.string.name)
                            PlaylistSortBy.DateAdded -> stringResource(id = R.string.date_added)
                            PlaylistSortBy.SongCount -> stringResource(id = R.string.song_count)
                        }
                    )
                }

                IconButton(
                    onClick = { sortOrder = !sortOrder },
                    modifier = Modifier.graphicsLayer { rotationZ = sortOrderIconRotation }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.weight(1F))

                Text(
                    text =
                    if (items.size == 1) "1 ${stringResource(id = R.string.playlist).lowercase()}"
                    else "${items.size} ${stringResource(id = R.string.playlists).lowercase()}",
                    style = MaterialTheme.typography.labelLarge
                )

                DropdownMenu(
                    expanded = isSorting,
                    onDismissRequest = { isSorting = false }
                ) {
                    PlaylistSortBy.entries.forEach { entry ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = when (entry) {
                                        PlaylistSortBy.Name -> stringResource(id = R.string.name)
                                        PlaylistSortBy.DateAdded -> stringResource(id = R.string.date_added)
                                        PlaylistSortBy.SongCount -> stringResource(id = R.string.song_count)
                                    }
                                )
                            },
                            onClick = {
                                isSorting = false
                                sortBy = entry
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = entry.icon,
                                    contentDescription = entry.name
                                )
                            },
                            trailingIcon = {
                                RadioButton(
                                    selected = sortBy == entry,
                                    onClick = { sortBy = entry }
                                )
                            }
                        )
                    }
                }
            }
        }

        item(key = "favorites") {
            BuiltInPlaylistItem(
                icon = Icons.Default.Favorite,
                name = stringResource(id = R.string.favorites),
                onClick = { onBuiltInPlaylist(BuiltInPlaylist.Favorites.ordinal) }
            )
        }

        item(key = "offline") {
            BuiltInPlaylistItem(
                icon = Icons.Default.DownloadForOffline,
                name = stringResource(id = R.string.offline),
                onClick = { onBuiltInPlaylist(BuiltInPlaylist.Offline.ordinal) }
            )
        }

        item(key = "new") {
            BuiltInPlaylistItem(
                icon = Icons.Default.Add,
                name = stringResource(id = R.string.new_playlist),
                onClick = { isCreatingANewPlaylist = true }
            )
        }

        items(items = items, key = { it.playlist.id }) { playlistPreview ->
            LocalPlaylistItem(
                modifier = Modifier.animateItem(),
                playlist = playlistPreview,
                onClick = { onPlaylistClick(playlistPreview.playlist) }
            )
        }
    }
}