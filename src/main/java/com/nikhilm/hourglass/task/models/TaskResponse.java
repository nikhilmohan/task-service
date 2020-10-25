package com.nikhilm.hourglass.task.models;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskResponse {

        private List<Task> tasks = new ArrayList<>();
        private Long totalTasks = 0L;

}
