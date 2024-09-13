/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.specs.service

import org.springframework.stereotype.Service

@Service
class JEPService : GetTitleService() {
    override fun getUrl(identifier: Int): String = "https://openjdk.org/jeps/$identifier"
}
