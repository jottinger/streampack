/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.http.service

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @GetMapping("/foo/{value}", "/foo", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTestThing(@PathVariable("value") value: Int = 1): ResponseEntity<TestThing> {
        return ResponseEntity.ok(TestThing(value))
    }
}
