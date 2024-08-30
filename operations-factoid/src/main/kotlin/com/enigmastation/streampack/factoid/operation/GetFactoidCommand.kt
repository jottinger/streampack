/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.operation

import com.enigmastation.streampack.factoid.model.FactoidAttributeType

data class GetFactoidCommand(val selector: String, val attribute: FactoidAttributeType)
