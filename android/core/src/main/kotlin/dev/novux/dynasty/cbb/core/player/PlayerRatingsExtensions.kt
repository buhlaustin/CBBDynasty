package dev.novux.dynasty.cbb.core.player

/** Composite shooting rating per [planning/player-cards.md#ratings] (average of sub-attributes). */
fun PlayerRatings.shootingOverall(): Int =
    (shootingInside + shootingMid + shootingThree) / 3
