package com.shaadow.tunes.ui.screens.builtinplaylist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.shaadow.tunes.R
import com.shaadow.tunes.enums.BuiltInPlaylist
import com.shaadow.tunes.ui.components.consumeCustomWindowInsets

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun BuiltInPlaylistScreen(
    builtInPlaylist: BuiltInPlaylist,
    pop: () -> Unit,
    onGoToAlbum: (String) -> Unit,
    onGoToArtist: (String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (builtInPlaylist) {
                            BuiltInPlaylist.Favorites -> stringResource(id = R.string.favorites)
                            BuiltInPlaylist.Offline -> stringResource(id = R.string.offline)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = pop) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .consumeCustomWindowInsets(paddingValues)
        ) {
            BuiltInPlaylistSongs(
                builtInPlaylist = builtInPlaylist,
                onGoToAlbum = onGoToAlbum,
                onGoToArtist = onGoToArtist
            )
        }
    }
}