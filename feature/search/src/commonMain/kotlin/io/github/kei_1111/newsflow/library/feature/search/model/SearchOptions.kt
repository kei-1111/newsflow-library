package io.github.kei_1111.newsflow.library.feature.search.model

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

enum class SortBy(val apiValue: String) {
    RELEVANCY("relevancy"),
    POPULARITY("popularity"),
    PUBLISHED_AT("publishedAt"),
}

enum class DateRangePreset {
    ALL,
    LAST_24_HOURS,
    LAST_WEEK,
    LAST_MONTH,
}

enum class SearchLanguage(val apiValue: String?) {
    ALL(null),
    ENGLISH("en"),
    JAPANESE("ja"),
}

data class SearchOptions(
    val sortBy: SortBy = SortBy.RELEVANCY,
    val dateRangePreset: DateRangePreset = DateRangePreset.ALL,
    val language: SearchLanguage = SearchLanguage.ALL,
)

private const val ONE_DAY = 1
private const val DAYS_IN_WEEK = 7
private const val ONE_MONTH = 1

fun DateRangePreset.toDateRange(clock: Clock = Clock.System): Pair<String?, String?> {
    val now = clock.now()
    val today = now.toLocalDateTime(TimeZone.UTC).date
    return when (this) {
        DateRangePreset.ALL -> null to null
        DateRangePreset.LAST_24_HOURS -> {
            val yesterday = today.minus(ONE_DAY, DateTimeUnit.DAY)
            yesterday.toString() to today.toString()
        }
        DateRangePreset.LAST_WEEK -> {
            val weekAgo = today.minus(DAYS_IN_WEEK, DateTimeUnit.DAY)
            weekAgo.toString() to today.toString()
        }
        DateRangePreset.LAST_MONTH -> {
            val monthAgo = today.minus(ONE_MONTH, DateTimeUnit.MONTH)
            monthAgo.toString() to today.toString()
        }
    }
}
