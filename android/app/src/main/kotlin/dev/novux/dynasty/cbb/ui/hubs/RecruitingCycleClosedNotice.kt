package dev.novux.dynasty.cbb.ui.hubs

import android.content.Context
import android.widget.Toast
import dev.novux.dynasty.cbb.R

fun notifyRecruitingCycleClosed(context: Context) {
    Toast.makeText(
        context,
        context.getString(R.string.recruiting_cycle_closed_body),
        Toast.LENGTH_LONG,
    ).show()
}
