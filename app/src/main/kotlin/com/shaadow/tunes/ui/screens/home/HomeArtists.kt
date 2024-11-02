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
import com.shaadow.tunes.enums.ArtistSortBy
import com.shaadow.tunes.enums.SortOrder
import com.shaadow.tunes.models.Artist
import com.shaadow.tunes.ui.items.LocalArtistItem
import com.shaadow.tunes.utils.artistSortByKey
import com.shaadow.tunes.utils.artistSortOrderKey
import com.shaadow.tunes.utils.rememberPreference

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeArtistList(
    onArtistClick: (Artist) -> Unit
) {
    var sortBy by rememberPreference(artistSortByKey, ArtistSortBy.Name)
    var sortOrder by rememberPreference(artistSortOrderKey, SortOrder.Ascending)

    var items: List<Artist> by remember { mutableStateOf(emptyList()) }
    var isSorting by rememberSaveable { mutableStateOf(false) }
    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        label = "rotation"
    )

    LaunchedEffect(sortBy, sortOrder) {
        Database.artists(sortBy, sortOrder).collect { items = it }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
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
                            ArtistSortBy.Name -> stringResource(id = R.string.name)
                            ArtistSortBy.DateAdded -> stringResource(id = R.string.date_added)
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
                    if (items.size == 1) "1 ${stringResource(id = R.string.artist).lowercase()}"
                    else "${items.size} ${stringResource(id = R.string.artists).lowercase()}",
                    style = MaterialTheme.typography.labelLarge
                )

                DropdownMenu(
                    expanded = isSorting,
                    onDismissRequest = { isSorting = false }
                ) {
                    ArtistSortBy.entries.forEach { entry ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = when (entry) {
                                        ArtistSortBy.Name -> stringResource(id = R.string.name)
                                        ArtistSortBy.DateAdded -> stringResource(id = R.string.date_added)
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

        items(items = items, key = Artist::id) { artist ->
            LocalArtistItem(
                modifier = Modifier.animateItem(),
                artist = artist,
                onClick = { onArtistClick(artist) }
            )
        }
    }
}