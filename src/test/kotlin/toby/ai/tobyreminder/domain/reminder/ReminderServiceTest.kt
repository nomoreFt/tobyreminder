package toby.ai.tobyreminder.domain.reminder

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import toby.ai.tobyreminder.domain.list.ReminderList
import toby.ai.tobyreminder.domain.list.ReminderListRepository
import toby.ai.tobyreminder.domain.reminder.ports.`in`.ReminderService
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@Transactional
@DisplayName("ReminderService")
class ReminderServiceTest {

    @Autowired lateinit var service: ReminderService
    @Autowired lateinit var reminderListRepository: ReminderListRepository
    @Autowired lateinit var reminderRepository: ReminderRepository

    private fun saveList(name: String = "테스트") =
        reminderListRepository.save(ReminderList(name = name))

    private fun saveReminder(
        listId: Long,
        title: String = "리마인더",
        sortOrder: Int = 0,
        isCompleted: Boolean = false,
        isFlagged: Boolean = false,
        dueDate: LocalDate? = null
    ) = reminderRepository.save(
        Reminder(
            listId = listId,
            title = title,
            sortOrder = sortOrder,
            isCompleted = isCompleted,
            isFlagged = isFlagged,
            dueDate = dueDate
        )
    )

    @Nested
    @DisplayName("getRemindersByList()")
    inner class GetRemindersByList {

        @Test
        @DisplayName("목록의 미완료 리마인더를 sortOrder 오름차순으로 반환한다")
        fun `returns incomplete reminders ordered by sortOrder`() {
            val list = saveList()
            saveReminder(list.id, "두번째", sortOrder = 1)
            saveReminder(list.id, "첫번째", sortOrder = 0)
            saveReminder(list.id, "완료됨", isCompleted = true)

            val result = service.getRemindersByList(list.id)

            assertThat(result).hasSize(2)
            assertThat(result[0].title).isEqualTo("첫번째")
            assertThat(result[1].title).isEqualTo("두번째")
        }

        @Test
        @DisplayName("리마인더가 없으면 빈 목록을 반환한다")
        fun `returns empty list when no reminders`() {
            val list = saveList()
            assertThat(service.getRemindersByList(list.id)).isEmpty()
        }

        @Test
        @DisplayName("존재하지 않는 목록 id이면 예외를 던진다")
        fun `throws when list not found`() {
            assertThatThrownBy { service.getRemindersByList(9999L) }
                .isInstanceOf(NoSuchElementException::class.java)
        }
    }

    @Nested
    @DisplayName("getByFilter()")
    inner class GetByFilter {

        @Test
        @DisplayName("TODAY: 오늘 마감이고 미완료인 리마인더만 반환한다")
        fun `today filter returns today's incomplete reminders`() {
            val list = saveList()
            saveReminder(list.id, "오늘", dueDate = LocalDate.now())
            saveReminder(list.id, "내일", dueDate = LocalDate.now().plusDays(1))
            saveReminder(list.id, "오늘완료", dueDate = LocalDate.now(), isCompleted = true)

            val result = service.getByFilter(ReminderFilter.TODAY)

            assertThat(result).hasSize(1)
            assertThat(result[0].title).isEqualTo("오늘")
        }

        @Test
        @DisplayName("SCHEDULED: 마감일이 있고 미완료인 리마인더를 반환한다")
        fun `scheduled filter returns reminders with due date`() {
            val list = saveList()
            saveReminder(list.id, "마감있음", dueDate = LocalDate.now().plusDays(3))
            saveReminder(list.id, "마감없음")

            val result = service.getByFilter(ReminderFilter.SCHEDULED)

            assertThat(result).hasSize(1)
            assertThat(result[0].title).isEqualTo("마감있음")
        }

        @Test
        @DisplayName("ALL: 미완료 리마인더 전체를 반환한다")
        fun `all filter returns all incomplete reminders`() {
            val list = saveList()
            saveReminder(list.id, "미완료1")
            saveReminder(list.id, "미완료2")
            saveReminder(list.id, "완료됨", isCompleted = true)

            val result = service.getByFilter(ReminderFilter.ALL)

            assertThat(result).hasSize(2)
            assertThat(result.map { it.title }).containsExactlyInAnyOrder("미완료1", "미완료2")
        }

        @Test
        @DisplayName("FLAGGED: 플래그가 있고 미완료인 리마인더를 반환한다")
        fun `flagged filter returns flagged incomplete reminders`() {
            val list = saveList()
            saveReminder(list.id, "플래그됨", isFlagged = true)
            saveReminder(list.id, "플래그없음")

            val result = service.getByFilter(ReminderFilter.FLAGGED)

            assertThat(result).hasSize(1)
            assertThat(result[0].title).isEqualTo("플래그됨")
        }

        @Test
        @DisplayName("COMPLETED: 완료된 리마인더를 반환한다")
        fun `completed filter returns completed reminders`() {
            val list = saveList()
            saveReminder(list.id, "완료됨", isCompleted = true)
            saveReminder(list.id, "미완료")

            val result = service.getByFilter(ReminderFilter.COMPLETED)

            assertThat(result).hasSize(1)
            assertThat(result[0].title).isEqualTo("완료됨")
        }
    }

