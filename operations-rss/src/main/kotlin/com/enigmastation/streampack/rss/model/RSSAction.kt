/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.model

import java.net.URL

class RSSAction(var action: RSSActionOperation? = null, var url: URL? = null) {
    fun setAction(operation: RSSActionOperation): RSSAction {
        action = operation
        return this
    }

    fun setUrl(url: URL): RSSAction {
        this.url = url
        return this
    }
}
