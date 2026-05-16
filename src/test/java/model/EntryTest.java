package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Entry")
public class EntryTest {
    private Entry makeEntry() {
        return Entry.builder()
                .id(UUID.randomUUID().toString())
                .site("github.com")
                .login("user@example.com")
                .password("s3cr3t".toCharArray())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("pushToHistory")
    class PushToHistoryTest {
        @Test
        @DisplayName("The first old password is added to the history")
        void firstPasswordAddedToHistory() {
            Entry entry = makeEntry();
            char[] old = "oldpass".toCharArray();

            entry.pushToHistory(old);
            assertThat(entry.getHistory()).hasSize(1);
            assertThat(entry.getHistory().getFirst()).isEqualTo(old);
        }

        @Test
        @DisplayName("The newest password is at the beginning of the history (LIFO)")
        void newestPasswordIsFirst() {
            Entry entry = makeEntry();

            entry.pushToHistory("first".toCharArray());
            entry.pushToHistory("second".toCharArray());

            assertThat(new String(entry.getHistory().getFirst())).isEqualTo("second");
            assertThat(new String(entry.getHistory().getLast())).isEqualTo("first");
        }

        @Test
        @DisplayName("The history does not store more than 2 entries (the oldest one is removed)")
        void historyLimitedToTwo() {
            Entry entry = makeEntry();

            entry.pushToHistory("pass1".toCharArray());
            entry.pushToHistory("pass2".toCharArray());
            entry.pushToHistory("pass3".toCharArray());

            assertThat(entry.getHistory()).hasSize(2);
            assertThat(new String(entry.getHistory().getFirst())).isEqualTo("pass3");
            assertThat(new String(entry.getHistory().get(1))).isEqualTo("pass2");
        }

        @Test
        @DisplayName("The evicted password is zeroed out in memory")
        void evictedPasswordIsZeroed() {
            Entry entry = makeEntry();
            char[] evicted = "evicted".toCharArray();

            entry.pushToHistory(evicted);
            entry.pushToHistory("second".toCharArray());
            entry.pushToHistory("third".toCharArray());

            for (char c : evicted) {
                assertThat(c).isEqualTo('\0');
            }
        }
    }

    @Nested
    @DisplayName("destroy")
    class DestroyTest {
        @Test
        @DisplayName("destroy zeroes out the current password")
        void destroyZerosCurrentPassword() {
            char[] password = "s3cr3t".toCharArray();
            Entry entry = Entry.builder()
                    .id(UUID.randomUUID().toString())
                    .site("site")
                    .login("login")
                    .password(password)
                    .createdAt(LocalDateTime.now())
                    .build();

            entry.destroy();
            for (char c : password) {
                assertThat(c).isEqualTo('\0');
            }
        }

        @Test
        @DisplayName("destroy zeroes out all passwords in the history")
        void destroyZerosHistory() {
            Entry entry = makeEntry();

            char[] hist1 = "old1".toCharArray();
            char[] hist2 = "old2".toCharArray();

            entry.pushToHistory(hist1);
            entry.pushToHistory(hist2);

            entry.destroy();

            for (char c : hist1) assertThat(c).isEqualTo('\0');
            for (char c : hist2) assertThat(c).isEqualTo('\0');

            assertThat(entry.getHistory()).isEmpty();
        }

        @Test
        @DisplayName("Calling destroy multiple times does not throw an exception")
        void doubleDestroyIsSafe() {
            Entry entry = makeEntry();
            assertThatCode(() -> {
                entry.destroy();
                entry.destroy();
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("equals / hashCode")
    class EqualsTest {
        @Test
        @DisplayName("Two entries with the same ID are considered equal")
        void saveIdIsEqual() {
            String id = UUID.randomUUID().toString();

            Entry e1 = Entry.builder()
                    .id(id).site("a")
                    .login("a")
                    .password(new char[0])
                    .createdAt(LocalDateTime.now())
                    .build();

            Entry e2 = Entry.builder()
                    .id(id).site("b")
                    .login("b")
                    .password(new char[0])
                    .createdAt(LocalDateTime.now())
                    .build();

            assertThat(e1).isEqualTo(e2);
        }

        @Test
        @DisplayName("Entries with different IDs are not equal")
        void differentIdIsNotEqual() {
            Entry e1 = makeEntry();
            Entry e2 = makeEntry();
            assertThat(e1).isNotEqualTo(e2);
        }
    }
}
