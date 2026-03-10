package toby.ai.tobyreminder.domain.reminder

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalTime

data class ReminderRequest(
    @field:NotBlank(message = "제목은 필수입니다")
    @field:Size(max = 255, message = "제목은 255자를 초과할 수 없습니다")
    val title: String,
    @field:Size(max = 2000, message = "메모는 2000자를 초과할 수 없습니다")
    val notes: String? = null,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val priority: Priority? = null,
    @JsonProperty("isFlagged") val isFlagged: Boolean? = null
)
