package yandex.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {

    private Long id;
    private String firstName;
    private String lastName;
    private int age;
    private boolean active;
}