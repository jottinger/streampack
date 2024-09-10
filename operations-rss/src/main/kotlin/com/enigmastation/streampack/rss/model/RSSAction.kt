/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.model

class RSSAction(var action: RSSActionOperation? = null, var url: String? = null) {
    fun setAction(operation: RSSActionOperation): RSSAction {
        action = operation
        return this
    }

    fun setUrl(url: String): RSSAction {
        this.url = url
        return this
    }
}
