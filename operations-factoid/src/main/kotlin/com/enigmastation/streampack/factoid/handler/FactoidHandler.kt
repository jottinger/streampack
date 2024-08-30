/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.handler

import com.enigmastation.streampack.factoid.model.FactoidAttributeType
import com.enigmastation.streampack.factoid.service.FactoidService
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FactoidHandler(val service: FactoidService) {
    @GetMapping("/factoids/{term}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getFactoids(
        @PathVariable("term", required = true) term: String,
        @RequestParam("type") type: FactoidAttributeType = FactoidAttributeType.UNKNOWN
    ): ResponseEntity<Any> {
        return when (type) {
            FactoidAttributeType.INFO,
            FactoidAttributeType.FORGET ->
                ResponseEntity.of(
                        ProblemDetail.forStatusAndDetail(
                            HttpStatusCode.valueOf(400),
                            "Bad factoid attribute type: $type"
                        )
                    )
                    .build()
            else -> {
                // need to make sure the search term has something in it, and not a wildcard
                ResponseEntity.ok().body(service.searchFactoids(term))
            }
        }
    }
}
