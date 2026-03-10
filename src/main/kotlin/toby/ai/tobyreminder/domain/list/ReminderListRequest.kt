package toby.ai.tobyreminder.domain.list

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ReminderListRequest(
    @field:NotBlank(message = "목록 이름은 필수입니다")
    @field:Size(max = 100, message = "목록 이름은 100자를 초과할 수 없습니다")
    val name: String,
    val color: String? = null,
    val icon: String? = null
)
