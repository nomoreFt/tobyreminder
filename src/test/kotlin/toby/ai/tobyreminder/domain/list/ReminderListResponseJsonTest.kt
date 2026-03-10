package toby.ai.tobyreminder.domain.list

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDateTime

@DisplayName("ReminderListResponse JSON 직렬화/역직렬화")
class ReminderListResponseJsonTest {

    private val mapper = JsonMapper.builder().build()

    private val sample = ReminderListResponse(
        id = 1L,
        name = "개인",
        color = "#007AFF",
        icon = "list.bullet",
        sortOrder = 0,
        reminderCount = 3,
        createdAt = LocalDateTime.of(2026, 3, 10, 12, 0)
    )

    @Nested
    @DisplayName("직렬화 (serialization)")
    inner class Serialization {

        @Test
        @DisplayName("createdAt이 ISO-8601 문자열로 직렬화된다")
        fun `createdAt is serialized as ISO-8601 string`() {
            val json = mapper.writeValueAsString(sample)

            assertThat(json).contains("\"createdAt\":\"2026-03-10T12:00:00\"")
        }

        @Test
        @DisplayName("모든 필드가 JSON에 포함된다")
        fun `all fields are present in JSON`() {
            val json = mapper.writeValueAsString(sample)

            assertThat(json)
                .contains("\"id\":1")
                .contains("\"name\":\"개인\"")
                .contains("\"color\":\"#007AFF\"")
                .contains("\"icon\":\"list.bullet\"")
                .contains("\"sortOrder\":0")
                .contains("\"reminderCount\":3")
        }
    }

    @Nested
    @DisplayName("역직렬화 (deserialization)")
    inner class Deserialization {

        @Test
        @DisplayName("ISO-8601 문자열에서 LocalDateTime으로 역직렬화된다")
        fun `deserializes createdAt from ISO-8601 string`() {
            val json = """
                {
                  "id": 1,
                  "name": "개인",
                  "color": "#007AFF",
                  "icon": "list.bullet",
                  "sortOrder": 0,
                  "reminderCount": 3,
                  "createdAt": "2026-03-10T12:00:00"
                }
            """.trimIndent()

            val response = mapper.readValue(json, ReminderListResponse::class.java)

            assertThat(response.createdAt).isEqualTo(LocalDateTime.of(2026, 3, 10, 12, 0))
            assertThat(response.name).isEqualTo("개인")
            assertThat(response.id).isEqualTo(1L)
        }

        @Test
        @DisplayName("직렬화 후 역직렬화하면 원본과 동일하다")
        fun `round-trip serialization produces equal object`() {
            val json = mapper.writeValueAsString(sample)
            val deserialized = mapper.readValue(json, ReminderListResponse::class.java)

            assertThat(deserialized).isEqualTo(sample)
        }
    }
}
