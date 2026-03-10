package toby.ai.tobyreminder.domain.list

import org.springframework.data.jpa.repository.JpaRepository

interface ReminderListRepository : JpaRepository<ReminderList, Long> {
    fun findAllByOrderBySortOrderAsc(): List<ReminderList>
}
