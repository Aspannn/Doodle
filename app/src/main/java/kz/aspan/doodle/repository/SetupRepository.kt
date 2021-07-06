package kz.aspan.doodle.repository

import kz.aspan.doodle.data.remote.ws.Room
import kz.aspan.doodle.util.Resource

interface SetupRepository {

    suspend fun createRoom(room: Room): Resource<Unit>

    suspend fun getRooms(searchQuery: String): Resource<List<Room>>

    suspend fun joinRoom(username: String, roomName: String): Resource<Unit>
}