package dev.novux.dynasty.cbb.ui.news

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.news.NewsHeadlineKind

@Composable
fun newsHeadlineKindLabel(kind: NewsHeadlineKind): String = when (kind) {
    NewsHeadlineKind.UserInjury -> stringResource(R.string.shell_news_kind_user_injury)
    NewsHeadlineKind.LeagueInjury -> stringResource(R.string.shell_news_kind_league_injury)
    NewsHeadlineKind.LeagueResult -> stringResource(R.string.shell_news_kind_league_result)
    NewsHeadlineKind.RecruitingSpotlight -> stringResource(R.string.shell_news_kind_recruiting)
    NewsHeadlineKind.General -> stringResource(R.string.shell_news_kind_general)
    NewsHeadlineKind.Awards -> stringResource(R.string.shell_news_kind_awards)
    NewsHeadlineKind.Bracketology -> stringResource(R.string.shell_news_kind_bracketology)
    NewsHeadlineKind.PollNetMovement -> stringResource(R.string.shell_news_kind_poll_net)
    NewsHeadlineKind.PortalEntry -> stringResource(R.string.shell_news_kind_portal_entry)
    NewsHeadlineKind.PortalCommit -> stringResource(R.string.shell_news_kind_portal_commit)
    NewsHeadlineKind.PortalDecommit -> stringResource(R.string.shell_news_kind_portal_decommit)
    NewsHeadlineKind.PortalFlip -> stringResource(R.string.shell_news_kind_portal_flip)
}