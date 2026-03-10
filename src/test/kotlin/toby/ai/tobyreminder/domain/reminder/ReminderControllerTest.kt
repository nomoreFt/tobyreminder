package toby.ai.tobyreminder.domain.reminder

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import toby.ai.tobyreminder.domain.reminder.ports.`in`.ReminderService
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@DisplayName("ReminderController")
class ReminderControllerTest {

    @Autowired lateinit var context: WebApplicationContext
    @MockitoBean lateinit var reminderService: ReminderService

    private lateinit var mockMvc: MockMvc
    private val mapper = JsonMapper.builder().build()

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    private fun sampleResponse(
        id: Long = 1L,
        listId: Long = 1L,
        title: String = "장보기"
    ) = ReminderResponse(
        id = id,
        listId = listId,
        title = title,
        notes = null,
        dueDate = LocalDate.of(2026, 3, 11),
        dueTime = null,
        priority = Priority.NONE,
        isFlagged = false,
        isCompleted = false,
        completedAt = null,
        sortOrder = 0,
        createdAt = LocalDateTime.of(2026, 3, 10, 12, 0)
    )

    private fun toJson(obj: Any): String = mapper.writeValueAsString(obj)

    @Nested
    @DisplayName("GET /api/lists/{listId}/reminders")
    inner class GetRemindersByList {

        @Test
        @DisplayName("200과 리마인더 배열을 반환한다")
        fun `returns 200 with reminder array`() {
            whenever(reminderService.getRemindersByList(1L)).thenReturn(
                listOf(sampleResponse(1L, title = "장보기"), sampleResponse(2L, title = "운동"))
            )

            mockMvc.get("/api/lists/1/reminders")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].title") { value("장보기") }
                    jsonPath("$[1].title") { value("운동") }
                }
        }

        @Test
        @DisplayName("목록을 찾을 수 없으면 404를 반환한다")
        fun `returns 404 when list not found`() {
            whenever(reminderService.getRemindersByList(999L))
                .thenThrow(NoSuchElementException("목록을 찾을 수 없습니다: 999"))

            mockMvc.get("/api/lists/999/reminders")
                .andExpect { status { isNotFound() } }
        }
    }

    @Nested
    @DisplayName("POST /api/lists/{listId}/reminders")
    inner class CreateReminder {

        @Test
        @DisplayName("201과 생성된 리마인더를 반환한다")
        fun `returns 201 with created reminder`() {
            whenever(reminderService.createReminder(eq(1L), any())).thenReturn(sampleResponse(title = "장보기"))

            mockMvc.post("/api/lists/1/reminders") {
                contentType = MediaType.APPLICATION_JSON
                content = toJson(ReminderRequest(title = "장보기", dueDate = LocalDate.of(2026, 3, 11)))
            }.andExpect {
                status { isCreated() }
                jsonPath("$.title") { value("장보기") }
                jsonPath("$.id") { value(1) }
            }
        }

        @Test
        @DisplayName("목록을 찾을 수 없으면 404를 반환한다")
        fun `returns 404 when list not found`() {
            whenever(reminderService.createReminder(eq(999L), any()))
                .thenThrow(NoSuchElementException("목록을 찾을 수 없습니다: 999"))

            mockMvc.post("/api/lists/999/reminders") {
                contentType = MediaType.APPLICATION_JSON
                content = toJson(ReminderRequest(title = "테스트"))
            }.andExpect { status { isNotFound() } }
        }
    }

    @Nested
    @DisplayName("PUT /api/reminders/{id}")
    inner class UpdateReminder {

        @Test
        @DisplayName("200과 수정된 리마인더를 반환한다")
        fun `returns 200 with updated reminder`() {
            whenever(reminderService.updateReminder(eq(1L), any()))
                .thenReturn(sampleResponse(title = "수정됨"))

            mockMvc.put("/api/reminders/1") {
                contentType = MediaType.APPLICATION_JSON
                content = toJson(ReminderRequest(title = "수정됨", priority = Priority.HIGH))
            }.andExpect {
                status { isOk() }
                jsonPath("$.title") { value("수정됨") }
            }
        }

        @Test
        @DisplayName("존재하지 않는 id이면 404를 반환한다")
        fun `returns 404 when reminder not found`() {
            whenever(reminderService.updateReminder(eq(999L), any()))
                .thenThrow(NoSuchElementException("리마인더를 찾을 수 없습니다: 999"))

            mockMvc.put("/api/reminders/999") {
                contentType = MediaType.APPLICATION_JSON
                content = toJson(ReminderRequest(title = "없음"))
            }.andExpect { status { isNotFound() } }
        }
    }

    @Nested
    @DisplayName("PATCH /api/reminders/{id}/complete")
    inner class ToggleComplete {

        @Test
        @DisplayName("200과 토글된 리마인더를 반환한다")
        fun `returns 200 with toggled reminder`() {
            val completed = sampleResponse().copy(isCompleted = true, completedAt = LocalDateTime.now())
            whenever(reminderService.toggleComplete(1L)).thenReturn(completed)

            mockMvc.patch("/api/reminders/1/complete")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.isCompleted") { value(true) }
                }
        }

        @Test
        @DisplayName("존재하지 않는 id이면 404를 반환한다")
        fun `returns 404 when reminder not found`() {
            whenever(reminderService.toggleComplete(999L))
                .thenThrow(NoSuchElementException("리마인더를 찾을 수 없습니다: 999"))

            mockMvc.patch("/api/reminders/999/complete")
                .andExpect { status { isNotFound() } }
        }
    }

    @Nested
    @DisplayName("DELETE /api/reminders/{id}")
    inner class DeleteReminder {

        @Test
        @DisplayName("204를 반환한다")
        fun `returns 204`() {
            mockMvc.delete("/api/reminders/1")
                .andExpect { status { isNoContent() } }
        }

        @Test
        @DisplayName("존재하지 않는 id이면 404를 반환한다")
        fun `returns 404 when reminder not found`() {
            whenever(reminderService.deleteReminder(999L))
                .thenThrow(NoSuchElementException("리마인더를 찾을 수 없습니다: 999"))

            mockMvc.delete("/api/reminders/999")
                .andExpect { status { isNotFound() } }
        }
    }

    @Nested
    @DisplayName("PATCH /api/lists/{listId}/reminders/reorder")
    inner class ReorderReminders {

        @Test
        @DisplayName("204를 반환한다")
        fun `returns 204`() {
            mockMvc.patch("/api/lists/1/reminders/reorder") {
                contentType = MediaType.APPLICATION_JSON
                content = toJson(ReorderRequest(ids = listOf(3L, 1L, 2L)))
            }.andExpect { status { isNoContent() } }
        }
    }

    @Nested
    @DisplayName("GET /api/reminders?filter=")
    inner class GetRemindersByFilter {

        @Test
        @DisplayName("200과 필터된 리마인더를 반환한다")
        fun `returns 200 with filtered reminders`() {
            whenever(reminderService.getByFilter(ReminderFilter.TODAY))
                .thenReturn(listOf(sampleResponse(title = "오늘 할 일")))

            mockMvc.get("/api/reminders") { param("filter", "today") }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()") { value(1) }
                    jsonPath("$[0].title") { value("오늘 할 일") }
                }
        }

        @Test
        @DisplayName("유효하지 않은 filter 값이면 400을 반환한다")
        fun `returns 400 for invalid filter`() {
            mockMvc.get("/api/reminders") { param("filter", "invalid") }
                .andExpect { status { isBadRequest() } }
        }
    }
}
