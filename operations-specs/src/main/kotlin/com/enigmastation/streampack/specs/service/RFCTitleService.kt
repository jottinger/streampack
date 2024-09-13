/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.specs.service

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Primary
@Service
class RFCTitleService : GetTitleService() {
    override fun getUrl(identifier: Int): String =
        "https://www.rfc-editor.org/rfc/rfc$identifier.html"
}
