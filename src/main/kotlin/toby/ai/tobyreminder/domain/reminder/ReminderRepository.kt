package toby.ai.tobyreminder.domain.reminder

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ReminderRepository : JpaRepository<Reminder, Long> {
    fun findByListIdAndIsCompletedFalseOrderBySortOrderAsc(listId: Long): List<Reminder>
    fun findByListIdOrderBySortOrderAsc(listId: Long): List<Reminder>
    fun deleteAllByListId(listId: Long)
    fun countByListIdAndIsCompletedFalse(listId: Long): Int
    fun findByDueDateAndIsCompletedFalse(dueDate: LocalDate): List<Reminder>
    fun findByDueDateIsNotNullAndIsCompletedFalse(): List<Reminder>
    fun findByIsCompletedFalse(): List<Reminder>
    fun findByIsFlaggedTrueAndIsCompletedFalse(): List<Reminder>
    fun findByIsCompletedTrue(): List<Reminder>
}
