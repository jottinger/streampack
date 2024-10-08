/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.summary.dto

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
class Summary {
    var summary: String? = null
    var categories: List<String>? = null

    override fun toString(): String {
        return "Summary[summary=$summary, categories=$categories]"
    }
}
