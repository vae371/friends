package main.pojo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class UserSession {
    private String username;
    private long time;
}
