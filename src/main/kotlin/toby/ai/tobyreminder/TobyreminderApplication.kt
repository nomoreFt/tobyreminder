package toby.ai.tobyreminder

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TobyreminderApplication

fun main(args: Array<String>) {
	runApplication<TobyreminderApplication>(*args)
}
