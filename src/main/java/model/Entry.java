package model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class Entry {
    private String id;
    private String site;
    private String login;
    private String password;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
