package toby.ai.tobyreminder.domain.list

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
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
import toby.ai.tobyreminder.domain.list.ports.`in`.ReminderListService
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDateTime

@SpringBootTest
@DisplayName("ReminderListController")
class ReminderListControllerTest {

    @Autowired lateinit var context: WebApplicationContext
    @MockitoBean lateinit var reminderListService: ReminderListService

    private lateinit var mockMvc: MockMvc
    private val mapper = JsonMapper.builder().build()

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    private fun sampleResponse(id: Long = 1L, name: String = "개인", sortOrder: Int = 0) =
        ReminderListResponse(
            id = id,
            name = name,
            color = "#007AFF",
            icon = "list.bullet",
            sortOrder = sortOrder,
            reminderCount = 0,
            createdAt = LocalDateTime.of(2026, 3, 10, 12, 0)
        )

    private fun toJson(obj: Any): String = mapper.writeValueAsString(obj)

    @Nested
    @DisplayName("GET /api/lists")
    inner class GetLists {

        @Test
        @DisplayName("200과 목록 배열을 반환한다")
        fun `returns 200 with list array`() {
            whenever(reminderListService.getLists()).thenReturn(
                listOf(sampleResponse(1L, "개인", 0), sampleResponse(2L, "업무", 1))
            )

            mockMvc.get("/api/lists")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].name") { value("개인") }
                    jsonPath("$[1].name") { value("업무") }
                }
        }

        @Test
        @DisplayName("목록이 없으면 빈 배열을 반환한다")
        fun `returns empty array when no lists`() {
            whenever(reminderListService.getLists()).thenReturn(emptyList())

            mockMvc.get("/api/lists")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()") { value(0) }
                }
        }
    }

    @Nested
    @DisplayName("POST /api/lists")
    inner class CreateList {

        @Test
        @DisplayName("201과 생성된 목록을 반환한다")
        fun `returns 201 with created list`() {
            whenever(reminderListService.createList(any())).thenReturn(sampleResponse(name = "개인"))

            mockMvc.post("/api/lists") {
                contentType = MediaType.APPLICATION_JSON
                content = toJson(ReminderListRequest(name = "개인", color = "#FF3B30", icon = "briefcase"))
            }.andExpect {
                status { isCreated() }
                jsonPath("$.name") { value("개인") }
                jsonPath("$.id") { value(1) }
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/lists/{id}")
    inner class UpdateList {

        @Test
        @DisplayName("200과 수정된 목록을 반환한다")
        fun `returns 200 with updated list`() {
            whenever(reminderListService.updateList(any(), any())).thenReturn(
                sampleResponse(name = "수정됨")
            )

            mockMvc.put("/api/lists/1") {
                contentType = MediaType.APPLICATION_JSON
                content = toJson(ReminderListRequest(name = "수정됨", color = "#34C759", icon = "star"))
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("수정됨") }
            }
        }

        @Test
        @DisplayName("존재하지 않는 id이면 404를 반환한다")
        fun `returns 404 when list not found`() {
            whenever(reminderListService.updateList(any(), any()))
                .thenThrow(NoSuchElementException("목록을 찾을 수 없습니다: 999"))

            mockMvc.put("/api/lists/999") {
                contentType = MediaType.APPLICATION_JSON
                content = toJson(ReminderListRequest(name = "없음", color = "#007AFF", icon = "list.bullet"))
            }.andExpect {
                status { isNotFound() }
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/lists/{id}")
    inner class DeleteList {

        @Test
        @DisplayName("204를 반환한다")
        fun `returns 204`() {
            mockMvc.delete("/api/lists/1")
                .andExpect {
                    status { isNoContent() }
                }
        }

        @Test
        @DisplayName("존재하지 않는 id이면 404를 반환한다")
        fun `returns 404 when list not found`() {
            whenever(reminderListService.deleteList(any()))
                .thenThrow(NoSuchElementException("목록을 찾을 수 없습니다: 999"))

            mockMvc.delete("/api/lists/999")
                .andExpect {
                    status { isNotFound() }
                }
        }
    }

    @Nested
    @DisplayName("PATCH /api/lists/reorder")
    inner class ReorderLists {

        @Test
        @DisplayName("204를 반환한다")
        fun `returns 204`() {
            mockMvc.patch("/api/lists/reorder") {
                contentType = MediaType.APPLICATION_JSON
                content = toJson(ReorderRequest(ids = listOf(2L, 1L, 3L)))
            }.andExpect {
                status { isNoContent() }
            }
        }
    }

    @Nested
    @DisplayName("입력 검증")
    inner class Validation {

        @Test
        @DisplayName("POST: name이 빈 문자열이면 400을 반환한다")
        fun `returns 400 when name is blank on create`() {
            mockMvc.post("/api/lists") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name": ""}"""
            }.andExpect { status { isBadRequest() } }
        }

        @Test
        @DisplayName("PUT: name이 공백만이면 400을 반환한다")
        fun `returns 400 when name is whitespace on update`() {
            mockMvc.put("/api/lists/1") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name": "   "}"""
            }.andExpect { status { isBadRequest() } }
        }
    }
}
