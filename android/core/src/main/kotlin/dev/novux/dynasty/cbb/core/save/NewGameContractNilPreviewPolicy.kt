package dev.novux.dynasty.cbb.core.save

/** Sign-contract step must echo school-step NIL preview for chosen program ([MEGA-GDD-056]). */
object NewGameContractNilPreviewPolicy {
    const val MEGA_ID: String = "MEGA-GDD-056"

    fun annualNilUsd(preview: NewGameSchoolSummaryPreview): Long = preview.annualNilUsd
}