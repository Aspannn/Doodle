package kz.aspan.doodle.data.remote.ws.models

import kz.aspan.doodle.data.remote.ws.Room
import kz.aspan.doodle.util.Constants.TYPE_PHASE_CHANGE


data class PhaseChange(
    var phase: Room.Phase?,
    var time: Long,
    val drawingPlayer: String? = null
) : BaseModel(TYPE_PHASE_CHANGE)
