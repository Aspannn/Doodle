package kz.aspan.doodle.data.remote.ws.models

data class RoomResponse(
    val name: String,
    val maxPlayers: Int,
    val playerCount: Int
)
