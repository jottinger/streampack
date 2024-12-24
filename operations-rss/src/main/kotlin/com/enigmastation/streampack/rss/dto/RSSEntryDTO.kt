package com.enigmastation.streampack.rss.dto

import java.net.URL
import java.time.OffsetDateTime
import java.util.*

data class RSSEntryDTO(
    override val id: UUID?,
    override val title: String?,
    override val url: URL?,
    override val summary: String?,
    override val llmSummary: String?,
    override val categories: List<String>?,
    override val published: OffsetDateTime?,
    override val createDate: OffsetDateTime?,
    override val updateDate: OffsetDateTime?
) : RSSEntryOut

interface RSSEntryOut {
    val id: UUID?
    val title: String?
    val url: URL?
    val summary: String?
    val llmSummary: String?
    val categories: List<String>?
    val published: OffsetDateTime?
    val createDate: OffsetDateTime?
    val updateDate: OffsetDateTime?
}
