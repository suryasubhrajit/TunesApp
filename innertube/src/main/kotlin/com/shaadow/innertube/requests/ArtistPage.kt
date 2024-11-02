package com.shaadow.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.models.BrowseResponse
import com.shaadow.innertube.models.MusicCarouselShelfRenderer
import com.shaadow.innertube.models.MusicShelfRenderer
import com.shaadow.innertube.models.SectionListRenderer
import com.shaadow.innertube.models.bodies.BrowseBody
import com.shaadow.innertube.utils.findSectionByTitle
import com.shaadow.innertube.utils.from
import com.shaadow.innertube.utils.runCatchingNonCancellable

suspend fun Innertube.artistPage(body: BrowseBody): Result<Innertube.ArtistPage>? =
    runCatchingNonCancellable {
        val response = client.post(browse) {
            setBody(body)
            mask("contents,header")
        }.body<BrowseResponse>()

        fun findSectionByTitle(text: String): SectionListRenderer.Content? {
            return response
                .contents
                ?.singleColumnBrowseResultsRenderer
                ?.tabs
                ?.get(0)
                ?.tabRenderer
                ?.content
                ?.sectionListRenderer
                ?.findSectionByTitle(text)
        }

        val songsSection = findSectionByTitle("Songs")?.musicShelfRenderer
        val albumsSection = findSectionByTitle("Albums")?.musicCarouselShelfRenderer
        val singlesSection = findSectionByTitle("Singles")?.musicCarouselShelfRenderer

        Innertube.ArtistPage(
            name = response
                .header
                ?.musicImmersiveHeaderRenderer
                ?.title
                ?.text,
            description = response
                .header
                ?.musicImmersiveHeaderRenderer
                ?.description
                ?.text,
            thumbnail = (response
                .header
                ?.musicImmersiveHeaderRenderer
                ?.foregroundThumbnail
                ?: response
                .header
                ?.musicImmersiveHeaderRenderer
                ?.thumbnail)
                ?.musicThumbnailRenderer
                ?.thumbnail
                ?.thumbnails
                ?.getOrNull(0),
            shuffleEndpoint = response
                .header
                ?.musicImmersiveHeaderRenderer
                ?.playButton
                ?.buttonRenderer
                ?.navigationEndpoint
                ?.watchEndpoint,
            radioEndpoint = response
                .header
                ?.musicImmersiveHeaderRenderer
                ?.startRadioButton
                ?.buttonRenderer
                ?.navigationEndpoint
                ?.watchEndpoint,
            songs = songsSection
                ?.contents
                ?.mapNotNull(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
                ?.mapNotNull(Innertube.SongItem::from),
            songsEndpoint = songsSection
                ?.bottomEndpoint
                ?.browseEndpoint,
            albums = albumsSection
                ?.contents
                ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                ?.mapNotNull(Innertube.AlbumItem::from),
            albumsEndpoint = albumsSection
                ?.header
                ?.musicCarouselShelfBasicHeaderRenderer
                ?.moreContentButton
                ?.buttonRenderer
                ?.navigationEndpoint
                ?.browseEndpoint,
            singles = singlesSection
                ?.contents
                ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                ?.mapNotNull(Innertube.AlbumItem::from),
            singlesEndpoint = singlesSection
                ?.header
                ?.musicCarouselShelfBasicHeaderRenderer
                ?.moreContentButton
                ?.buttonRenderer
                ?.navigationEndpoint
                ?.browseEndpoint,
        )
    }
