package toby.ai.tobyreminder.domain.list

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import toby.ai.tobyreminder.domain.list.ports.`in`.ReminderListService

@RestController
@RequestMapping("/api/lists")
class ReminderListController(
    private val reminderListService: ReminderListService
) {
    @GetMapping
    fun getLists(): List<ReminderListResponse> =
        reminderListService.getLists()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createList(@RequestBody request: ReminderListRequest): ReminderListResponse =
        reminderListService.createList(request)

    @PutMapping("/{id}")
    fun updateList(
        @PathVariable id: Long,
        @RequestBody request: ReminderListRequest
    ): ReminderListResponse =
        reminderListService.updateList(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteList(@PathVariable id: Long) =
        reminderListService.deleteList(id)

    @PatchMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun reorderLists(@RequestBody request: ReorderRequest) =
        reminderListService.reorderLists(request)
}
