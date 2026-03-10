package toby.ai.tobyreminder.domain.reminder

enum class ReminderFilter(val value: String) {
    TODAY("today"),
    SCHEDULED("scheduled"),
    ALL("all"),
    FLAGGED("flagged"),
    COMPLETED("completed");

    companion object {
        fun from(value: String): ReminderFilter =
            entries.find { it.value == value.lowercase() }
                ?: throw IllegalArgumentException("유효하지 않은 필터: $value")
    }
}
