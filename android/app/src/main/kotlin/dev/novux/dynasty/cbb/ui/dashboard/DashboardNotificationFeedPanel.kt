package dev.novux.dynasty.cbb.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink
import dev.novux.dynasty.cbb.core.notification.NotificationFeedItem
import dev.novux.dynasty.cbb.core.notification.NotificationFeedCommitRowPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedGameWeekRowPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationEventKind
import dev.novux.dynasty.cbb.core.notification.NotificationFeedPlayWeekDeepLinkPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedSigningDayFeedPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubTab

@Composable
fun DashboardNotificationFeedPanel(
    feed: List<NotificationFeedItem>,
    onDeepLink: (RecruitingHubTab, String?) -> Unit,
    onStrategyOpen: () -> Unit,
    onCoachCardOpen: () -> Unit,
    onSigningDayOpen: (DashboardDeepLink) -> Unit = {},
    onGamePreviewOpen: () -> Unit = onStrategyOpen,
    onDisciplineOpen: () -> Unit = {},
    onMarchMayhemOpen: () -> Unit = {},
    onFundraisingOpen: () -> Unit = {},
    onRankingsOpen: () -> Unit = onStrategyOpen,
    onHistoryOpen: () -> Unit = onStrategyOpen,
    modifier: Modifier = Modifier,
) {
    if (feed.isEmpty()) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.dashboard_notification_heading),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        feed.forEach { row ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        when (row.deepLink) {
                            DashboardDeepLink.Strategy -> onStrategyOpen()
                            DashboardDeepLink.PlayWeekGame -> {
                                if (NotificationFeedGameWeekRowPolicy.routesToGamePreview(row.deepLink)) {
                                    onGamePreviewOpen()
                                } else {
                                    onStrategyOpen()
                                }
                            }
                            DashboardDeepLink.RankingsHub -> onRankingsOpen()
                            DashboardDeepLink.HistoryHub -> onHistoryOpen()
                            DashboardDeepLink.CoachCard -> onCoachCardOpen()
                            DashboardDeepLink.DisciplineSheet -> onDisciplineOpen()
                            DashboardDeepLink.MarchMayhemHub -> onMarchMayhemOpen()
                            DashboardDeepLink.FundraisingWeekNine -> onFundraisingOpen()
                            DashboardDeepLink.SigningDayRankings,
                            DashboardDeepLink.SigningDayPositions,
                            -> {
                                check(NotificationFeedSigningDayFeedPolicy.isSigningDayDeepLink(row.deepLink)) {
                                    "Signing Day feed rows must use catalogued deep links"
                                }
                                onSigningDayOpen(row.deepLink)
                            }
                            else -> {
                                val focusName = when (row.kind) {
                                    NotificationEventKind.UserCommit,
                                    NotificationEventKind.UserDecommit,
                                    -> NotificationFeedCommitRowPolicy.prospectNameFromFeedBody(row.body)
                                    else -> null
                                }
                                onDeepLink(row.deepLink.toHubTab(), focusName)
                            }
                        }
                    },
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = row.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = row.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun DashboardDeepLink.toHubTab(): RecruitingHubTab = when (this) {
    DashboardDeepLink.RecruitingRecommended -> RecruitingHubTab.Recommended
    DashboardDeepLink.RecruitingPortal -> RecruitingHubTab.TransferPortal
    DashboardDeepLink.RecruitingHsJuco -> RecruitingHubTab.AllHsJuco
    else -> RecruitingHubTab.Recommended
}
