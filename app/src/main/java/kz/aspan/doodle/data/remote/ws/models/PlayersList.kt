package kz.aspan.doodle.data.remote.ws.models

import kz.aspan.doodle.util.Constants.TYPE_PLAYERS_LIST


data class PlayersList(
    val players: List<PlayerData>
) : BaseModel(TYPE_PLAYERS_LIST)
