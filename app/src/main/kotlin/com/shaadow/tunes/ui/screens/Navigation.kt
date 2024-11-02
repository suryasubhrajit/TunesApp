package com.shaadow.tunes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shaadow.tunes.enums.BuiltInPlaylist
import com.shaadow.tunes.enums.SettingsSection
import com.shaadow.tunes.models.Screen
import com.shaadow.tunes.ui.screens.album.AlbumScreen
import com.shaadow.tunes.ui.screens.artist.ArtistScreen
import com.shaadow.tunes.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import com.shaadow.tunes.ui.screens.home.HomeScreen
import com.shaadow.tunes.ui.screens.localplaylist.LocalPlaylistScreen
import com.shaadow.tunes.ui.screens.playlist.PlaylistScreen
import com.shaadow.tunes.ui.screens.search.SearchScreen
import com.shaadow.tunes.ui.screens.settings.SettingsPage
import com.shaadow.tunes.ui.screens.settings.SettingsScreen
import com.shaadow.tunes.utils.homeScreenTabIndexKey
import com.shaadow.tunes.utils.rememberPreference
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun Navigation(
    navController: NavHostController,
    sheetState: SheetState
) {
    val scope = rememberCoroutineScope()
    val (screenIndex, _) = rememberPreference(homeScreenTabIndexKey, defaultValue = 0)
    val homeRoutes = listOf(
        Screen.Home,
        Screen.Songs,
        Screen.Artists,
        Screen.Albums,
        Screen.Playlists
    ).map { it.route }

    @Composable
    fun SheetBackHandler() {
        BackHandler(enabled = sheetState.currentValue == SheetValue.Expanded) {
            scope.launch { sheetState.partialExpand() }
        }
    }

    NavHost(
        navController = navController,
        startDestination = homeRoutes.getOrElse(screenIndex) { Screen.Home.route },
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) + fadeIn() }
    ) {
        val navigateToAlbum =
            { browseId: String -> navController.navigate(route = "album/$browseId") }
        val navigateToArtist = { browseId: String -> navController.navigate("artist/$browseId") }
        val popDestination = {
            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
        }

        composable(
            route = "home",
            enterTransition = {
                if (homeRoutes.contains(initialState.destination.route)) slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up
                ) + fadeIn()
                else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) + fadeIn()
            },
            popEnterTransition = {
                if (homeRoutes.contains(initialState.destination.route)) slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up
                ) + fadeIn()
                else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) + fadeIn()
            }
        ) {
            HomeScreen(
                navController = navController,
                screenIndex = 0
            )

            SheetBackHandler()
        }

        composable(
            route = "songs",
            enterTransition = {
                if (homeRoutes.contains(initialState.destination.route)) slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up
                ) + fadeIn()
                else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) + fadeIn()
            },
            popEnterTransition = {
                if (homeRoutes.contains(initialState.destination.route)) slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up
                ) + fadeIn()
                else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) + fadeIn()
            }
        ) {
            HomeScreen(
                navController = navController,
                screenIndex = 1
            )

            SheetBackHandler()
        }

        composable(
            route = "artists",
            enterTransition = {
                if (homeRoutes.contains(initialState.destination.route)) slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up
                ) + fadeIn()
                else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) + fadeIn()
            },
            popEnterTransition = {
                if (homeRoutes.contains(initialState.destination.route)) slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up
                ) + fadeIn()
                else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) + fadeIn()
            }
        ) {
            HomeScreen(
                navController = navController,
                screenIndex = 2
            )

            SheetBackHandler()
        }

        composable(
            route = "albums",
            enterTransition = {
                if (homeRoutes.contains(initialState.destination.route)) slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up
                ) + fadeIn()
                else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) + fadeIn()
            },
            popEnterTransition = {
                if (homeRoutes.contains(initialState.destination.route)) slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up
                ) + fadeIn()
                else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) + fadeIn()
            }
        ) {
            HomeScreen(
                navController = navController,
                screenIndex = 3
            )

            SheetBackHandler()
        }

        composable(
            route = "playlists",
            enterTransition = {
                if (homeRoutes.contains(initialState.destination.route)) slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up
                ) + fadeIn()
                else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) + fadeIn()
            },
            popEnterTransition = {
                if (homeRoutes.contains(initialState.destination.route)) slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up
                ) + fadeIn()
                else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) + fadeIn()
            }
        ) {
            HomeScreen(
                navController = navController,
                screenIndex = 4
            )

            SheetBackHandler()
        }

        composable(
            route = "artist/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""

            ArtistScreen(
                browseId = id,
                pop = popDestination,
                onAlbumClick = navigateToAlbum
            )

            SheetBackHandler()
        }

        composable(
            route = "album/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""

            AlbumScreen(
                browseId = id,
                pop = popDestination,
                onAlbumClick = navigateToAlbum,
                onGoToArtist = navigateToArtist
            )

            SheetBackHandler()
        }

        composable(
            route = "playlist/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""

            PlaylistScreen(
                browseId = id,
                pop = popDestination,
                onGoToAlbum = navigateToAlbum,
                onGoToArtist = navigateToArtist
            )

            SheetBackHandler()
        }

        composable(route = "settings") {
            SettingsScreen(
                pop = popDestination,
                onGoToSettingsPage = { index -> navController.navigate("settingsPage/$index") }
            )

            SheetBackHandler()
        }

        composable(
            route = "settingsPage/{index}",
            arguments = listOf(
                navArgument(
                    name = "index",
                    builder = { type = NavType.IntType }
                )
            )
        ) { navBackStackEntry ->
            val index = navBackStackEntry.arguments?.getInt("index") ?: 0

            SettingsPage(
                section = SettingsSection.entries[index],
                pop = popDestination
            )

            SheetBackHandler()
        }

        composable(route = "search") {
            SearchScreen(
                pop = popDestination,
                onAlbumClick = navigateToAlbum,
                onArtistClick = navigateToArtist,
                onPlaylistClick = { browseId -> navController.navigate("playlist/$browseId") }
            )

            SheetBackHandler()
        }

        composable(
            route = "builtInPlaylist/{index}",
            arguments = listOf(
                navArgument(
                    name = "index",
                    builder = { type = NavType.IntType }
                )
            )
        ) { navBackStackEntry ->
            val index = navBackStackEntry.arguments?.getInt("index") ?: 0

            BuiltInPlaylistScreen(
                builtInPlaylist = BuiltInPlaylist.entries[index],
                pop = popDestination,
                onGoToAlbum = navigateToAlbum,
                onGoToArtist = navigateToArtist
            )

            SheetBackHandler()
        }

        composable(
            route = "localPlaylist/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.LongType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getLong("id") ?: 0L

            LocalPlaylistScreen(
                playlistId = id,
                pop = popDestination,
                onGoToAlbum = navigateToAlbum,
                onGoToArtist = navigateToArtist
            )

            SheetBackHandler()
        }
    }
}