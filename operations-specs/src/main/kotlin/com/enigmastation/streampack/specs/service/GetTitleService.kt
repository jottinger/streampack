/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.specs.service

import com.enigmastation.streampack.web.service.JsoupService
import org.springframework.beans.factory.annotation.Autowired

abstract class GetTitleService {
    @Autowired lateinit var jsoupService: JsoupService

    abstract fun getUrl(identifier: Int): String

    open fun getTitle(identifier: Int): Pair<String, String> {

        val url = getUrl(identifier)
        return Pair(url, jsoupService.get(url).title())
    }
}
