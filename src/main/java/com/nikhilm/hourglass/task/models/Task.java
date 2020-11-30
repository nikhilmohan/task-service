package com.nikhilm.hourglass.task.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "tasks")
public class Task {
    private String id;
    private String userId;
    private String name;
    private String description;
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate dueDate;


}
