package com.shaadow.tunes.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.models.NavigationEndpoint
import com.shaadow.tunes.Database
import com.shaadow.tunes.LocalPlayerServiceBinder
import com.shaadow.tunes.R
import com.shaadow.tunes.enums.QuickPicksSource
import com.shaadow.tunes.models.LocalMenuState
import com.shaadow.tunes.models.QuickPicksViewModel
import com.shaadow.tunes.query
import com.shaadow.tunes.ui.components.ShimmerHost
import com.shaadow.tunes.ui.components.themed.NonQueuedMediaItemMenu
import com.shaadow.tunes.ui.components.themed.TextPlaceholder
import com.shaadow.tunes.ui.items.AlbumItem
import com.shaadow.tunes.ui.items.ArtistItem
import com.shaadow.tunes.ui.items.ItemPlaceholder
import com.shaadow.tunes.ui.items.ListItemPlaceholder
import com.shaadow.tunes.ui.items.LocalSongItem
import com.shaadow.tunes.ui.items.PlaylistItem
import com.shaadow.tunes.ui.items.SongItem
import com.shaadow.tunes.ui.styling.Dimensions
import com.shaadow.tunes.utils.SnapLayoutInfoProvider
import com.shaadow.tunes.utils.asMediaItem
import com.shaadow.tunes.utils.forcePlay
import com.shaadow.tunes.utils.isLandscape
import com.shaadow.tunes.utils.quickPicksSourceKey
import com.shaadow.tunes.utils.rememberPreference

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun QuickPicks(
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    val viewModel: QuickPicksViewModel = viewModel()
    val quickPicksSource by rememberPreference(quickPicksSourceKey, QuickPicksSource.Trending)

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val itemSize = 108.dp + 2 * 8.dp
    val quickPicksLazyGridState = rememberLazyGridState()
    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(bottom = 8.dp)

    LaunchedEffect(quickPicksSource) {
        viewModel.loadQuickPicks(quickPicksSource)
    }

    BoxWithConstraints {
        val quickPicksLazyGridItemWidthFactor =
            if (isLandscape && maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f

        val density = LocalDensity.current

        val snapLayoutInfoProvider = remember(quickPicksLazyGridState) {
            with(density) {
                SnapLayoutInfoProvider(
                    lazyGridState = quickPicksLazyGridState,
                    positionInLayout = { layoutSize, itemSize ->
                        (layoutSize * quickPicksLazyGridItemWidthFactor / 2f - itemSize / 2f)
                    }
                )
            }
        }

        val itemInHorizontalGridWidth = maxWidth * quickPicksLazyGridItemWidthFactor

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 4.dp, bottom = 16.dp)
        ) {
            viewModel.relatedPageResult?.getOrNull()?.let { related ->
                Text(
                    text = stringResource(id = R.string.quick_picks),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = sectionTextModifier
                )

                LazyHorizontalGrid(
                    state = quickPicksLazyGridState,
                    rows = GridCells.Fixed(count = 4),
                    flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((songThumbnailSizeDp + Dimensions.itemsVerticalPadding * 2) * 4)
                ) {
                    viewModel.trending?.let { song ->
                        item {
                            LocalSongItem(
                                modifier = Modifier
                                    .animateItem()
                                    .width(itemInHorizontalGridWidth),
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
                                            onRemoveFromQuickPicks = {
                                                query {
                                                    Database.clearEventsFor(song.id)
                                                }
                                            },
                                            onGoToAlbum = onAlbumClick,
                                            onGoToArtist = onArtistClick
                                        )
                                    }
                                }
                            )
                        }
                    }

                    items(
                        items = related.songs?.dropLast(if (viewModel.trending == null) 0 else 1)
                            ?: emptyList(),
                        key = Innertube.SongItem::key
                    ) { song ->
                        SongItem(
                            modifier = Modifier
                                .animateItem()
                                .width(itemInHorizontalGridWidth),
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
                                        onGoToAlbum = onAlbumClick,
                                        onGoToArtist = onArtistClick
                                    )
                                }
                            }
                        )
                    }
                }

                related.albums?.let { albums ->
                    Spacer(modifier = Modifier.height(Dimensions.spacer))

                    Text(
                        text = stringResource(id = R.string.related_albums),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = sectionTextModifier
                    )

                    LazyRow(
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

                related.artists?.let { artists ->
                    Spacer(modifier = Modifier.height(Dimensions.spacer))

                    Text(
                        text = stringResource(id = R.string.similar_artists),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = sectionTextModifier
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(
                            items = artists,
                            key = Innertube.ArtistItem::key,
                        ) { artist ->
                            ArtistItem(
                                modifier = Modifier.widthIn(max = itemSize),
                                artist = artist,
                                onClick = { onArtistClick(artist.key) }
                            )
                        }
                    }
                }

                related.playlists?.let { playlists ->
                    Spacer(modifier = Modifier.height(Dimensions.spacer))

                    Text(
                        text = stringResource(id = R.string.recommended_playlists),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = sectionTextModifier
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(
                            items = playlists,
                            key = Innertube.PlaylistItem::key,
                        ) { playlist ->
                            PlaylistItem(
                                modifier = Modifier.widthIn(max = itemSize),
                                playlist = playlist,
                                onClick = { onPlaylistClick(playlist.key) }
                            )
                        }
                    }
                }

                Unit
            } ?: viewModel.relatedPageResult?.exceptionOrNull()?.let {
                Text(
                    text = stringResource(id = R.string.home_error),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(all = 16.dp)
                        .alpha(Dimensions.mediumOpacity)
                )
            } ?: ShimmerHost {
                TextPlaceholder(modifier = sectionTextModifier)

                repeat(4) {
                    ListItemPlaceholder()
                }

                Spacer(modifier = Modifier.height(Dimensions.spacer))

                TextPlaceholder(modifier = sectionTextModifier)

                Row(
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    repeat(2) {
                        ItemPlaceholder(modifier = Modifier.widthIn(max = itemSize))
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spacer))

                TextPlaceholder(modifier = sectionTextModifier)

                Row(
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    repeat(2) {
                        ItemPlaceholder(
                            modifier = Modifier.widthIn(max = itemSize),
                            shape = CircleShape
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spacer))

                TextPlaceholder(modifier = sectionTextModifier)

                Row(
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    repeat(2) {
                        ItemPlaceholder(modifier = Modifier.widthIn(max = itemSize))
                    }
                }
            }
        }
    }
}
