package kz.aspan.doodle.data.remote.responses

data class BasicApiResponse(
    val successful: Boolean,
    val message: String? = null
)
