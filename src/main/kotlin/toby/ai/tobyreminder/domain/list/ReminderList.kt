package toby.ai.tobyreminder.domain.list

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "reminder_list")
class ReminderList(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var name: String,

    var color: String = "#007AFF",

    var icon: String = "list.bullet",

    var sortOrder: Int = 0,

    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun update(name: String, color: String, icon: String) {
        this.name = name
        this.color = color
        this.icon = icon
    }
}
