package toby.ai.tobyreminder.config

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import toby.ai.tobyreminder.domain.list.ports.`in`.ReminderListService

@SpringBootTest
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Autowired lateinit var context: WebApplicationContext
    @MockitoBean lateinit var reminderListService: ReminderListService

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    @DisplayName("잘못된 JSON 전달 시 400을 반환한다")
    fun `returns 400 for malformed JSON`() {
        mockMvc.post("/api/lists") {
            contentType = MediaType.APPLICATION_JSON
            content = "{ invalid json }"
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    @DisplayName("서비스에서 RuntimeException 발생 시 500을 반환한다")
    fun `returns 500 for unexpected RuntimeException`() {
        whenever(reminderListService.getLists()).thenThrow(RuntimeException("예기치 못한 오류"))

        mockMvc.get("/api/lists")
            .andExpect { status { isInternalServerError() } }
    }
}
