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

fun DateRangePreset.toDateRange(): Pair<String?, String?> {
    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.UTC).date
    return when (this) {
        DateRangePreset.ALL -> null to null
        DateRangePreset.LAST_24_HOURS -> {
            val yesterday = today.minus(1, DateTimeUnit.DAY)
            yesterday.toString() to today.toString()
        }
        DateRangePreset.LAST_WEEK -> {
            val weekAgo = today.minus(7, DateTimeUnit.DAY)
            weekAgo.toString() to today.toString()
        }
        DateRangePreset.LAST_MONTH -> {
            val monthAgo = today.minus(1, DateTimeUnit.MONTH)
            monthAgo.toString() to today.toString()
        }
    }
}