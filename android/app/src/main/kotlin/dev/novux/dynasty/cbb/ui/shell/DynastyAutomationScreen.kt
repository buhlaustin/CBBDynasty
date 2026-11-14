package dev.novux.dynasty.cbb.ui.shell

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.CoachArchetype
import dev.novux.dynasty.cbb.core.settings.DynastyAutomationPriorityPolicy
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.core.settings.DynastyAutomationSettings
import dev.novux.dynasty.cbb.data.coach.CoachSkillPointRepository
import dev.novux.dynasty.cbb.data.save.SaveSlotRepository
import dev.novux.dynasty.cbb.data.settings.DynastyAutomationRepository
import kotlinx.coroutines.launch

@Composable
fun DynastyAutomationScreen(
    loaded: LoadedDynasty,
    automationRepository: DynastyAutomationRepository,
    coachSkillPointRepository: CoachSkillPointRepository,
    saveSlotRepository: SaveSlotRepository,
    onBack: () -> Unit,
) {
    var settings by remember { mutableStateOf(DynastyAutomationSettings()) }
    var archetype by remember { mutableStateOf(CoachArchetype.Balanced) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(loaded.dynastyId) {
        settings = automationRepository.load(loaded.dynastyId)
        archetype = saveSlotRepository.coachArchetypeForDynasty(loaded.dynastyId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(R.string.dynasty_automation_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.dynasty_automation_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(
                R.string.dynasty_automation_priority_hint,
                DynastyAutomationPriorityPolicy.coachPriorityTreeDepth,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.dynasty_automation_recruiting_ledger_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AutomationToggleRow(
            label = stringResource(R.string.dynasty_automation_recruiting),
            description = stringResource(R.string.dynasty_automation_recruiting_summary),
            checked = settings.recruitingAutoMaintain,
            onCheckedChange = { enabled ->
                scope.launch {
                    val next = settings.copy(recruitingAutoMaintain = enabled)
                    automationRepository.save(loaded.dynastyId, next)
                    settings = next
                }
            },
        )
        AutomationToggleRow(
            label = stringResource(R.string.dynasty_automation_coach_skills),
            description = stringResource(R.string.dynasty_automation_coach_skills_summary),
            checked = settings.coachAutoSpend,
            onCheckedChange = { enabled ->
                scope.launch {
                    val next = settings.copy(coachAutoSpend = enabled)
                    automationRepository.save(loaded.dynastyId, next)
                    coachSkillPointRepository.setAutoSpend(loaded.dynastyId, enabled, archetype)
                    settings = next
                }
            },
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_back))
        }
    }
}

@Composable
private fun AutomationToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
