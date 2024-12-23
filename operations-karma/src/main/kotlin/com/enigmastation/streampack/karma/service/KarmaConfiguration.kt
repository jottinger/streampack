/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.karma.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("streampack.karma")
@Component
class KarmaConfiguration {
    var commentsEnabled = true
    var selfKarmaAllowed = false
}
