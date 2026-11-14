package dev.novux.dynasty.cbb.core.shell

import dev.novux.dynasty.cbb.core.GameConstants

/** Studio splash full-bleed brand background ([MEGA-SHELL-006], [planning/branding.md]). */
object StudioBrandBackgroundPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-006"

    val backgroundArgb: Long get() = GameConstants.BRAND_BACKGROUND_HEX
}