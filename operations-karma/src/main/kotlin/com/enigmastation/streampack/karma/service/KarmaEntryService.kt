/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.karma.service

import com.enigmastation.streampack.karma.entity.KarmaEntry
import com.enigmastation.streampack.karma.repository.KarmaEntryRepository
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.exp
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class KarmaEntryService(val karmaEntryRepository: KarmaEntryRepository) {
    @Transactional
    fun addEntry(selector: String, value: Int, comment: String?): Int {
        karmaEntryRepository.save(
            KarmaEntry(selector = selector.lowercase(), increment = value, comment = comment)
        )
        return getKarma(selector)
    }

    @Transactional
    fun hasKarma(selector: String): Boolean {
        return karmaEntryRepository.findTop1BySelector(selector.lowercase()).isNotEmpty()
    }

    @Transactional
    fun getKarma(selector: String): Int {
        val now = OffsetDateTime.now()
        val cutOffDate = now.minus(1, ChronoUnit.YEARS)
        // first, let's get rid of the really old karma.
        // we don't care about selectors here; we wouldn't want any really old data.
        karmaEntryRepository.deleteAll(
            karmaEntryRepository.findKarmaEntryByCreateTimestampBefore(cutOffDate)
        )
        // okay, for all of the karma that remains...
        return karmaEntryRepository
            .findKarmaEntryBySelector(selector.lowercase())
            .filter { it.increment != null }
            .map {
                calculateWeightedScore(
                    it.increment!!,
                    ChronoUnit.DAYS.between(it.createTimestamp!!, now)
                )
            }
            .sum()
            .toInt()
    }

    fun calculateWeightedScore(value: Int, ageInDays: Long, k: Double = 0.002): Double {
        val weight = exp(-k * ageInDays)
        return value * weight
    }
}
