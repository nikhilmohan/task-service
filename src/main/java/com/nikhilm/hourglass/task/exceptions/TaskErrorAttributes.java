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

        if (getError(request).getMessage().contains("404")) {
            map.put("exception", getError(request));
            map.put("message", getError(request).getMessage());
            map.put("status", HttpStatus.NOT_FOUND);
            map.put("error", getError(request).getMessage());
            return map;
        }
        if (getError(request).getMessage().contains("409")) {
            map.put("exception", getError(request));
            map.put("message", getError(request).getMessage());
            map.put("status", HttpStatus.CONFLICT);
            map.put("error", getError(request).getMessage());
            return map;
        }
        map.put("exception", getError(request));
        map.put("message", getError(request).getMessage());
        map.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
        map.put("error", getError(request).getMessage());
        return map;
    }
}