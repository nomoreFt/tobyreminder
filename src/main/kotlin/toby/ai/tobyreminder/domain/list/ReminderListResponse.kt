package toby.ai.tobyreminder.domain.list

import java.time.LocalDateTime

data class ReminderListResponse(
    val id: Long,
    val name: String,
    val color: String,
    val icon: String,
    val sortOrder: Int,
    val reminderCount: Int,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(list: ReminderList, reminderCount: Int = 0) = ReminderListResponse(
            id = list.id,
            name = list.name,
            color = list.color,
            icon = list.icon,
            sortOrder = list.sortOrder,
            reminderCount = reminderCount,
            createdAt = list.createdAt
        )
    }
}
