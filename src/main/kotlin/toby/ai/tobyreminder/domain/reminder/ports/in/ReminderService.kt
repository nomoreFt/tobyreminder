package toby.ai.tobyreminder.domain.reminder.ports.`in`

import toby.ai.tobyreminder.domain.reminder.ReminderFilter
import toby.ai.tobyreminder.domain.reminder.ReminderRequest
import toby.ai.tobyreminder.domain.reminder.ReminderResponse
import toby.ai.tobyreminder.domain.reminder.ReorderRequest

interface ReminderService {
    fun getRemindersByList(listId: Long): List<ReminderResponse>
    fun getByFilter(filter: ReminderFilter): List<ReminderResponse>
    fun createReminder(listId: Long, request: ReminderRequest): ReminderResponse
    fun updateReminder(id: Long, request: ReminderRequest): ReminderResponse
    fun toggleComplete(id: Long): ReminderResponse
    fun deleteReminder(id: Long)
    fun reorderReminders(listId: Long, request: ReorderRequest)
}
