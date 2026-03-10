package toby.ai.tobyreminder.domain.list

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import toby.ai.tobyreminder.domain.list.ports.`in`.ReminderListService
import toby.ai.tobyreminder.domain.reminder.ReminderRepository

@Service
@Transactional(readOnly = true)
class DefaultReminderListService(
    private val reminderListRepository: ReminderListRepository,
    private val reminderRepository: ReminderRepository
) : ReminderListService {

    override fun getLists(): List<ReminderListResponse> {
        val lists = reminderListRepository.findAllByOrderBySortOrderAsc()
        return lists.map { list ->
            val count = reminderRepository.countByListIdAndIsCompletedFalse(list.id)
            ReminderListResponse.from(list, count)
        }
    }

    @Transactional
    override fun createList(request: ReminderListRequest): ReminderListResponse {
        val maxSortOrder = reminderListRepository.findAllByOrderBySortOrderAsc()
            .maxOfOrNull { it.sortOrder } ?: -1
        val list = ReminderList(
            name = request.name,
            color = request.color ?: "#007AFF",
            icon = request.icon ?: "list.bullet",
            sortOrder = maxSortOrder + 1
        )
        val saved = reminderListRepository.save(list)
        return ReminderListResponse.from(saved)
    }

    @Transactional
    override fun updateList(id: Long, request: ReminderListRequest): ReminderListResponse {
        val list = reminderListRepository.findById(id)
            .orElseThrow { NoSuchElementException("목록을 찾을 수 없습니다: $id") }
        list.update(name = request.name, color = request.color ?: list.color, icon = request.icon ?: list.icon)
        return ReminderListResponse.from(list)
    }

    @Transactional
    override fun deleteList(id: Long) {
        if (!reminderListRepository.existsById(id)) {
            throw NoSuchElementException("목록을 찾을 수 없습니다: $id")
        }
        reminderRepository.deleteAllByListId(id)
        reminderListRepository.deleteById(id)
    }

    @Transactional
    override fun reorderLists(request: ReorderRequest) {
        request.ids.forEachIndexed { index, id ->
            val list = reminderListRepository.findById(id)
                .orElseThrow { NoSuchElementException("목록을 찾을 수 없습니다: $id") }
            list.sortOrder = index
        }
    }
}
