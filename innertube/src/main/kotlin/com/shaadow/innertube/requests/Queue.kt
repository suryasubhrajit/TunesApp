package com.shaadow.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.models.GetQueueResponse
import com.shaadow.innertube.models.bodies.QueueBody
import com.shaadow.innertube.utils.from
import com.shaadow.innertube.utils.runCatchingNonCancellable

suspend fun Innertube.queue(body: QueueBody) = runCatchingNonCancellable {
    val response = client.post(queue) {
        setBody(body)
        mask("queueDatas.content.$playlistPanelVideoRendererMask")
    }.body<GetQueueResponse>()

    response
        .queueDatas
        ?.mapNotNull { queueData ->
            queueData
                .content
                ?.playlistPanelVideoRenderer
                ?.let(Innertube.SongItem::from)
        }
}

suspend fun Innertube.song(videoId: String): Result<Innertube.SongItem?>? =
    queue(QueueBody(videoIds = listOf(videoId)))?.map { it?.firstOrNull() }
