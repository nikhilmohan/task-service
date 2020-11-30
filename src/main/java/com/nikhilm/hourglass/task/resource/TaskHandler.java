package com.nikhilm.hourglass.task.resource;

import com.mongodb.internal.connection.Server;
import com.nikhilm.hourglass.task.exceptions.TaskException;
import com.nikhilm.hourglass.task.models.Task;
import com.nikhilm.hourglass.task.models.TaskResponse;
import com.nikhilm.hourglass.task.repositories.TaskRepository;
import com.nikhilm.hourglass.task.services.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
@Slf4j
public class TaskHandler {

    @Autowired
    TaskService taskService;

    @Autowired
    ReactiveCircuitBreakerFactory factory;

    ReactiveCircuitBreaker rcb;

    public TaskHandler(ReactiveCircuitBreakerFactory factory)   {
        this.factory = factory;
        rcb = this.factory.create("task");
    }



    public Mono<ServerResponse> getTasks(ServerRequest request)   {
        String user = request.headers().header("user").get(0);
        log.info("user " + user);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskService.fetchTasksByUser(user), TaskResponse.class);

    }

    public Mono<ServerResponse> addTask(ServerRequest request) {
        String user = request.headers().header("user").get(0);
        log.info("user " + user);


//        if (validator.hasErrors(taskMono)) {
//            throw new TaskException(400, "wrong input!");
//
//        }


//        Mono<Task> responseBody = request
//                    .bodyToMono(Task.class)
//                    .map(body -> {
//                        if (validator.hasErrors(body)) {
//                          throw new TaskException(400, "wrong input!");
//
//                        }
//                        return body;
//
//
//
//                    });
//

//          return ServerResponse.ok()
//                            .contentType(MediaType.APPLICATION_JSON).build();

            return taskService.addTask(request.bodyToMono(Task.class), user)
                    .flatMap(task -> {
                        log.info("Created task" + task);
                        return ServerResponse.created(URI.create("/" + ((Task)(task)).getId()))
                                .body(fromValue(task));

                    });


    }

    public Mono<ServerResponse> completeTask(ServerRequest request) {

        String user = request.headers().header("user").get(0);
        log.info("user " + user);

//        Mono<Task> responseBody = request
//                .bodyToMono(Task.class)
//                .map(body -> {
//
//                    if (body.getName() == null || body.getName().isEmpty()) {
//                        throw new TaskException(400,"wrong input!");
//                    }
//                    return body;
//                });

        return taskService.completeTask(request.bodyToMono(Task.class), user)
                .then(ServerResponse.noContent().build());

    }




}
