/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.repository

import com.enigmastation.streampack.factoid.entity.FactoidAttribute
import com.enigmastation.streampack.factoid.model.FactoidAttributeType
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FactoidAttributeRepository : JpaRepository<FactoidAttribute, UUID> {
    fun findBySelectorIgnoreCaseAndAttributeType(
        selector: String,
        attrType: FactoidAttributeType
    ): Optional<FactoidAttribute>

    fun findBySelectorIgnoreCase(selector: String): List<FactoidAttribute>

    @Query(
        value =
            """
        select f.selector from FactoidAttribute f where lower(f.attributeValue||' '||f.selector|| ' '|| f.updatedBy) LIKE :term
        GROUP BY f.selector
        """
    )
    fun findAllWith(@Param("term") term: String): List<String>
}
