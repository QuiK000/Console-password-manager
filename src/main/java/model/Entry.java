package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Arrays;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Entry {
    private String id;
    private String site;
    private String login;
    private char[] password;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void destroy() {
        if (this.password != null) {
            Arrays.fill(this.password, '\0');
        }
    }
}
