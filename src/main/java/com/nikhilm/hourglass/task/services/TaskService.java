package com.nikhilm.hourglass.task.services;

import com.nikhilm.hourglass.task.exceptions.TaskException;
import com.nikhilm.hourglass.task.models.Task;
import com.nikhilm.hourglass.task.models.TaskResponse;
import com.nikhilm.hourglass.task.repositories.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class TaskService {

    @Autowired
    TaskRepository taskRepository;

    public Mono<TaskResponse> fetchTasks() {

        TaskResponse taskResponse = new TaskResponse();
        return taskRepository.findAll()
                .reduce(taskResponse, (response, task) -> {
                    response.getTasks().add(task);
                    response.setTotalTasks(response.getTotalTasks() + 1);
                    log.info("TaskResponse " + response);
                    return response;
                });
    }

    public Mono<Object> addTask( Mono<Task> task) {
        return task.flatMap(task1 -> {
            return taskRepository.findByName(task1.getName())
                    .flatMap(task2 -> Mono.error(new TaskException(409, "Conflict!")))
                    .switchIfEmpty(taskRepository.save(task1));

        });

    }

    public Mono<Void> completeTask( Mono<Task> task) {
        return task.flatMap(task1 -> {
            return taskRepository.findByName(task1.getName())
                    .switchIfEmpty(Mono.error(new TaskException(404, "No data found")))
                    .flatMap(taskRepository::delete);

                    });


    }
    public Mono<Object> checkTaskExists(Task task) {
        return taskRepository.findByName(task.getName())
                .switchIfEmpty(Mono.just(task))
                .then(Mono.error(new TaskException(409, "Conflict!!!")));

    }
}
