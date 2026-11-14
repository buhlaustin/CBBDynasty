package dev.novux.dynasty.cbb.ui.player

enum class PlayerCardTab(val wire: String) {
    Overview("overview"),
    Ratings("ratings"),
    Stats("stats"),
    Games("games"),
    History("history"),
    ;

    companion object {
        val ORDER = listOf(Overview, Ratings, Stats, Games, History)
    }
}
