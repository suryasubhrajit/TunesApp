package com.shaadow.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.models.SearchSuggestionsResponse
import com.shaadow.innertube.models.bodies.SearchSuggestionsBody
import com.shaadow.innertube.utils.runCatchingNonCancellable

suspend fun Innertube.searchSuggestions(body: SearchSuggestionsBody) = runCatchingNonCancellable {
    val response = client.post(searchSuggestions) {
        setBody(body)
        mask("contents.searchSuggestionsSectionRenderer.contents.searchSuggestionRenderer.navigationEndpoint.searchEndpoint.query")
    }.body<SearchSuggestionsResponse>()

    response
        .contents
        ?.firstOrNull()
        ?.searchSuggestionsSectionRenderer
        ?.contents
        ?.mapNotNull { content ->
            content
                .searchSuggestionRenderer
                ?.navigationEndpoint
                ?.searchEndpoint
                ?.query
        }
}
