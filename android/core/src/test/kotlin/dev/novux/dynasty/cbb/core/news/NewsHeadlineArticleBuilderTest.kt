package dev.novux.dynasty.cbb.core.news

import dev.novux.dynasty.cbb.core.reports.ReportArticleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsHeadlineArticleBuilderTest {
    @Test
    fun miniArticle_wrapsHeadlineWithTwoToFourParagraphs() {
        NewsHeadlineKind.entries.forEach { kind ->
            val headline = NewsHeadline(
                id = "id-$kind",
                seasonIndex = 1,
                kind = kind,
                title = "Sample $kind",
                rsWeekIndex = 4,
            )
            val article = NewsHeadlineArticleBuilder.miniArticle(headline)
            assertEquals(ReportArticleType.NewsStory, article.type)
            assertEquals(headline.title, article.headline)
            assertTrue(article.paragraphs.size in 2..4)
        }
    }

    @Test
    fun recruitingSpotlight_prefersNewsHub() {
        val headline = NewsHeadline(
            id = "r",
            seasonIndex = 1,
            kind = NewsHeadlineKind.RecruitingSpotlight,
            title = "Five-star commit",
        )
        assertTrue(NewsHeadlineArticleBuilder.prefersNewsHubTab(headline))
    }
}
