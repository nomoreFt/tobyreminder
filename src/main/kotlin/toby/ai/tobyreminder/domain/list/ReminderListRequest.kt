package toby.ai.tobyreminder.domain.list

data class ReminderListRequest(
    val name: String,
    val color: String = "#007AFF",
    val icon: String = "list.bullet"
)
