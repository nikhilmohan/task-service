package com.nikhilm.hourglass.task.routers;

import com.nikhilm.hourglass.task.resource.TaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> route(TaskHandler taskHandler)  {
        return RouterFunctions.route(GET("/tasks").and(accept(MediaType.APPLICATION_JSON)),
                taskHandler::getTasks)
                .andRoute(POST("/task").and(accept(MediaType.APPLICATION_JSON)),
                        taskHandler::addTask)
                .andRoute(DELETE("/task").and(accept(MediaType.APPLICATION_JSON)),
                        taskHandler::completeTask);

    }

   

}