package com.nikhilm.hourglass.task.services;

import com.nikhilm.hourglass.task.exceptions.TaskException;
import com.nikhilm.hourglass.task.models.Event;
import com.nikhilm.hourglass.task.models.Task;
import com.nikhilm.hourglass.task.models.TaskResponse;
import com.nikhilm.hourglass.task.repositories.TaskRepository;
import com.nikhilm.hourglass.task.resource.TaskValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@Slf4j
@EnableBinding(TaskService.MessageSources.class)
public class TaskService {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    private MessageSources messageSources;

    public Mono<TaskResponse> fetchTasksByUser(String user) {

        TaskResponse taskResponse = new TaskResponse();
        return taskRepository.findAllByUserId(user)
                .reduce(taskResponse, (response, task) -> {
                    response.getTasks().add(task);
                    response.setTotalTasks(response.getTotalTasks() + 1);
                    log.info("TaskResponse " + response);
                    return response;
                })
                .onErrorMap(throwable -> new TaskException(500, "Internal server error!"));
    }

    public Mono<Object> addTask( Mono<Task> task, String user) {
        return task.flatMap(task1 -> {
            TaskValidator validator = new TaskValidator();
            if (validator.hasErrors(task1)) {
                throw new TaskException(400, "Wrong input!");
            }
            return taskRepository.findByNameAndUserId(task1.getName(), user)
                    .onErrorMap(throwable -> {
                        return new TaskException(500, "Internal server error!");
                    })
                    .flatMap(task2 -> Mono.error(new TaskException(409, "Conflict!")))
                    .switchIfEmpty(Mono.defer(()->saveTaskAndSendEvent(task1)));

        });

    }

    public Mono<Object> completeTask( Mono<Task> task, String user) {
        return task.flatMap(task1 -> {

            log.info("task " + task1);
            if (task1.getName() == null || task1.getName().trim().isEmpty())   {

                throw new TaskException(400, "Wrong input!");
            }
            return taskRepository.findByNameAndUserId(task1.getName(), user)
                    .onErrorMap(throwable -> {
                        return new TaskException(500, "Internal server error!");
                    })
                    .switchIfEmpty(Mono.defer(()->Mono.error(new TaskException(404, "No data found"))))
                    .flatMap(foundTask -> {
                        return taskRepository.delete(foundTask)
                                .then(Mono.just(foundTask))
                                .onErrorMap(throwable -> new TaskException(500, "Internal server error!"));
                    })
                    .map(completedTask -> {
                        messageSources.outputTasks().send((MessageBuilder.withPayload(new Event(Event.Type.TASK_COMPLETED, completedTask.getId(), completedTask)).build()));
                        log.info("Complete task event published!");
                        return Mono.empty();
                    });

        });


    }
    public Mono<Object> checkTaskExists(Task task, String user) {
        return taskRepository.findByNameAndUserId(task.getName(), user)
                .onErrorMap(throwable -> new TaskException(500, "Internal server error!"))
                .switchIfEmpty(Mono.just(task))
                .then(Mono.error(new TaskException(409, "Conflict!!!")));

    }

    private Mono<Object> saveTaskAndSendEvent(Task task)    {
        return taskRepository.save(task)
                .onErrorMap(throwable -> new TaskException(500, "Internal server error!"))
                .map(savedTask -> {
                    messageSources.outputTasks().send((MessageBuilder.withPayload(new Event(Event.Type.TASK_ADDED, savedTask.getId(), savedTask)).build()));
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
