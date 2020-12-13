package com.nikhilm.hourglass.task.routers;

import com.nikhilm.hourglass.task.exceptions.ErrorMap;
import com.nikhilm.hourglass.task.exceptions.TaskErrorAttributes;
import com.nikhilm.hourglass.task.models.Task;
import com.nikhilm.hourglass.task.models.TaskResponse;
import com.nikhilm.hourglass.task.resource.TaskHandler;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Task service API",
                version = "1.0",
                description = "API for managing tasks in hourglass application",
                contact = @Contact(name = "Nikhil Mohan", email = "nikmohan81@gmail.com")
        )
)
public class RouterConfig {

    @Bean
    @RouterOperations({ @RouterOperation(path = "/tasks", beanClass = TaskHandler.class, beanMethod = "getTasks",
            operation = @Operation(operationId = "getTasks", summary = "Fetch all tasks for the user",
                    responses = { @ApiResponse(responseCode = "200", description = "list of tasks",
                            content = @Content(schema = @Schema(implementation = TaskResponse.class))),
                            @ApiResponse(responseCode = "401", description = "Unauthorized",
                                    content = @Content(schema = @Schema(implementation = ErrorMap.class))) })
            ),
            @RouterOperation(path = "/task/add", beanClass = TaskHandler.class, beanMethod = "addTask",
                    operation = @Operation(operationId = "addTask", summary = "Add a new task for the user",
                            requestBody = @RequestBody(required = true, description = "Task to add",
                                    content = @Content(
                                            schema = @Schema(implementation = Task.class))),
                            responses = { @ApiResponse(responseCode = "201", description = "Task created",
                                    content = @Content(schema = @Schema(implementation = Task.class))),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                                            content = @Content(schema = @Schema(implementation = ErrorMap.class))),
                                    @ApiResponse(responseCode = "409", description = "Task already exists",
                                            content = @Content(schema = @Schema(implementation = ErrorMap.class))) })
            ),
            @RouterOperation(path = "/task/complete", beanClass = TaskHandler.class, beanMethod = "completeTask",
                    operation = @Operation(operationId = "completeTask", summary = "Complete the task",
                            requestBody = @RequestBody(required = true, description = "Task to complete",
                                    content = @Content(
                                            schema = @Schema(implementation = Task.class))),
                            responses = { @ApiResponse(responseCode = "204", description = "Task completed",
                                    content = @Content),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                                    content = @Content(schema = @Schema(implementation = ErrorMap.class))),
                                    @ApiResponse(responseCode = "404", description = "Task not found",
                                            content = @Content(schema = @Schema(implementation = ErrorMap.class)))}))})
    public RouterFunction<ServerResponse> route(TaskHandler taskHandler)  {
        return RouterFunctions.route(GET("/tasks").and(accept(MediaType.APPLICATION_JSON)),
                taskHandler::getTasks)
                .andRoute(POST("/task/add").and(accept(MediaType.APPLICATION_JSON)),
                        taskHandler::addTask)
                .andRoute(POST("/task/complete").and(accept(MediaType.APPLICATION_JSON)),
                        taskHandler::completeTask);

    }

   

}