package kz.aspan.doodle.data.remote.ws.models

import kz.aspan.doodle.util.Constants.TYPE_JOIN_ROOM_HANDSHAKE


data class JoinRoomHandShake(
    val username: String,
    val roomName: String,
    val clientId: String
) : BaseModel(TYPE_JOIN_ROOM_HANDSHAKE)