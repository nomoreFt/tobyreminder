package toby.ai.tobyreminder.domain.list

data class ReminderListRequest(
    val name: String,
    val color: String? = null,
    val icon: String? = null
)
