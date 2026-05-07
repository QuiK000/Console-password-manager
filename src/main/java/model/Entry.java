package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Builder
@Getter
@ToString(exclude = "password")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Entry {
    private String id;
    private String site;
    private String login;
    @Setter
    private char[] password;
    private String notes;
    private LocalDateTime createdAt;
    @Setter
    private LocalDateTime updatedAt;
    @Builder.Default
    private List<char[]> history = new ArrayList<>();

    public void pushToHistory(char[] oldPassword) {
        if (history == null) history = new ArrayList<>();
        history.addFirst(oldPassword);
        if (history.size() > 2) {
            char[] removed = history.removeLast();
            Arrays.fill(removed, '\0');
        }
    }

    public void destroy() {
        if (this.password != null) Arrays.fill(this.password, '\0');
        if (this.history != null) {
            for (char[] p : history) {
                if (p != null) {
                    Arrays.fill(p, '\0');
                }
            }

            history.clear();
        }
    }
}
