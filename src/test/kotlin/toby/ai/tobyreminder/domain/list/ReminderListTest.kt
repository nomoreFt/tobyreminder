package toby.ai.tobyreminder.domain.list

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("ReminderList 엔티티")
class ReminderListTest {

    @Nested
    @DisplayName("생성자")
    inner class Constructor {

        @Test
        @DisplayName("이름만 지정하면 나머지 필드는 기본값으로 생성된다")
        fun `creates with default values when only name is given`() {
            val list = ReminderList(name = "개인")

            assertThat(list.name).isEqualTo("개인")
            assertThat(list.color).isEqualTo("#007AFF")
            assertThat(list.icon).isEqualTo("list.bullet")
            assertThat(list.sortOrder).isEqualTo(0)
        }

        @Test
        @DisplayName("모든 필드를 직접 지정해서 생성할 수 있다")
        fun `creates with all fields specified`() {
            val list = ReminderList(
                name = "업무",
                color = "#FF3B30",
                icon = "briefcase",
                sortOrder = 3
            )

            assertThat(list.name).isEqualTo("업무")
            assertThat(list.color).isEqualTo("#FF3B30")
            assertThat(list.icon).isEqualTo("briefcase")
            assertThat(list.sortOrder).isEqualTo(3)
        }
    }

    @Nested
    @DisplayName("update()")
    inner class Update {

        @Test
        @DisplayName("이름, 색상, 아이콘을 한번에 변경할 수 있다")
        fun `updates name, color, and icon at once`() {
            val list = ReminderList(name = "개인", color = "#007AFF", icon = "list.bullet")

            list.update(name = "업무", color = "#FF3B30", icon = "briefcase")

            assertThat(list.name).isEqualTo("업무")
            assertThat(list.color).isEqualTo("#FF3B30")
            assertThat(list.icon).isEqualTo("briefcase")
        }

        @Test
        @DisplayName("update() 호출 후 sortOrder와 createdAt은 변경되지 않는다")
        fun `sortOrder and createdAt are not affected by update()`() {
            val before = LocalDateTime.now()
            val list = ReminderList(name = "개인", sortOrder = 2)

            list.update(name = "수정됨", color = "#34C759", icon = "star")

            assertThat(list.sortOrder).isEqualTo(2)
            assertThat(list.createdAt).isAfterOrEqualTo(before)
        }
    }

    @Nested
    @DisplayName("createdAt 자동 등록")
    inner class CreatedAt {

        @Test
        @DisplayName("생성 시각이 자동으로 현재 시간으로 등록된다")
        fun `createdAt is set to current time on instantiation`() {
            val before = LocalDateTime.now()
            val list = ReminderList(name = "테스트")
            val after = LocalDateTime.now()

            assertThat(list.createdAt)
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after)
        }

        @Test
        @DisplayName("서로 다른 시점에 생성된 인스턴스는 다른 createdAt을 가진다")
        fun `two instances created at different times have different createdAt`() {
            val first = ReminderList(name = "첫번째")
            Thread.sleep(10)
            val second = ReminderList(name = "두번째")

            assertThat(second.createdAt).isAfter(first.createdAt)
        }

        @Test
        @DisplayName("createdAt은 불변이다 (val)")
        fun `createdAt is immutable`() {
            // val 필드이므로 컴파일 타임에 보장됨 — 런타임에서 타입 확인
            val list = ReminderList(name = "테스트")

            val field = list::class.java.getDeclaredField("createdAt")
            // Kotlin val → JVM final 여부 확인
            assertThat(java.lang.reflect.Modifier.isFinal(field.modifiers)).isTrue()
        }
    }
}
