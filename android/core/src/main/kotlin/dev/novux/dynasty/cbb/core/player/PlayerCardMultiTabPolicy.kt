package dev.novux.dynasty.cbb.core.player

/** Full player card exposes tab strip ([MEGA-GDD-057]). */
object PlayerCardMultiTabPolicy {
    const val MEGA_ID: String = "MEGA-GDD-057"

    /** Overview, ratings, stats, games, history — matches `PlayerCardTab.ORDER`. */
    const val tabCount: Int = 5

    const val FULL_SCREEN_COMPOSABLE: String = "PlayerCardScreen"
}