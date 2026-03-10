package toby.ai.tobyreminder.domain.list

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import toby.ai.tobyreminder.domain.list.ports.`in`.ReminderListService
import toby.ai.tobyreminder.domain.reminder.Reminder
import toby.ai.tobyreminder.domain.reminder.ReminderRepository

@SpringBootTest
@Transactional
@DisplayName("ReminderListService")
class ReminderListServiceTest {

    @Autowired lateinit var service: ReminderListService
    @Autowired lateinit var reminderListRepository: ReminderListRepository
    @Autowired lateinit var reminderRepository: ReminderRepository

    @Nested
    @DisplayName("getLists()")
    inner class GetLists {

        @Test
        @DisplayName("sortOrder 순으로 정렬된 목록을 반환한다")
        fun `returns lists ordered by sortOrder`() {
            reminderListRepository.save(ReminderList(name = "업무", sortOrder = 1))
            val personal = reminderListRepository.save(ReminderList(name = "개인", sortOrder = 0))
            reminderRepository.save(Reminder(listId = personal.id, title = "할 일 1"))
            reminderRepository.save(Reminder(listId = personal.id, title = "할 일 2"))

            val result = service.getLists()

            assertThat(result[0].name).isEqualTo("개인")
            assertThat(result[0].reminderCount).isEqualTo(2)
            assertThat(result[1].name).isEqualTo("업무")
            assertThat(result[1].reminderCount).isEqualTo(0)
        }

        @Test
        @DisplayName("완료된 리마인더는 reminderCount에 포함되지 않는다")
        fun `completed reminders are not included in reminderCount`() {
            val list = reminderListRepository.save(ReminderList(name = "개인"))
            reminderRepository.save(Reminder(listId = list.id, title = "미완료"))
            reminderRepository.save(Reminder(listId = list.id, title = "완료됨", isCompleted = true))

            val result = service.getLists()

            assertThat(result[0].reminderCount).isEqualTo(1)
        }

        @Test
        @DisplayName("목록이 없으면 빈 리스트를 반환한다")
        fun `returns empty list when no lists exist`() {
            val result = service.getLists()

            assertThat(result).isEmpty()
        }
    }

    @Nested
    @DisplayName("createList()")
    inner class CreateList {

        @Test
        @DisplayName("첫 번째 목록은 sortOrder 0으로 생성된다")
        fun `first list is created with sortOrder 0`() {
            val result = service.createList(ReminderListRequest(name = "개인"))

            assertThat(result.sortOrder).isEqualTo(0)
        }

        @Test
        @DisplayName("기존 목록의 최대 sortOrder + 1로 생성된다")
        fun `creates list with sortOrder max + 1`() {
            reminderListRepository.save(ReminderList(name = "개인", sortOrder = 0))
            reminderListRepository.save(ReminderList(name = "업무", sortOrder = 1))

            val result = service.createList(ReminderListRequest(name = "쇼핑"))

            assertThat(result.sortOrder).isEqualTo(2)
        }

        @Test
        @DisplayName("요청한 이름, 색상, 아이콘으로 생성된다")
        fun `creates list with given name, color, icon`() {
            val result = service.createList(
                ReminderListRequest(name = "업무", color = "#FF3B30", icon = "briefcase")
            )

            assertThat(result.name).isEqualTo("업무")
            assertThat(result.color).isEqualTo("#FF3B30")
            assertThat(result.icon).isEqualTo("briefcase")
        }

        @Test
        @DisplayName("색상과 아이콘을 지정하지 않으면 기본값으로 생성된다")
        fun `creates list with default color and icon`() {
            val result = service.createList(ReminderListRequest(name = "개인"))

            assertThat(result.color).isEqualTo("#007AFF")
            assertThat(result.icon).isEqualTo("list.bullet")
        }
    }

    @Nested
    @DisplayName("updateList()")
    inner class UpdateList {

        @Test
        @DisplayName("이름, 색상, 아이콘을 수정할 수 있다")
        fun `updates name, color, and icon`() {
            val saved = reminderListRepository.save(
                ReminderList(name = "개인", color = "#007AFF", icon = "list.bullet")
            )

            val result = service.updateList(
                saved.id,
                ReminderListRequest(name = "수정됨", color = "#FF3B30", icon = "star")
            )

            assertThat(result.name).isEqualTo("수정됨")
            assertThat(result.color).isEqualTo("#FF3B30")
            assertThat(result.icon).isEqualTo("star")
        }

        @Test
        @DisplayName("존재하지 않는 id로 수정하면 NoSuchElementException이 발생한다")
        fun `throws NoSuchElementException when list not found`() {
            assertThatThrownBy {
                service.updateList(999L, ReminderListRequest(name = "없음"))
            }.isInstanceOf(NoSuchElementException::class.java)
                .hasMessageContaining("999")
        }
    }

    @Nested
    @DisplayName("deleteList()")
    inner class DeleteList {

        @Test
        @DisplayName("목록을 삭제하면 DB에서 제거된다")
        fun `deleted list is removed from DB`() {
            val saved = reminderListRepository.save(ReminderList(name = "삭제할 목록"))

            service.deleteList(saved.id)

            assertThat(reminderListRepository.existsById(saved.id)).isFalse()
        }

        @Test
        @DisplayName("목록 삭제 시 소속 리마인더도 함께 삭제된다")
        fun `deletes all reminders belonging to the list`() {
            val list = reminderListRepository.save(ReminderList(name = "삭제할 목록"))
            reminderRepository.save(Reminder(listId = list.id, title = "리마인더 1"))
            reminderRepository.save(Reminder(listId = list.id, title = "리마인더 2"))

            service.deleteList(list.id)

            assertThat(reminderRepository.findByListIdOrderBySortOrderAsc(list.id)).isEmpty()
        }

        @Test
        @DisplayName("존재하지 않는 id로 삭제하면 NoSuchElementException이 발생한다")
        fun `throws NoSuchElementException when list not found`() {
            assertThatThrownBy {
                service.deleteList(999L)
            }.isInstanceOf(NoSuchElementException::class.java)
                .hasMessageContaining("999")
        }
    }

    @Nested
    @DisplayName("reorderLists()")
    inner class ReorderLists {

        @Test
        @DisplayName("전달된 id 순서대로 sortOrder가 0부터 재할당된다")
        fun `reassigns sortOrder from 0 based on given id order`() {
            val list1 = reminderListRepository.save(ReminderList(name = "개인", sortOrder = 0))
            val list2 = reminderListRepository.save(ReminderList(name = "업무", sortOrder = 1))

            service.reorderLists(ReorderRequest(ids = listOf(list2.id, list1.id)))

            assertThat(list2.sortOrder).isEqualTo(0)
            assertThat(list1.sortOrder).isEqualTo(1)
        }

        @Test
        @DisplayName("존재하지 않는 id가 포함되면 NoSuchElementException이 발생한다")
        fun `throws NoSuchElementException when unknown id is included`() {
            assertThatThrownBy {
                service.reorderLists(ReorderRequest(ids = listOf(999L)))
            }.isInstanceOf(NoSuchElementException::class.java)
        }
    }
}
