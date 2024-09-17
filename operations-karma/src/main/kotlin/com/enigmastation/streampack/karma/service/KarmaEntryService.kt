/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.karma.service

import com.enigmastation.streampack.karma.dto.KarmaSummary
import com.enigmastation.streampack.karma.entity.KarmaEntry
import com.enigmastation.streampack.karma.repository.KarmaEntryRepository
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.exp
import kotlin.math.roundToInt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class KarmaEntryService(val karmaEntryRepository: KarmaEntryRepository) {
    @Transactional
    fun addEntry(selector: String, value: Int, comment: String?): KarmaSummary {
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
    fun getKarma(selector: String): KarmaSummary {
        val now = OffsetDateTime.now()
        val cutOffDate = now.minus(1, ChronoUnit.YEARS)
        // first, let's get rid of the really old karma.
        // we don't care about selectors here; we wouldn't want any really old data.
        karmaEntryRepository.deleteAll(
            karmaEntryRepository.findKarmaEntryByCreateTimestampBefore(cutOffDate)
        )
        // okay, for all of the karma that remains...
        val summary =
            karmaEntryRepository
                .findKarmaEntryBySelectorOrderByCreateTimestampDesc(selector.lowercase())
                .filter { it.increment != null }
                .fold(
                    KarmaSummary(0.0.toDouble(), emptyList()),
                    { summary, entry ->
                        val score =
                            calculateWeightedScore(
                                entry.increment!!,
                                ChronoUnit.DAYS.between(entry.createTimestamp!!, now)
                            )
                        summary.copy(
                            karma = summary.karma + score,
                            comments =
                                summary.comments +
                                    if (entry.comment.isNullOrEmpty()) {
                                        emptyList()
                                    } else {
                                        listOf(entry.comment!!)
                                    }
                        )
                    }
                )
        return summary.copy(
            karma = summary.karma.roundToInt().toDouble(),
            comments = summary.comments.take(10)
        )
    }

    fun calculateWeightedScore(value: Int, ageInDays: Long, k: Double = 0.002): Double {
        val weight = exp(-k * ageInDays)
        return value * weight
    }
}
