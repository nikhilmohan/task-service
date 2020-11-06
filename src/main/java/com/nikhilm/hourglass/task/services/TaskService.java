package com.nikhilm.hourglass.task.services;

import com.nikhilm.hourglass.task.exceptions.TaskException;
import com.nikhilm.hourglass.task.models.Event;
import com.nikhilm.hourglass.task.models.Task;
import com.nikhilm.hourglass.task.models.TaskResponse;
import com.nikhilm.hourglass.task.repositories.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.nikhilm.hourglass.task.models.Event.Type.ADD;
import static com.nikhilm.hourglass.task.models.Event.Type.COMPLETE;

@Service
@Slf4j
@EnableBinding(TaskService.MessageSources.class)
public class TaskService {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    private MessageSources messageSources;

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
                    .switchIfEmpty(Mono.defer(()->saveTaskAndSendEvent(task1)));
        });

    }

    public Mono<Object> completeTask( Mono<Task> task) {
        return task.flatMap(task1 -> {
            return taskRepository.findByName(task1.getName())
                    .switchIfEmpty(Mono.defer(()->Mono.error(new TaskException(404, "No data found"))))
                    .flatMap(foundTask -> {
                        return taskRepository.delete(foundTask).thenReturn(foundTask);
                    })
                    .map(completedTask -> {
                        messageSources.outputTasks().send((MessageBuilder.withPayload(new Event(COMPLETE, completedTask.getId(), completedTask)).build()));
                        log.info("Complete task event published!");
                        return Mono.empty();
                    });

        });


    }
    public Mono<Object> checkTaskExists(Task task) {
        return taskRepository.findByName(task.getName())
                .switchIfEmpty(Mono.just(task))
                .then(Mono.error(new TaskException(409, "Conflict!!!")));

    }

    private Mono<Object> saveTaskAndSendEvent(Task task)    {
        return taskRepository.save(task)
                .map(savedTask -> {
                    messageSources.outputTasks().send((MessageBuilder.withPayload(new Event(ADD, savedTask.getId(), savedTask)).build()));
                    log.info("Add task event published!");
                    return savedTask;
                });
    }

    public interface MessageSources {

        String OUTPUT_TASKS = "output-tasks";

        @Output(OUTPUT_TASKS)
        MessageChannel outputTasks();

    }

}
