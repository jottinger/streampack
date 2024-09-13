/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.service

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.factoid.entity.Factoid
import com.enigmastation.streampack.factoid.entity.FactoidAttribute
import com.enigmastation.streampack.factoid.exception.FactoidLockedException
import com.enigmastation.streampack.factoid.model.FactoidAttributeType
import com.enigmastation.streampack.factoid.model.FactoidDTO
import com.enigmastation.streampack.factoid.repository.FactoidAttributeRepository
import com.enigmastation.streampack.factoid.repository.FactoidRepository
import java.util.Optional
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FactoidService(
    val factoidRepository: FactoidRepository,
    val factoidAttributeRepository: FactoidAttributeRepository
) {
    fun findBySelector(selector: String): List<FactoidAttribute> {
        return factoidAttributeRepository.findBySelectorIgnoreCase(selector)
    }

    @Transactional
    fun deleteSelector(selector: String) {
        factoidAttributeRepository.deleteAll(
            factoidAttributeRepository.findBySelectorIgnoreCase(selector)
        )
        factoidRepository.findBySelectorIgnoreCase(selector).ifPresent {
            factoidRepository.delete(it)
        }
    }

    @Transactional
    fun findBySelectorAndAttributeType(
        selector: String,
        type: FactoidAttributeType
    ): Optional<FactoidAttribute> =
        factoidAttributeRepository.findBySelectorIgnoreCaseAndAttributeType(selector, type)

    @Transactional
    fun save(entity: FactoidAttribute) {
        // need to check the factoid to make sure it's present and updated, too
        val factoid = factoidRepository.findBySelectorIgnoreCase(entity.selector!!)
        when (factoid.isPresent) {
            true -> {
                if (factoid.get().locked == true) {
                    throw FactoidLockedException(entity.selector!!)
                }
                factoid.get().updatedBy = entity.updatedBy
            }
            false -> {
                factoidRepository.save(
                    Factoid(
                        selector = entity.selector,
                        locked = false,
                        updatedBy = entity.updatedBy
                    )
                )
            }
        }
        factoidAttributeRepository.save(entity)
    }

    fun findSelectorWithArguments(selector: String): Optional<Pair<String, String>> {
        val components = selector.split(" ")
        for (i in components.indices) {
            // Join the first (components.size - i) components to form the search string
            val searchSelector = components.take(components.size - i).joinToString(" ")

            // Attempt to find a match with the current selector
            val match = factoidRepository.findBySelectorIgnoreCase(searchSelector)
            if (match.isPresent()) {
                // If a match is found, collect the remaining components as the extras
                val extras = components.drop(components.size - i)
                return Optional.of(Pair(match.get().selector!!, extras.joinToString(" ")))
            }
        }
        // If no match is found, return null
        return Optional.empty()
    }

    @Transactional
    fun searchFactoidsForTerm(term: String): List<String> =
        factoidAttributeRepository.findAllWith("%$term%")

    @Transactional
    fun searchFactoids(term: String): List<FactoidDTO> {
        // get the factoids that match the search term
        val candidates = searchFactoidsForTerm(term)

        // okay, now we build a DTO for each matching search term. ... this might get
        // expensive, we may want to build an aggregate object that has
        // all the factoid ontology represented.
        return candidates
            .map { selector ->
                val dto = FactoidDTO(selector = selector)
                // we get the list this way so timestamp and updated by are easier to assign.
                // the most recent values will be last.
                val attributes =
                    factoidAttributeRepository.findBySelectorIgnoreCase(selector).sortedBy {
                        it.updateTimestamp
                    }
                attributes.forEach { attribute ->
                    val value = (attribute.attributeValue ?: "").compress()
                    attribute.attributeType?.let { type ->
                        when (type) {
                            FactoidAttributeType.TAGS -> dto.tags = value
                            FactoidAttributeType.URLS -> dto.urls = value
                            FactoidAttributeType.TEXT -> dto.text = value
                            FactoidAttributeType.SEEALSO -> dto.seealso = value
                            FactoidAttributeType.LANGUAGES -> dto.languages = value
                            else -> {}
                        }
                        dto.updateTimestamp = attribute.updateTimestamp
                        dto.updatedBy = attribute.updatedBy
                    }
                }
                dto
            }
            .toList()
    }
}
