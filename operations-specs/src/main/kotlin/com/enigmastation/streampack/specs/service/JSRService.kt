/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.specs.service

import org.springframework.stereotype.Service

@Service
class JSRService : GetTitleService() {
    override fun getUrl(identifier: Int): String = "https://jcp.org/en/jsr/detail?id=$identifier"

    /**
     * For the JCP, you look for the *text* of the div with class "header1." Very convenient, JCP.
     */
    override fun getTitle(identifier: Int): Pair<String, String> {
        val url = getUrl(identifier)
        val data = jsoupService.get(url).selectFirst(".header1")

        data?.select("sup")?.remove()

        return Pair(url, data!!.text())
    }
}
