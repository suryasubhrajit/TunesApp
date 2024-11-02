package com.shaadow.tunes.ui.screens.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScaffold(
    navController: NavController,
    sheetState: SheetState,
    scaffoldPadding: PaddingValues,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    BottomSheetScaffold(
        sheetContent = {
            AnimatedContent(
                targetState = sheetState.targetValue,
                label = "player",
                contentKey = { value ->
                    if (value == SheetValue.Expanded) 0 else 1
                }
            ) { value ->
                if (value == SheetValue.Expanded) {
                    Player(
                        onGoToAlbum = { browseId ->
                            scope.launch { sheetState.partialExpand() }
                            navController.navigate(route = "album/$browseId")
                        },
                        onGoToArtist = { browseId ->
                            scope.launch { sheetState.partialExpand() }
                            navController.navigate(route = "artist/$browseId")
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        MiniPlayer(
                            openPlayer = {
                                scope.launch { sheetState.expand() }
                            },
                            stopPlayer = {
                                scope.launch { sheetState.hide() }
                            }
                        )
                    }

                }
            }
        },
        scaffoldState = scaffoldState,
        sheetPeekHeight = 76.dp + 16.dp + scaffoldPadding.calculateBottomPadding(),
        sheetMaxWidth = Int.MAX_VALUE.dp,
        sheetDragHandle = {
            Surface(
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Box(modifier = Modifier.size(width = 32.dp, height = 4.dp))
            }
        }
    ) {
        val bottomPadding = animateDpAsState(
            targetValue = if (sheetState.currentValue == SheetValue.Hidden) scaffoldPadding.calculateBottomPadding() else scaffoldPadding.calculateBottomPadding() + 76.dp + 16.dp,
            label = "padding"
        )

        Surface(
            modifier = Modifier.padding(bottom = bottomPadding.value),
            color = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}