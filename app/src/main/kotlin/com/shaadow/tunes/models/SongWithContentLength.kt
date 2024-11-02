package com.shaadow.tunes.models

import androidx.compose.runtime.Immutable
import androidx.room.Embedded

@Immutable
data class SongWithContentLength(
    @Embedded val song: Song,
    val contentLength: Long?
)
