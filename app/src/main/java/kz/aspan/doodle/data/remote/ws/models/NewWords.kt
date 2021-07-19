package kz.aspan.doodle.data.remote.ws.models

import kz.aspan.doodle.util.Constants.TYPE_NEW_WORDS


data class NewWords(
    val newWords: List<String>
) : BaseModel(TYPE_NEW_WORDS)
