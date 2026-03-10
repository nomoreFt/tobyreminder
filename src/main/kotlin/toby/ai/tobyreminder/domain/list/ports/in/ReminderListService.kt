package toby.ai.tobyreminder.domain.list.ports.`in`

import toby.ai.tobyreminder.domain.list.ReminderListRequest
import toby.ai.tobyreminder.domain.list.ReminderListResponse
import toby.ai.tobyreminder.domain.list.ReorderRequest

interface ReminderListService {
    fun getLists(): List<ReminderListResponse>
    fun createList(request: ReminderListRequest): ReminderListResponse
    fun updateList(id: Long, request: ReminderListRequest): ReminderListResponse
    fun deleteList(id: Long)
    fun reorderLists(request: ReorderRequest)
}
