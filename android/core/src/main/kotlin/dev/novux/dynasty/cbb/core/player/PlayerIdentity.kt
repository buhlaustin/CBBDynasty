package dev.novux.dynasty.cbb.core.player

data class PlayerIdentity(
    val displayName: String,
    val hometownCity: String,
    val hometownState: String,
    val heightInches: Int,
    val weightLbs: Int,
    val nilUsdPerYear: Long = 0,
    val nilDealRaisedThisSeason: Boolean = false,
) {
    val hometownLabel: String
        get() = if (hometownCity.isBlank()) hometownState else "$hometownCity, $hometownState"
}
