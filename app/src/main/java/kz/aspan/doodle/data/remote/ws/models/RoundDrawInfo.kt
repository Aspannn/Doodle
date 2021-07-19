package kz.aspan.data.models

import kz.aspan.doodle.data.remote.ws.models.BaseModel
import kz.aspan.doodle.util.Constants.TYPE_CUR_ROUND_DRAW_INFO


data class RoundDrawInfo(
    val data: List<String>
) : BaseModel(TYPE_CUR_ROUND_DRAW_INFO)
