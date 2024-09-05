/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.urltitle.handler

import com.enigmastation.streampack.urltitle.service.UrlTitleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IgnoredHostsHandler {
    @Autowired lateinit var urlTitleService: UrlTitleService

    @GetMapping("/ignoredhosts", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getIgnoredHosts(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(urlTitleService.findAll().mapNotNull { it.hostName }.toList())
    }
}
