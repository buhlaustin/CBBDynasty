package dev.novux.dynasty.cbb.ui.shell

import android.content.Context
import android.content.Intent
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.save.DynastyExportArchive

object DynastyExportShare {
    fun shareJson(context: Context, archive: DynastyExportArchive, slotNumber: Int) {
        val fileLabel = "cbb-dynasty-slot-$slotNumber-${archive.dynastyId.take(12)}.json"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, fileLabel)
            putExtra(Intent.EXTRA_TEXT, archive.toJson(pretty = true))
        }
        val chooserTitle = context.getString(R.string.load_slot_export_chooser)
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }
}
