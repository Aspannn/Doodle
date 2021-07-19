package kz.aspan.doodle.data.remote.ws.models

import kz.aspan.doodle.util.Constants.TYPE_GAME_STATE


data class GameState(
    val drawingPlayer: String,
    val word: String
) : BaseModel(TYPE_GAME_STATE)
