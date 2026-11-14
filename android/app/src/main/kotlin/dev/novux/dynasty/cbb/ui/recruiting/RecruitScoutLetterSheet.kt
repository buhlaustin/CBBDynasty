package dev.novux.dynasty.cbb.ui.recruiting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.recruiting.RecruitScoutLetterCategory
import dev.novux.dynasty.cbb.core.recruiting.RecruitScoutLetters

@Composable
fun RecruitScoutLetterSheet(
    letters: RecruitScoutLetters,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.recruiting_scout_sheet_heading),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            letters.asRows().forEach { (category, grade) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = categoryLabel(category),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = grade,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun categoryLabel(category: RecruitScoutLetterCategory): String = when (category) {
    RecruitScoutLetterCategory.Overall -> stringResource(R.string.recruiting_scout_cat_overall)
    RecruitScoutLetterCategory.Offense -> stringResource(R.string.recruiting_scout_cat_offense)
    RecruitScoutLetterCategory.Defense -> stringResource(R.string.recruiting_scout_cat_defense)
    RecruitScoutLetterCategory.Shooting -> stringResource(R.string.recruiting_scout_cat_shooting)
    RecruitScoutLetterCategory.Handling -> stringResource(R.string.recruiting_scout_cat_handling)
    RecruitScoutLetterCategory.Athleticism -> stringResource(R.string.recruiting_scout_cat_athleticism)
    RecruitScoutLetterCategory.Potential -> stringResource(R.string.recruiting_scout_cat_potential)
}
