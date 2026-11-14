package dev.novux.dynasty.cbb.ui.discipline

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import dev.novux.dynasty.cbb.core.discipline.PendingDisciplineIncident
import dev.novux.dynasty.cbb.core.qa.DisciplineDashboardFirstPaint
import dev.novux.dynasty.cbb.data.discipline.DisciplineRepository

/** Full-screen discipline decision sheet from in-shell dashboard ([MEGA-FRAME-032]). */
object DisciplineDashboardSheet {
    const val QA_ID: String = DisciplineDashboardFirstPaint.FRAME_CROSS_REF
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberDisciplineDecisionSheetState() = rememberModalBottomSheetState(skipPartiallyExpanded = true)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisciplineDecisionBottomSheet(
    incident: PendingDisciplineIncident?,
    dynastyId: String,
    seasonIndex: Int,
    disciplineRepository: DisciplineRepository,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onResolved: () -> Unit,
) {
    val pending = incident ?: return
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        DisciplineDecisionScreen(
            dynastyId = dynastyId,
            seasonIndex = seasonIndex,
            incident = pending,
            disciplineRepository = disciplineRepository,
            onResolved = {
                onResolved()
                onDismiss()
            },
            onBack = onDismiss,
        )
    }
}