    @Nested
    @DisplayName("createReminder()")
    inner class CreateReminder {

        @Test
        @DisplayName("리마인더를 생성하고 반환한다")
        fun `creates and returns reminder`() {
            val list = saveList()
            val request = ReminderRequest(title = "장보기", isFlagged = true)

            val result = service.createReminder(list.id, request)

            assertThat(result.title).isEqualTo("장보기")
            assertThat(result.listId).isEqualTo(list.id)
            assertThat(result.isFlagged).isTrue()
            assertThat(result.id).isPositive()
        }

        @Test
        @DisplayName("sortOrder는 기존 최대값 + 1로 설정된다")
        fun `sortOrder is max + 1`() {
            val list = saveList()
            saveReminder(list.id, "첫번째", sortOrder = 0)
            saveReminder(list.id, "두번째", sortOrder = 1)

            val result = service.createReminder(list.id, ReminderRequest(title = "세번째"))

            assertThat(result.sortOrder).isEqualTo(2)
        }

        @Test
        @DisplayName("존재하지 않는 목록 id이면 예외를 던진다")
        fun `throws when list not found`() {
            assertThatThrownBy { service.createReminder(9999L, ReminderRequest(title = "테스트")) }
                .isInstanceOf(NoSuchElementException::class.java)
        }
    }

    @Nested
    @DisplayName("updateReminder()")
    inner class UpdateReminder {

        @Test
        @DisplayName("리마인더 필드를 수정하고 반환한다")
        fun `updates reminder fields`() {
            val list = saveList()
            val reminder = saveReminder(list.id, "원본")
            val request = ReminderRequest(
                title = "수정됨",
                notes = "메모",
                dueDate = LocalDate.of(2026, 4, 1),
                priority = Priority.HIGH,
                isFlagged = true
            )

            val result = service.updateReminder(reminder.id, request)

            assertThat(result.title).isEqualTo("수정됨")
            assertThat(result.notes).isEqualTo("메모")
            assertThat(result.priority).isEqualTo(Priority.HIGH)
            assertThat(result.isFlagged).isTrue()
        }

        @Test
        @DisplayName("존재하지 않는 id이면 예외를 던진다")
        fun `throws when reminder not found`() {
            assertThatThrownBy { service.updateReminder(9999L, ReminderRequest(title = "없음")) }
                .isInstanceOf(NoSuchElementException::class.java)
        }
    }

    @Nested
    @DisplayName("toggleComplete()")
    inner class ToggleComplete {

        @Test
        @DisplayName("미완료 → 완료: isCompleted=true, completedAt이 기록된다")
        fun `marks as complete with completedAt`() {
            val list = saveList()
            val reminder = saveReminder(list.id)
            val before = LocalDateTime.now()

            val result = service.toggleComplete(reminder.id)

            assertThat(result.isCompleted).isTrue()
            assertThat(result.completedAt).isAfterOrEqualTo(before)
        }

        @Test
        @DisplayName("완료 → 미완료: isCompleted=false, completedAt이 null이 된다")
        fun `marks as incomplete and clears completedAt`() {
            val list = saveList()
            val reminder = saveReminder(list.id, isCompleted = true)

            val result = service.toggleComplete(reminder.id)

            assertThat(result.isCompleted).isFalse()
            assertThat(result.completedAt).isNull()
        }

        @Test
        @DisplayName("존재하지 않는 id이면 예외를 던진다")
        fun `throws when reminder not found`() {
            assertThatThrownBy { service.toggleComplete(9999L) }
                .isInstanceOf(NoSuchElementException::class.java)
        }
    }

    @Nested
    @DisplayName("deleteReminder()")
    inner class DeleteReminder {

        @Test
        @DisplayName("리마인더를 삭제한다")
        fun `deletes reminder`() {
            val list = saveList()
            val reminder = saveReminder(list.id)

            service.deleteReminder(reminder.id)

            assertThat(reminderRepository.existsById(reminder.id)).isFalse()
        }

        @Test
        @DisplayName("존재하지 않는 id이면 예외를 던진다")
        fun `throws when reminder not found`() {
            assertThatThrownBy { service.deleteReminder(9999L) }
                .isInstanceOf(NoSuchElementException::class.java)
        }
    }

    @Nested
    @DisplayName("reorderReminders()")
    inner class ReorderReminders {

        @Test
        @DisplayName("id 배열 순서대로 sortOrder를 재할당한다")
        fun `reassigns sortOrder by id array order`() {
            val list = saveList()
            val r1 = saveReminder(list.id, "A", sortOrder = 0)
            val r2 = saveReminder(list.id, "B", sortOrder = 1)
            val r3 = saveReminder(list.id, "C", sortOrder = 2)

            service.reorderReminders(list.id, ReorderRequest(ids = listOf(r3.id, r1.id, r2.id)))

            val reordered = reminderRepository.findByListIdOrderBySortOrderAsc(list.id)
            assertThat(reordered.map { it.title }).containsExactly("C", "A", "B")
        }

        @Test
        @DisplayName("존재하지 않는 목록 id이면 예외를 던진다")
        fun `throws when list not found`() {
            assertThatThrownBy {
                service.reorderReminders(9999L, ReorderRequest(ids = listOf(1L)))
            }.isInstanceOf(NoSuchElementException::class.java)
        }
    }
}
