package dev.novux.dynasty.cbb.ui.dynasty

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.novux.dynasty.cbb.data.save.ActiveDynastySession

/** Bottom-nav selection; returns to Dashboard when overlay hubs pop ([MEGA-FRAME-139]). */
@Composable
fun rememberDynastyShellSelectedTab(): MutableState<DynastyShellTab> {
    val state = remember { mutableStateOf(DynastyShellTab.Dashboard) }
    DisposableEffect(Unit) {
        var lastNonce = ActiveDynastySession.shellDashboardTabRestoreNonce
        val remove = ActiveDynastySession.addListener {
            val nonce = ActiveDynastySession.shellDashboardTabRestoreNonce
            if (nonce != lastNonce) {
                lastNonce = nonce
                state.value = DynastyShellTab.Dashboard
            }
        }
        onDispose(remove)
    }
    return state
}