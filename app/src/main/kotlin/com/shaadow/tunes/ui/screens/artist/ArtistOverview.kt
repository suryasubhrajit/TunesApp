package com.shaadow.tunes.ui.screens.artist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.models.NavigationEndpoint
import com.shaadow.tunes.LocalPlayerServiceBinder
import com.shaadow.tunes.R
import com.shaadow.tunes.models.ActionInfo
import com.shaadow.tunes.models.LocalMenuState
import com.shaadow.tunes.ui.components.CoverScaffold
import com.shaadow.tunes.ui.components.ShimmerHost
import com.shaadow.tunes.ui.components.themed.NonQueuedMediaItemMenu
import com.shaadow.tunes.ui.components.themed.TextPlaceholder
import com.shaadow.tunes.ui.items.AlbumItem
import com.shaadow.tunes.ui.items.ItemPlaceholder
import com.shaadow.tunes.ui.items.ListItemPlaceholder
import com.shaadow.tunes.ui.items.SongItem
import com.shaadow.tunes.ui.styling.Dimensions
import com.shaadow.tunes.utils.asMediaItem
import com.shaadow.tunes.utils.forcePlay

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun ArtistOverview(
    youtubeArtistPage: Innertube.ArtistPage?,
    onViewAllSongsClick: () -> Unit,
    onViewAllAlbumsClick: () -> Unit,
    onViewAllSinglesClick: () -> Unit,
    onAlbumClick: (String) -> Unit,
    thumbnailContent: @Composable () -> Unit,
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val itemSize = 140.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val radioEndpoint = youtubeArtistPage?.radioEndpoint
        val shuffleEndpoint = youtubeArtistPage?.shuffleEndpoint

        CoverScaffold(
            primaryButton = ActionInfo(
                enabled = radioEndpoint != null,
                onClick = {
                    binder?.stopRadio()
                    binder?.playRadio(radioEndpoint)
                },
                icon = Icons.Outlined.Podcasts,
                description = R.string.start_radio
            ),
            secondaryButton = ActionInfo(
                enabled = shuffleEndpoint != null,
                onClick = {
                    binder?.stopRadio()
                    binder?.playRadio(shuffleEndpoint)
                },
                icon = Icons.Outlined.Shuffle,
                description = R.string.shuffle
            ),
            content = thumbnailContent
        )

        if (youtubeArtistPage != null) {
            Spacer(modifier = Modifier.height(Dimensions.spacer))

            youtubeArtistPage.songs?.let { songs ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.songs),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    youtubeArtistPage.songsEndpoint?.let {
                        TextButton(onClick = onViewAllSongsClick) {
                            Text(text = stringResource(id = R.string.view_all))
                        }
                    }
                }

                songs.forEach { song ->
                    SongItem(
                        song = song,
                        onClick = {
                            val mediaItem = song.asMediaItem
                            binder?.stopRadio()
                            binder?.player?.forcePlay(mediaItem)
                            binder?.setupRadio(
                                NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                            )
                        },
                        onLongClick = {
                            menuState.display {
                                NonQueuedMediaItemMenu(
                                    onDismiss = menuState::hide,
                                    mediaItem = song.asMediaItem,
                                    onGoToAlbum = onAlbumClick
                                )
                            }
                        }
                    )
                }
            }

            youtubeArtistPage.albums?.let { albums ->
                Spacer(modifier = Modifier.height(Dimensions.spacer))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.albums),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    youtubeArtistPage.albumsEndpoint?.let {
                        TextButton(onClick = onViewAllAlbumsClick) {
                            Text(text = stringResource(id = R.string.view_all))
                        }
                    }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(
                        items = albums,
                        key = Innertube.AlbumItem::key
                    ) { album ->
                        AlbumItem(
                            modifier = Modifier.widthIn(max = itemSize),
                            album = album,
                            onClick = { onAlbumClick(album.key) }
                        )
                    }
                }
            }

            youtubeArtistPage.singles?.let { singles ->
                Spacer(modifier = Modifier.height(Dimensions.spacer))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.singles),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    youtubeArtistPage.singlesEndpoint?.let {
                        TextButton(onClick = onViewAllSinglesClick) {
                            Text(text = stringResource(id = R.string.view_all))
                        }

                    }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(
                        items = singles,
                        key = Innertube.AlbumItem::key
                    ) { album ->
                        AlbumItem(
                            modifier = Modifier.widthIn(max = itemSize),
                            album = album,
                            onClick = { onAlbumClick(album.key) }
                        )
                    }
                }
            }

            youtubeArtistPage.description?.let { description ->
                val attributionsIndex = description.lastIndexOf("\n\nFrom Wikipedia")

                Spacer(modifier = Modifier.height(Dimensions.spacer))

                Text(
                    text = stringResource(id = R.string.about),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Text(
                    text = if (attributionsIndex == -1) {
                        description
                    } else {
                        description.substring(0, attributionsIndex)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .alpha(Dimensions.mediumOpacity)
                )

                if (attributionsIndex != -1) {
                    Text(
                        text = "From Wikipedia under Creative Commons Attribution CC-BY-SA 3.0",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .alpha(Dimensions.mediumOpacity)
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                    )
                }
            }
        } else {
            ShimmerHost(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                val placeholderModifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)

                TextPlaceholder(modifier = placeholderModifier)

                repeat(5) {
                    ListItemPlaceholder()
                }

                Spacer(modifier = Modifier.height(Dimensions.spacer))

                repeat(2) {
                    TextPlaceholder(modifier = placeholderModifier)

                    Row {
                        repeat(2) {
                            ItemPlaceholder(modifier = Modifier.widthIn(max = itemSize))
                        }
                    }
                }
            }
        }
    }
}
