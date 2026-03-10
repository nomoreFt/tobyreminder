package toby.ai.tobyreminder.domain.reminder

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import toby.ai.tobyreminder.domain.list.ReminderListRepository
import toby.ai.tobyreminder.domain.reminder.ports.`in`.ReminderService
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class DefaultReminderService(
    private val reminderRepository: ReminderRepository,
    private val reminderListRepository: ReminderListRepository
) : ReminderService {

    override fun getRemindersByList(listId: Long): List<ReminderResponse> {
        reminderListRepository.findById(listId)
            .orElseThrow { NoSuchElementException("목록을 찾을 수 없습니다: $listId") }
        return reminderRepository.findByListIdAndIsCompletedFalseOrderBySortOrderAsc(listId)
            .map { ReminderResponse.from(it) }
    }

    override fun getByFilter(filter: ReminderFilter): List<ReminderResponse> {
        val reminders = when (filter) {
            ReminderFilter.TODAY      -> reminderRepository.findByDueDateAndIsCompletedFalse(LocalDate.now())
            ReminderFilter.SCHEDULED  -> reminderRepository.findByDueDateIsNotNullAndIsCompletedFalse()
            ReminderFilter.ALL        -> reminderRepository.findByIsCompletedFalse()
            ReminderFilter.FLAGGED    -> reminderRepository.findByIsFlaggedTrueAndIsCompletedFalse()
            ReminderFilter.COMPLETED  -> reminderRepository.findByIsCompletedTrue()
        }
        return reminders.map { ReminderResponse.from(it) }
    }

    @Transactional
    override fun createReminder(listId: Long, request: ReminderRequest): ReminderResponse {
        reminderListRepository.findById(listId)
            .orElseThrow { NoSuchElementException("목록을 찾을 수 없습니다: $listId") }
        val nextOrder = (reminderRepository.findByListIdOrderBySortOrderAsc(listId).lastOrNull()?.sortOrder ?: -1) + 1
        val reminder = reminderRepository.save(
            Reminder(
                listId = listId,
                title = request.title,
                notes = request.notes,
                dueDate = request.dueDate,
                dueTime = request.dueTime,
                priority = request.priority ?: Priority.NONE,
                isFlagged = request.isFlagged ?: false,
                sortOrder = nextOrder
            )
        )
        return ReminderResponse.from(reminder)
    }

    @Transactional
    override fun updateReminder(id: Long, request: ReminderRequest): ReminderResponse {
        val reminder = reminderRepository.findById(id)
            .orElseThrow { NoSuchElementException("리마인더를 찾을 수 없습니다: $id") }
        reminder.update(request)
        return ReminderResponse.from(reminder)
    }

    @Transactional
    override fun toggleComplete(id: Long): ReminderResponse {
        val reminder = reminderRepository.findById(id)
            .orElseThrow { NoSuchElementException("리마인더를 찾을 수 없습니다: $id") }
        if (reminder.isCompleted) {
            reminder.isCompleted = false
            reminder.completedAt = null
        } else {
            reminder.isCompleted = true
            reminder.completedAt = LocalDateTime.now()
        }
        return ReminderResponse.from(reminder)
    }

    @Transactional
    override fun deleteReminder(id: Long) {
        if (!reminderRepository.existsById(id)) throw NoSuchElementException("리마인더를 찾을 수 없습니다: $id")
        reminderRepository.deleteById(id)
    }

    @Transactional
    override fun reorderReminders(listId: Long, request: ReorderRequest) {
        reminderListRepository.findById(listId)
            .orElseThrow { NoSuchElementException("목록을 찾을 수 없습니다: $listId") }
        val reminders = reminderRepository.findByListIdOrderBySortOrderAsc(listId).associateBy { it.id }
        if (request.ids.size != reminders.size)
            throw IllegalArgumentException("IDs 수(${request.ids.size})가 목록의 리마인더 수(${reminders.size})와 다릅니다")
        request.ids.forEachIndexed { index, id ->
            val reminder = reminders[id]
                ?: throw IllegalArgumentException("reminder $id 는 목록 $listId 에 속하지 않습니다")
            reminder.sortOrder = index
        }
    }
}
