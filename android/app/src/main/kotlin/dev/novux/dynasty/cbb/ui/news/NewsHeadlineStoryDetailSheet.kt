package dev.novux.dynasty.cbb.ui.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.news.NewsHeadline
import dev.novux.dynasty.cbb.core.news.NewsHeadlineArticleBuilder
import dev.novux.dynasty.cbb.core.news.NewsPortalStoryFooterPolicy
import dev.novux.dynasty.cbb.core.news.NewsStoryDrillInPolicy
import dev.novux.dynasty.cbb.ui.reports.ReportArticleView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsHeadlineStoryDetailSheet(
    headline: NewsHeadline?,
    onDismiss: () -> Unit,
    onOpenNewsHub: (() -> Unit)? = null,
    onOpenNewsHubPortal: (() -> Unit)? = null,
) {
    val selected = headline ?: return
    check(NewsStoryDrillInPolicy.opensMiniArticleDetail())
    val article = NewsStoryDrillInPolicy.articleFor(selected)
    val showHubCta = NewsHeadlineArticleBuilder.prefersNewsHubTab(selected) && onOpenNewsHub != null
    val showPortalFooter = NewsPortalStoryFooterPolicy.showViewInPortalFooter(selected) &&
        onOpenNewsHubPortal != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ReportArticleView(article = article)
            if (showPortalFooter) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenNewsHubPortal.invoke()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.news_story_footer_view_portal))
                }
            }
            if (showHubCta) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenNewsHub?.invoke()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.shell_news_detail_open_hub))
                }
            }
        }
    }
}