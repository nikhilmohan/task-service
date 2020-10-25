package com.nikhilm.hourglass.task.repositories;

import com.nikhilm.hourglass.task.models.Task;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskRepository extends ReactiveMongoRepository<Task, String> {

    Mono<Task> findByName(String name);
}
