package com.shaadow.tunes.ui.screens.album

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.models.bodies.BrowseBody
import com.shaadow.innertube.requests.albumPage
import com.shaadow.tunes.Database
import com.shaadow.tunes.R
import com.shaadow.tunes.models.Album
import com.shaadow.tunes.models.Section
import com.shaadow.tunes.models.SongAlbumMap
import com.shaadow.tunes.query
import com.shaadow.tunes.ui.components.TabScaffold
import com.shaadow.tunes.ui.components.TooltipIconButton
import com.shaadow.tunes.ui.components.themed.adaptiveThumbnailContent
import com.shaadow.tunes.ui.items.AlbumItem
import com.shaadow.tunes.ui.items.ItemPlaceholder
import com.shaadow.tunes.ui.screens.search.ItemsPage
import com.shaadow.tunes.utils.asMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun AlbumScreen(
    browseId: String,
    pop: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onGoToArtist: (String) -> Unit
) {
    var album: Album? by remember { mutableStateOf(null) }
    var albumPage: Innertube.PlaylistOrAlbumPage? by remember { mutableStateOf(null) }

    val tabs = listOf(
        Section(stringResource(id = R.string.songs), Icons.Outlined.MusicNote),
        Section(stringResource(id = R.string.other_versions), Icons.Outlined.Album)
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    LaunchedEffect(Unit) {
        Database
            .album(browseId)
            .combine(snapshotFlow { pagerState.currentPage }) { album, tabIndex -> album to tabIndex }
            .collect { (currentAlbum, tabIndex) ->
                album = currentAlbum

                if (albumPage == null && (currentAlbum?.timestamp == null || tabIndex == 1)) {
                    withContext(Dispatchers.IO) {
                        Innertube.albumPage(BrowseBody(browseId = browseId))
                            ?.onSuccess { currentAlbumPage ->
                                albumPage = currentAlbumPage

                                Database.clearAlbum(browseId)

                                Database.upsert(
                                    Album(
                                        id = browseId,
                                        title = currentAlbumPage.title,
                                        thumbnailUrl = currentAlbumPage.thumbnail?.url,
                                        year = currentAlbumPage.year,
                                        authorsText = currentAlbumPage.authors
                                            ?.joinToString("") { it.name ?: "" },
                                        shareUrl = currentAlbumPage.url,
                                        timestamp = System.currentTimeMillis(),
                                        bookmarkedAt = album?.bookmarkedAt
                                    ),
                                    currentAlbumPage
                                        .songsPage
                                        ?.items
                                        ?.map(Innertube.SongItem::asMediaItem)
                                        ?.onEach(Database::insert)
                                        ?.mapIndexed { position, mediaItem ->
                                            SongAlbumMap(
                                                songId = mediaItem.mediaId,
                                                albumId = browseId,
                                                position = position
                                            )
                                        } ?: emptyList()
                                )
                            }
                    }
                }
            }
    }

    val thumbnailContent =
        adaptiveThumbnailContent(album?.timestamp == null, album?.thumbnailUrl)

    TabScaffold(
        pagerState = pagerState,
        topIconButtonId = Icons.AutoMirrored.Outlined.ArrowBack,
        onTopIconButtonClick = pop,
        sectionTitle = album?.title ?: "",
        appBarActions = {
            val context = LocalContext.current

            TooltipIconButton(
                description = if (album?.bookmarkedAt == null) R.string.add_bookmark else R.string.remove_bookmark,
                onClick = {
                    val bookmarkedAt =
                        if (album?.bookmarkedAt == null) System.currentTimeMillis() else null

                    query {
                        album
                            ?.copy(bookmarkedAt = bookmarkedAt)
                            ?.let(Database::update)
                    }
                },
                icon = if (album?.bookmarkedAt == null) Icons.Outlined.BookmarkAdd else Icons.Filled.Bookmark,
                inTopBar = true
            )

            TooltipIconButton(
                description = R.string.share,
                onClick = {
                    album?.shareUrl?.let { url ->
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, url)
                        }

                        context.startActivity(
                            Intent.createChooser(
                                sendIntent,
                                null
                            )
                        )
                    }
                },
                icon = Icons.Outlined.Share,
                inTopBar = true
            )
        },
        tabColumnContent = tabs
    ) { index ->
        when (index) {
            0 -> AlbumSongs(
                browseId = browseId,
                thumbnailContent = thumbnailContent,
                onGoToArtist = onGoToArtist
            )

            1 -> {
                ItemsPage(
                    tag = "album/$browseId/alternatives",
                    initialPlaceholderCount = 1,
                    continuationPlaceholderCount = 1,
                    emptyItemsText = stringResource(id = R.string.no_alternative_versions),
                    itemsPageProvider = albumPage?.let {
                        {
                            Result.success(
                                Innertube.ItemsPage(
                                    items = albumPage?.otherVersions,
                                    continuation = null
                                )
                            )
                        }
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
        }
    }
}