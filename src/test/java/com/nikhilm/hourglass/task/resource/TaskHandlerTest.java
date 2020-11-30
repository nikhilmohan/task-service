package com.nikhilm.hourglass.task.resource;

import com.nikhilm.hourglass.task.exceptions.TaskErrorAttributes;
import com.nikhilm.hourglass.task.exceptions.TaskException;
import com.nikhilm.hourglass.task.models.Task;
import com.nikhilm.hourglass.task.models.TaskResponse;
import com.nikhilm.hourglass.task.routers.RouterConfig;
import com.nikhilm.hourglass.task.services.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.requests.ApiError;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.awt.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

@WebFluxTest
@ContextConfiguration(classes = {RouterConfig.class, TaskHandler.class, TaskErrorAttributes.class})
@Slf4j
class TaskHandlerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    TaskService taskService;

    @MockBean
    ReactiveCircuitBreakerFactory factory;

    @Test
    public void testGetTasks()  {
        TaskResponse taskResponse = new TaskResponse();

        Task task = new Task();
        task.setName("New task");
        task.setDescription("A cool task");
        task.setDueDate(LocalDate.now().plusDays(5L));
        taskResponse.setTotalTasks(1L);
        Mockito.when(taskService.fetchTasksByUser("abc")).thenReturn(Mono.just(taskResponse));
        webTestClient.get().uri("http://localhost:9010/tasks")
                .header("user", "abc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskResponse.class)
                .value(response -> {
                    assertEquals(1L, response.getTotalTasks());
                });
    }
    @Test
    public void testGetTasksError()  {
        TaskResponse taskResponse = new TaskResponse();

        Task task = new Task();
        task.setName("New task");
        task.setDescription("A cool task");
        task.setDueDate(LocalDate.now().plusDays(5L));
        task.setUserId("abc");
        taskResponse.setTotalTasks(1L);

        Mockito.when(taskService.fetchTasksByUser("abc")).thenReturn(Mono.error(new TaskException(500, "Internal server error!")));
        webTestClient.get().uri("http://localhost:9010/tasks")
                .header("user", "abc")
                .exchange()
                .expectStatus().is5xxServerError();

    }
    @Test
    public void testAddTask()  {
        TaskResponse taskResponse = new TaskResponse();

        Task task = new Task();
        task.setName("New task");
        task.setDescription("A cool task");
        task.setDueDate(LocalDate.now().plusDays(5L));
        task.setUserId("abc");
        Mockito.when(taskService.addTask(any(Mono.class), eq("abc"))).thenReturn(Mono.just(task));

        webTestClient.post().uri("http://localhost:9010/task/add")
                .header("user", "abc")
                .body(BodyInserters.fromValue(task))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Task.class)
                .value(task1 -> {
                    assertEquals("New task", task1.getName());
                });
    }
    @Test
    public void testAddTaskInvalid()  {
        TaskResponse taskResponse = new TaskResponse();

        Task task = new Task();
        task.setName("New task");
        task.setDescription("A cool task");
        task.setDueDate(LocalDate.now().plusDays(5L));
        task.setUserId("abc");
        Mockito.when(taskService.addTask(any(Mono.class), eq("abc")))
                .thenReturn(Mono.error(new TaskException(400, "wrong input!")));

        webTestClient.post().uri("http://localhost:9010/task/add")
                .header("user", "abc")
                .body(BodyInserters.fromValue(task))
                .exchange()
                .expectStatus().isBadRequest();

    }
    @Test
    public void testAddTaskExists()  {
        TaskResponse taskResponse = new TaskResponse();

        Task task = new Task();
        task.setName("New task");
        task.setDescription("A cool task");
        task.setDueDate(LocalDate.now().plusDays(5L));
        task.setUserId("abc");
        Mockito.when(taskService.addTask(any(Mono.class), eq("abc")))
                .thenReturn(Mono.error(new TaskException(409, "Conflict!")));

        webTestClient.post().uri("http://localhost:9010/task/add")
                .header("user", "abc")
                .body(BodyInserters.fromValue(task))
                .exchange()
                .expectStatus().is4xxClientError();

    }
    @Test
    public void testCompleteTask()  {
        TaskResponse taskResponse = new TaskResponse();

        Task task = new Task();
        task.setName("New task");
        task.setDescription("A cool task");
        task.setDueDate(LocalDate.now().plusDays(5L));
        task.setUserId("abc");
        Mockito.when(taskService.completeTask(any(Mono.class), eq("abc")))
                .thenReturn(Mono.just(new Object()));

        webTestClient.post().uri("http://localhost:9010/task/complete")
                .header("user", "abc")
                .body(BodyInserters.fromValue(task))
                .exchange()
                .expectStatus().isNoContent();


    }
    @Test
    public void testCompleteTaskInvalid()  {
        TaskResponse taskResponse = new TaskResponse();

        Task task = new Task();
        task.setName("");
        task.setDescription("A cool task");
        task.setDueDate(LocalDate.now().plusDays(5L));
        task.setUserId("abc");
        Mockito.when(taskService.completeTask(any(Mono.class), eq("abc")))
                .thenReturn(Mono.error(new TaskException(400, "wrong input!")));

        webTestClient.post().uri("http://localhost:9010/task/complete")
                .header("user", "abc")
                .body(BodyInserters.fromValue(task))
                .exchange()
                .expectStatus().isBadRequest();


    }
}