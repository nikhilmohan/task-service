package com.nikhilm.hourglass.task.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Component
@Slf4j
public class TaskErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        log.info("Exception is " + getError(request).getMessage() +":"+ getError(request));
        Map<String, Object> map = super.getErrorAttributes(request, includeStackTrace);

        if (getError(request) instanceof TaskException) {
            TaskException ex = (TaskException) getError(request);
            map.put("exception", ex.getClass().getSimpleName());
            map.put("message", ex.getMessage());
            map.put("status", ex.getStatus());
            map.put("error", ex.getMessage());

            log.info("Does it get here!");
            return map;
        }
        log.info("Exception " + getError(request).getMessage() +":"+ getError(request));

        return createMap(map, getError(request));
    }
    public Map<String, Object> createMap(Map<String, Object> map, Throwable throwable) {
        log.info("Exception message " + throwable.getMessage());
        String status = "500";
        if (throwable.getMessage().contains("404")) {
            status = "404";
        }
        if (throwable.getMessage().contains("409")) {

            status = "409";
        }


        switch(status)  {
            case "404":
                map.put("exception", throwable);
                map.put("message", throwable.getMessage());
                map.put("status", HttpStatus.NOT_FOUND);
                map.put("error", throwable.getMessage());
                break;
            case "409":
                map.put("exception", throwable);
                map.put("message", throwable.getMessage());
                map.put("status", HttpStatus.CONFLICT);
                map.put("error", throwable.getMessage());
                break;
            default:
                map.put("exception", throwable);
                map.put("message", throwable.getMessage());
                map.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
                map.put("error", throwable.getMessage());
                break;
        }
        return map;
    }
}