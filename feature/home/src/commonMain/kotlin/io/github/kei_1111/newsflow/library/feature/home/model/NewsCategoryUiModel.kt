package io.github.kei_1111.newsflow.library.feature.home.model

import io.github.kei_1111.newsflow.library.core.model.NewsCategory

enum class NewsCategoryUiModel {
    GENERAL,
    BUSINESS,
    TECHNOLOGY,
    ENTERTAINMENT,
    SPORTS,
    SCIENCE,
    HEALTH;

    fun toNewsCategory(): NewsCategory = when (this) {
        GENERAL -> NewsCategory.GENERAL
        BUSINESS -> NewsCategory.BUSINESS
        TECHNOLOGY -> NewsCategory.TECHNOLOGY
        ENTERTAINMENT -> NewsCategory.ENTERTAINMENT
        SPORTS -> NewsCategory.SPORTS
        SCIENCE -> NewsCategory.SCIENCE
        HEALTH -> NewsCategory.HEALTH
    }

    companion object {
        fun convert(newsCategory: NewsCategory): NewsCategoryUiModel = when (newsCategory) {
            NewsCategory.GENERAL -> GENERAL
            NewsCategory.BUSINESS -> BUSINESS
            NewsCategory.TECHNOLOGY -> TECHNOLOGY
            NewsCategory.ENTERTAINMENT -> ENTERTAINMENT
            NewsCategory.SPORTS -> SPORTS
            NewsCategory.SCIENCE -> SCIENCE
            NewsCategory.HEALTH -> HEALTH
        }
    }
}
