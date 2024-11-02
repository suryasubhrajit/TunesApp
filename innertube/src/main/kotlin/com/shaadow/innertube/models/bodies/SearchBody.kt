package com.shaadow.innertube.models.bodies

import com.shaadow.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SearchBody(
    val context: Context = Context.DefaultWeb,
    val query: String,
    val params: String
)
