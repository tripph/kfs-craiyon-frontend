@kotlinx.serialization.Serializable
data class PatchReq(
    val itemTs: String, //timestamp used as primary key, kill me
    val patchAction: PatchAction
)

@kotlinx.serialization.Serializable
data class PatchResp(
    val itemTs: String,
    val newValue: FeedEntry
)
@kotlinx.serialization.Serializable
enum class PatchAction {
    LIKE
}




@kotlinx.serialization.Serializable
data class GenerateReq(
    val prompt: String? = null
)
@kotlinx.serialization.Serializable
data class GenerateResponse(
    val version: String? = null,
    var images: List<String>
)
data class Piece(val text: String, var results: List<String> = emptyList(), val ts: String? = null)


@kotlinx.serialization.Serializable
data class FeedEntry(
    val username: String? = null,
    val prompt: String? = null,
    var images: List<String>? = null, // if null or empty, request errored
    var ts: String? = null,
    var likes: Int? = 0
)
