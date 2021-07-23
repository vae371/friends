package main.pojo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProgress {
    private int id;
    private String username;
    private int last_pos;
}
