/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.handler

import com.enigmastation.streampack.whiteboard.dto.NamedServiceDTO
import com.enigmastation.streampack.whiteboard.model.NamedService
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

data class Operation(val valname: String, val priority: Int)

@RestController
class OperationsHandler(
    val operations: List<RouterOperation>,
    val namedObjects: List<NamedService>
) {
    @GetMapping("/operations", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getSetOfOperations(): ResponseEntity<List<NamedServiceDTO>> {
        val data =
            operations
                .map {
                    NamedServiceDTO(it.name, it.description(), it.longDescription(), it.priority)
                }
                .toList()
        return ResponseEntity.ok(data)
    }
}
