package toby.ai.tobyreminder.domain.reminder

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ReminderResponse(
    val id: Long,
    val listId: Long,
    val title: String,
    val notes: String?,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val priority: Priority,
    @JsonProperty("isFlagged") val isFlagged: Boolean,
    @JsonProperty("isCompleted") val isCompleted: Boolean,
    val completedAt: LocalDateTime?,
    val sortOrder: Int,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(reminder: Reminder) = ReminderResponse(
            id = reminder.id,
            listId = reminder.listId,
            title = reminder.title,
            notes = reminder.notes,
            dueDate = reminder.dueDate,
            dueTime = reminder.dueTime,
            priority = reminder.priority,
            isFlagged = reminder.isFlagged,
            isCompleted = reminder.isCompleted,
            completedAt = reminder.completedAt,
            sortOrder = reminder.sortOrder,
            createdAt = reminder.createdAt
        )
    }
}
