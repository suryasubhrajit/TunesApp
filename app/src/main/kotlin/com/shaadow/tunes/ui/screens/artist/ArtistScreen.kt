package com.shaadow.tunes.ui.screens.artist

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.models.bodies.BrowseBody
import com.shaadow.innertube.models.bodies.ContinuationBody
import com.shaadow.innertube.requests.itemsPage
import com.shaadow.innertube.utils.from
import com.shaadow.tunes.Database
import com.shaadow.tunes.LocalPlayerServiceBinder
import com.shaadow.tunes.R
import com.shaadow.tunes.models.ArtistViewModel
import com.shaadow.tunes.models.LocalMenuState
import com.shaadow.tunes.models.Section
import com.shaadow.tunes.query
import com.shaadow.tunes.ui.components.TabScaffold
import com.shaadow.tunes.ui.components.TooltipIconButton
import com.shaadow.tunes.ui.components.themed.NonQueuedMediaItemMenu
import com.shaadow.tunes.ui.components.themed.adaptiveThumbnailContent
import com.shaadow.tunes.ui.items.AlbumItem
import com.shaadow.tunes.ui.items.ItemPlaceholder
import com.shaadow.tunes.ui.items.ListItemPlaceholder
import com.shaadow.tunes.ui.items.SongItem
import com.shaadow.tunes.ui.screens.search.ItemsPage
import com.shaadow.tunes.utils.artistScreenTabIndexKey
import com.shaadow.tunes.utils.asMediaItem
import com.shaadow.tunes.utils.forcePlay
import com.shaadow.tunes.utils.rememberPreference
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun ArtistScreen(
    browseId: String,
    pop: () -> Unit,
    onAlbumClick: (String) -> Unit
) {
    val viewModel: ArtistViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val tabs = listOf(
        Section(stringResource(id = R.string.overview), Icons.Outlined.Person),
        Section(stringResource(id = R.string.songs), Icons.Outlined.MusicNote),
        Section(stringResource(id = R.string.albums), Icons.Outlined.Album),
        Section(stringResource(id = R.string.singles), Icons.Outlined.Album),
        Section(stringResource(id = R.string.library), Icons.Outlined.LibraryMusic)
    )
    var tabIndex by rememberPreference(artistScreenTabIndexKey, defaultValue = 0)
    val pagerState = rememberPagerState(
        initialPage = tabIndex,
        pageCount = { tabs.size }
    )

    LaunchedEffect(Unit) {
        viewModel.loadArtist(
            browseId = browseId,
            tabIndex = pagerState.currentPage
        )
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { index ->
            tabIndex = index
        }
    }

    val thumbnailContent = adaptiveThumbnailContent(
        isLoading = viewModel.artist?.timestamp == null,
        url = viewModel.artist?.thumbnailUrl
    )

    TabScaffold(
        pagerState = pagerState,
        topIconButtonId = Icons.AutoMirrored.Outlined.ArrowBack,
        onTopIconButtonClick = pop,
        sectionTitle = viewModel.artist?.name ?: "",
        appBarActions = {
            val context = LocalContext.current

            TooltipIconButton(
                description = if (viewModel.artist?.bookmarkedAt == null) R.string.add_bookmark else R.string.remove_bookmark,
                onClick = {
                    val bookmarkedAt =
                        if (viewModel.artist?.bookmarkedAt == null) System.currentTimeMillis() else null

                    query {
                        viewModel.artist
                            ?.copy(bookmarkedAt = bookmarkedAt)
                            ?.let(Database::update)
                    }
                },
                icon = if (viewModel.artist?.bookmarkedAt == null) Icons.Outlined.BookmarkAdd else Icons.Filled.Bookmark,
                inTopBar = true
            )

            TooltipIconButton(
                description = R.string.share,
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "https://music.youtube.com/channel/$browseId"
                        )
                    }

                    context.startActivity(Intent.createChooser(sendIntent, null))
                },
                icon = Icons.Outlined.Share,
                inTopBar = true
            )
        },
        tabColumnContent = tabs
    ) { index ->
        when (index) {
            0 -> ArtistOverview(
                youtubeArtistPage = viewModel.artistPage,
                thumbnailContent = thumbnailContent,
                onAlbumClick = { id -> onAlbumClick(id) },
                onViewAllSongsClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                onViewAllAlbumsClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                },
                onViewAllSinglesClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(3)
                    }
                },
            )

            1 -> {
                val binder = LocalPlayerServiceBinder.current
                val menuState = LocalMenuState.current

                ItemsPage(
                    tag = "artist/$browseId/songs",
                    itemsPageProvider = viewModel.artistPage?.let {
                        ({ continuation ->
                            continuation?.let {
                                Innertube.itemsPage(
                                    body = ContinuationBody(continuation = continuation),
                                    fromMusicResponsiveListItemRenderer = Innertube.SongItem::from,
                                )
                            } ?: viewModel.artistPage
                                ?.songsEndpoint
                                ?.takeIf { it.browseId != null }
                                ?.let { endpoint ->
                                    Innertube.itemsPage(
                                        body = BrowseBody(
                                            browseId = endpoint.browseId!!,
                                            params = endpoint.params,
                                        ),
                                        fromMusicResponsiveListItemRenderer = Innertube.SongItem::from,
                                    )
                                }
                            ?: Result.success(
                                Innertube.ItemsPage(
                                    items = viewModel.artistPage?.songs,
                                    continuation = null
                                )
                            )
                        })
                    },
                    itemContent = { song ->
                        SongItem(
                            song = song,
                            onClick = {
                                binder?.stopRadio()
                                binder?.player?.forcePlay(song.asMediaItem)
                                binder?.setupRadio(song.info?.endpoint)
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
                    },
                    itemPlaceholderContent = {
                        ListItemPlaceholder()
                    }
                )
            }

            2 -> {
                ItemsPage(
                    tag = "artist/$browseId/albums",
                    emptyItemsText = stringResource(id = R.string.no_albums_artist),
                    itemsPageProvider = viewModel.artistPage?.let {
                        ({ continuation ->
                            continuation?.let {
                                Innertube.itemsPage(
                                    body = ContinuationBody(continuation = continuation),
                                    fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from,
                                )
                            } ?: viewModel.artistPage
                                ?.albumsEndpoint
                                ?.takeIf { it.browseId != null }
                                ?.let { endpoint ->
                                    Innertube.itemsPage(
                                        body = BrowseBody(
                                            browseId = endpoint.browseId!!,
                                            params = endpoint.params,
                                        ),
                                        fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from,
                                    )
                                }
                            ?: Result.success(
                                Innertube.ItemsPage(
                                    items = viewModel.artistPage?.albums,
                                    continuation = null
                                )
                            )
                        })
                    },
                    itemContent = { album ->
                        AlbumItem(
                            album = album,
                            onClick = { onAlbumClick(album.key) }
                        )
                    },
                    itemPlaceholderContent = {
                        ItemPlaceholder()
                    }
                )
            }

            3 -> {
                ItemsPage(
                    tag = "artist/$browseId/singles",
                    emptyItemsText = stringResource(id = R.string.no_singles_artist),
                    itemsPageProvider = viewModel.artistPage?.let {
                        ({ continuation ->
                            continuation?.let {
                                Innertube.itemsPage(
                                    body = ContinuationBody(continuation = continuation),
                                    fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from,
                                )
                            } ?: viewModel.artistPage
                                ?.singlesEndpoint
                                ?.takeIf { it.browseId != null }
                                ?.let { endpoint ->
                                    Innertube.itemsPage(
                                        body = BrowseBody(
                                            browseId = endpoint.browseId!!,
                                            params = endpoint.params,
                                        ),
                                        fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from,
                                    )
                                }
                            ?: Result.success(
                                Innertube.ItemsPage(
                                    items = viewModel.artistPage?.singles,
                                    continuation = null
                                )
                            )
                        })
                    },
                    itemContent = { album ->
                        AlbumItem(
                            album = album,
                            onClick = { onAlbumClick(album.key) }
                        )
                    },
                    itemPlaceholderContent = {
                        ItemPlaceholder()
                    }
                )
            }

            4 -> ArtistLocalSongs(
                browseId = browseId,
                thumbnailContent = thumbnailContent,
                onGoToAlbum = onAlbumClick
            )
        }
    }
}