package com.shaadow.tunes.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.HistoryToggleOff
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.Database
import com.shaadow.tunes.R
import com.shaadow.tunes.internal
import com.shaadow.tunes.path
import com.shaadow.tunes.query
import com.shaadow.tunes.service.PlayerService
import com.shaadow.tunes.ui.styling.Dimensions
import com.shaadow.tunes.utils.intent
import com.shaadow.tunes.utils.pauseSearchHistoryKey
import com.shaadow.tunes.utils.rememberPreference
import com.shaadow.tunes.utils.toast
import kotlinx.coroutines.flow.distinctUntilChanged
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess

@ExperimentalAnimationApi
@Composable
fun DatabaseSettings() {
    val context = LocalContext.current

    var pauseSearchHistory by rememberPreference(pauseSearchHistoryKey, false)

    val queriesCount by remember {
        Database.queriesCount().distinctUntilChanged()
    }.collectAsState(initial = 0)

    val eventsCount by remember {
        Database.eventsCount().distinctUntilChanged()
    }.collectAsState(initial = 0)

    val backupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.sqlite3")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            query {
                Database.checkpoint()

                context.applicationContext.contentResolver.openOutputStream(uri)
                    ?.use { outputStream ->
                        FileInputStream(internal.path).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
            }
        }

    val restoreLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            query {
                Database.checkpoint()
                internal.close()

                context.applicationContext.contentResolver.openInputStream(uri)
                    ?.use { inputStream ->
                        FileOutputStream(internal.path).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                context.stopService(context.intent<PlayerService>())
                exitProcess(0)
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.history),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )

        SwitchSettingEntry(
            title = stringResource(id = R.string.pause_search_history),
            text = stringResource(id = R.string.pause_search_history_description),
            icon = Icons.Outlined.HistoryToggleOff,
            isChecked = pauseSearchHistory,
            onCheckedChange = { pauseSearchHistory = it }
        )

        SettingsEntry(
            title = stringResource(id = R.string.clear_search_history),
            text = if (queriesCount > 0) {
                stringResource(id = R.string.delete_search_queries, queriesCount)
            } else {
                stringResource(id = R.string.history_is_empty)
            },
            icon = Icons.Outlined.DeleteSweep,
            onClick = { query(Database::clearQueries) },
            isEnabled = queriesCount > 0
        )

        SettingsEntry(
            title = stringResource(id = R.string.reset_quick_picks),
            text = if (eventsCount > 0) {
                stringResource(id = R.string.delete_playback_events, eventsCount)
            } else {
                stringResource(id = R.string.quick_picks_cleared)
            },
            icon = Icons.Outlined.RestartAlt,
            onClick = { query(Database::clearEvents) },
            isEnabled = eventsCount > 0
        )

        Spacer(modifier = Modifier.height(Dimensions.spacer))

        Text(
            text = stringResource(id = R.string.backup),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )

        SettingsEntry(
            title = stringResource(id = R.string.backup),
            text = stringResource(id = R.string.backup_description),
            icon = Icons.Outlined.SaveAlt,
            onClick = {
                @SuppressLint("SimpleDateFormat")
                val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")

                try {
                    backupLauncher.launch("musicyou_${dateFormat.format(Date())}.db")
                } catch (e: ActivityNotFoundException) {
                    context.toast("Couldn't find an application to create documents")
                }
            }
        )

        SettingsEntry(
            title = stringResource(id = R.string.restore),
            text = stringResource(id = R.string.restore_description),
            icon = Icons.Outlined.SettingsBackupRestore,
            onClick = {
                try {
                    restoreLauncher.launch(
                        arrayOf(
                            "application/vnd.sqlite3",
                            "application/x-sqlite3",
                            "application/octet-stream"
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    context.toast("Couldn't find an application to open documents")
                }
            }
        )

        SettingsInformation(text = stringResource(id = R.string.restore_information))
    }
}
