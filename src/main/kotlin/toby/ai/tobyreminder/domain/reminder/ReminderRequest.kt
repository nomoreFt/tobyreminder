package toby.ai.tobyreminder.domain.reminder

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalTime

data class ReminderRequest(
    val title: String,
    val notes: String? = null,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val priority: Priority? = null,
    @JsonProperty("isFlagged") val isFlagged: Boolean? = null
)
