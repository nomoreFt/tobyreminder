package toby.ai.tobyreminder.domain.reminder

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import toby.ai.tobyreminder.domain.reminder.ports.`in`.ReminderService

@RestController
class ReminderController(
    private val reminderService: ReminderService
) {
    @GetMapping("/api/lists/{listId}/reminders")
    fun getRemindersByList(@PathVariable listId: Long): List<ReminderResponse> =
        reminderService.getRemindersByList(listId)

    @PostMapping("/api/lists/{listId}/reminders")
    @ResponseStatus(HttpStatus.CREATED)
    fun createReminder(
        @PathVariable listId: Long,
        @Valid @RequestBody request: ReminderRequest
    ): ReminderResponse =
        reminderService.createReminder(listId, request)

    @PatchMapping("/api/lists/{listId}/reminders/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun reorderReminders(
        @PathVariable listId: Long,
        @RequestBody request: ReorderRequest
    ) = reminderService.reorderReminders(listId, request)

    @GetMapping("/api/reminders")
    fun getRemindersByFilter(@RequestParam filter: String): List<ReminderResponse> =
        reminderService.getByFilter(ReminderFilter.from(filter))

    @PutMapping("/api/reminders/{id}")
    fun updateReminder(
        @PathVariable id: Long,
        @Valid @RequestBody request: ReminderRequest
    ): ReminderResponse =
        reminderService.updateReminder(id, request)

    @PatchMapping("/api/reminders/{id}/complete")
    fun toggleComplete(@PathVariable id: Long): ReminderResponse =
        reminderService.toggleComplete(id)

    @DeleteMapping("/api/reminders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteReminder(@PathVariable id: Long) =
        reminderService.deleteReminder(id)
}
