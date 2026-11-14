package dev.novux.dynasty.cbb.ui.advance

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.save.DynastyLoadFailure
import dev.novux.dynasty.cbb.core.save.DynastyLoadResult
import dev.novux.dynasty.cbb.core.save.DynastyWeekAdvanceBlockReason
import dev.novux.dynasty.cbb.core.save.LoadedDynasty

fun weekAdvanceBlockedMessage(context: Context, reason: DynastyWeekAdvanceBlockReason): String =
    when (reason) {
        DynastyWeekAdvanceBlockReason.DisciplinePending ->
            context.getString(R.string.advance_blocked_discipline)
    }

fun weekAdvanceUnreadableMessage(context: Context, failure: DynastyLoadFailure): String = when (failure) {
    is DynastyLoadFailure.DatabaseRead ->
        context.getString(R.string.advance_failed_database_read, failure.detail)
    is DynastyLoadFailure.InvalidCalendar ->
        context.getString(R.string.advance_failed_invalid_calendar, failure.detail)
    is DynastyLoadFailure.MissingMetadata,
    is DynastyLoadFailure.DynastyIdMismatch,
    is DynastyLoadFailure.PackIdMismatch,
    is DynastyLoadFailure.InvalidBalanceRuleset,
    is DynastyLoadFailure.InvalidSimTuning,
    is DynastyLoadFailure.IncompleteNewGameSign,
    -> context.getString(R.string.advance_failed_load_generic)
}

suspend fun applyWeekAdvanceResult(
    result: DynastyLoadResult,
    snackbarHostState: SnackbarHostState,
    context: Context,
    onSuccess: (LoadedDynasty) -> Unit,
): Boolean =
    when (result) {
        is DynastyLoadResult.Success -> {
            onSuccess(result.loaded)
            snackbarHostState.showSnackbar(context.getString(R.string.advance_week_saved))
            true
        }
        is DynastyLoadResult.WeekAdvanceBlocked -> {
            snackbarHostState.showSnackbar(weekAdvanceBlockedMessage(context, result.reason))
            false
        }
        is DynastyLoadResult.Unreadable -> {
            snackbarHostState.showSnackbar(weekAdvanceUnreadableMessage(context, result.failure))
            false
        }
        else -> false
    }
}

suspend fun showWeekAdvanceConfirmationUnavailable(
    snackbarHostState: SnackbarHostState,
    context: Context,
) {
    snackbarHostState.showSnackbar(context.getString(R.string.advance_confirm_unavailable))
}
