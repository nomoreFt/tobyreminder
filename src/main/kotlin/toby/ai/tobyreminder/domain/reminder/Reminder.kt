package toby.ai.tobyreminder.domain.reminder

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "reminder")
class Reminder(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "list_id", nullable = false)
    var listId: Long,

    var title: String,

    @Column(columnDefinition = "TEXT")
    var notes: String? = null,

    var dueDate: LocalDate? = null,

    var dueTime: LocalTime? = null,

    @Enumerated(EnumType.STRING)
    var priority: Priority = Priority.NONE,

    var isFlagged: Boolean = false,

    var isCompleted: Boolean = false,

    var completedAt: LocalDateTime? = null,

    var sortOrder: Int = 0,

    val createdAt: LocalDateTime = LocalDateTime.now()
)
