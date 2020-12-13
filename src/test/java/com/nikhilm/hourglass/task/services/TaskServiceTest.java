package com.nikhilm.hourglass.task.services;

import com.nikhilm.hourglass.task.exceptions.ErrorMap;
import com.nikhilm.hourglass.task.exceptions.TaskException;
import com.nikhilm.hourglass.task.models.Event;
import com.nikhilm.hourglass.task.models.Task;
import com.nikhilm.hourglass.task.models.TaskResponse;
import com.nikhilm.hourglass.task.repositories.TaskRepository;
import com.nikhilm.hourglass.task.resource.TaskValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    TaskService.MessageSources messageSources;

    @InjectMocks
    TaskService taskService;

    @Test
    public void testFetchTasksByUser()  {
        Task task = new Task();
        task.setUserId("abc");
        task.setDueDate(LocalDate.now().plusDays(10L));
        task.setName("first task");
        task.setDescription("first task is cool!");
        Task task1 = new Task();
        task1.setUserId("abc");
        task1.setDueDate(LocalDate.now().plusDays(14L));
        task1.setName("second task");
        task1.setDescription("second task is also cool!");
        Mockito.when(taskRepository.findAllByUserId("abc")).thenReturn(Flux.fromIterable(List.of(task, task1)));
        StepVerifier.create(taskService.fetchTasksByUser("abc"))
                .expectSubscription()
                .expectNextMatches(taskResponse1 -> taskResponse1.getTotalTasks() == 2
                        && taskResponse1.getTasks().size() == 2)
                .verifyComplete();

    }
    @Test
    public void testFetchTasksByUserError()  {
        Task task = new Task();
        task.setUserId("abc");
        task.setDueDate(LocalDate.now().plusDays(10L));
        task.setName("first task");
        task.setDescription("first task is cool!");
        Task task1 = new Task();
        task1.setUserId("abc");
        task1.setDueDate(LocalDate.now().plusDays(14L));
        task1.setName("second task");
        task1.setDescription("second task is also cool!");
        Mockito.when(taskRepository.findAllByUserId("abc")).thenReturn(Flux.error(new RuntimeException()));
        StepVerifier.create(taskService.fetchTasksByUser("abc"))
                .expectSubscription()
                .expectErrorMessage("Internal server error!")
                .verify();

    }
    @Test
    public void testAddTaskByUser()  {
        Task task = new Task();
        task.setUserId("abc");
        task.setDueDate(LocalDate.now().plusDays(10L));
        task.setName("first task");
        task.setDescription("first task is cool!");
        MessageChannel channel = mock(MessageChannel.class);
        Mockito.when(taskRepository.findByNameAndUserId("first task", "abc")).thenReturn(Mono.empty());
        Mockito.when(taskRepository.save(task)).thenReturn(Mono.just(task));
        Mockito.when(messageSources.outputTasks()).thenReturn(channel);
        Mockito.when(channel.send(any(Message.class))).thenReturn(true);

        StepVerifier.create(taskService.addTask(Mono.just(task), "abc"))
                .expectSubscription()
                .expectNextMatches(task1 -> task1 instanceof Task)
                .verifyComplete();

        verify(messageSources, times(1)).outputTasks();

    }

    @Test
    public void testAddTaskByUserExists()  {
        Task task = new Task();
        task.setUserId("abc");
        task.setDueDate(LocalDate.now().plusDays(10L));
        task.setName("first task");
        task.setDescription("first task is cool!");
        Mockito.when(taskRepository.findByNameAndUserId("first task", "abc")).thenReturn(Mono.just(task));

        StepVerifier.create(taskService.addTask(Mono.just(task), "abc"))
                .expectSubscription()
                .expectErrorMessage("Conflict!")
                .verify();

    }
    @Test
    public void testAddTaskError()  {
        Task task = new Task();
        task.setUserId("abc");
        task.setDueDate(LocalDate.now().plusDays(10L));
        task.setName("first task");
        task.setDescription("first task is cool!");
        Mockito.when(taskRepository.findByNameAndUserId("first task", "abc"))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(taskService.addTask(Mono.just(task), "abc"))
                .expectSubscription()
                .expectErrorMessage("Internal server error!")
                .verify();

    }
    @Test
    public void testAddTaskInvalid()  {
        Task task = new Task();
        task.setUserId("abc");
        task.setDueDate(LocalDate.now().plusDays(10L));
        task.setName("");
        task.setDescription("first task is cool!");
        StepVerifier.create(taskService.addTask(Mono.just(task), "abc"))
                .expectSubscription()
                .expectErrorMessage("Wrong input!")
                .verify();

    }
    @Test
    public void testCompleteTaskFailure()  {
        Task task = new Task();
        task.setUserId("abc");
        task.setDueDate(LocalDate.now().plusDays(10L));
        task.setName("first task");
        task.setDescription("first task is cool!");
        Mockito.when(taskRepository.findByNameAndUserId("first task", "abc"))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(taskService.completeTask(Mono.just(task), "abc"))
                .expectSubscription()
                .expectErrorMessage("Internal server error!")
                .verify();

    }
    @Test
    public void testCompleteTaskInvalid()  {
        Task task = new Task();
        task.setUserId("abc");
        task.setDueDate(LocalDate.now().plusDays(10L));
        task.setName("");
        task.setDescription("first task is cool!");
        StepVerifier.create(taskService.completeTask(Mono.just(task), "abc"))
                .expectSubscription()
                .expectErrorMessage("Wrong input!")
                .verify();

    }
    @Test
    public void testCompleteTaskByUser()  {
        ArgumentCaptor<Message> eventCaptor = ArgumentCaptor.forClass(Message.class);
        //Mono<Void> voidMono = mock(Mono.empty().getClass());
        Task task = new Task();
        task.setUserId("abc");
        task.setDueDate(LocalDate.now().plusDays(10L));
        task.setName("first task");
        task.setDescription("first task is cool!");
        task.setId("abcdef");
        MessageChannel channel = mock(MessageChannel.class);
        Mockito.when(taskRepository.findByNameAndUserId("first task", "abc")).thenReturn(Mono.just(task));
        when(taskRepository.delete(task)).thenReturn(Mono.empty());
        Mockito.when(messageSources.outputTasks()).thenReturn(channel);
        Mockito.when(channel.send(eventCaptor.capture())).thenReturn(true);

        StepVerifier.create(taskService.completeTask(Mono.just(task), "abc"))
                .expectSubscription()
                .expectNext(Mono.empty())
                .verifyComplete();

        verify(messageSources, times(1)).outputTasks();
        Event event = (Event)eventCaptor.getValue().getPayload();
        Event justEvent = new Event();
        Task eventData = (Task) event.getData();
        assertTrue(event.getEventType().equals(Event.Type.TASK_COMPLETED));
        assertTrue(event.getKey().toString().equalsIgnoreCase(task.getId()));
        assertTrue(event.getData() instanceof Task);



    }
    @Test
    public void testCompleteTaskNotFound()  {
        Task task = new Task();
        task.setUserId("abc");
        task.setName("first task");
        task.setDescription("A cool first task!");
        Mockito.when(taskRepository.findByNameAndUserId("first task", "abc")).thenReturn(Mono.empty());
        StepVerifier.create(taskService.completeTask(Mono.just(task), "abc"))
                .expectSubscription()
                .expectErrorMessage("No data found")
                .verify();

    }
    @Test
    public void testCompleteTaskError()  {
        Task task = new Task();
        task.setUserId("abc");
        task.setName("first task");
        task.setDescription("A cool first task!");
        when(taskRepository.findByNameAndUserId("first task", "abc")).thenReturn(Mono.just(task));
        when(taskRepository.delete(task)).thenReturn(Mono.error(new RuntimeException()));
        StepVerifier.create(taskService.completeTask(Mono.just(task), "abc"))
                .expectSubscription()
                .expectErrorMessage("Internal server error!")
                .verify();

    }
    @Test
    public void testCheckTaskExists()   {
        Task task = new Task();
        task.setUserId("abc");
        task.setName("first task");
        Mockito.when(taskRepository.findByNameAndUserId("first task", "abc")).thenReturn(Mono.just(task));
        StepVerifier.create(taskService.checkTaskExists(task, "abc"))
                .expectSubscription()
                .expectErrorMessage("Conflict!!!")
                .verify();

    }
    @Test
    public void testErrorMap()   {
        ErrorMap errorMap = new ErrorMap("400", "Bad request", "TaskException", "400");

        assertEquals("400", errorMap.getError());
        assertEquals("400", errorMap.getStatus());
        assertEquals("Bad request", errorMap.getMessage());
        assertEquals("TaskException", errorMap.getException());
    }


}