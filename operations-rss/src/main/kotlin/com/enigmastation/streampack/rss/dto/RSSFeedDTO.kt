/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.dto

import java.net.URL
import java.time.OffsetDateTime
import java.util.*

data class RSSFeedDTO(
    override val id: UUID?,
    override val title: String?,
    override val url: URL?,
    override val feedUrl: URL?,
    override val createDate: OffsetDateTime?,
    override val updateDate: OffsetDateTime?,
) : RSSFeedOut

interface RSSFeedOut {
    val id: UUID?
    val title: String?
    val url: URL?
    val feedUrl: URL?
    val createDate: OffsetDateTime?
    val updateDate: OffsetDateTime?
}
